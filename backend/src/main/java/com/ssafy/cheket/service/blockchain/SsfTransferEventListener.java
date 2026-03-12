package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.entity.eventsyncstatus.EventSyncStatus;
import com.ssafy.cheket.entity.transaction.Transaction.TxStatus;
import com.ssafy.cheket.repository.wallet.EventSyncStatusRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.wallet.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * SSF(ERC-20) Transfer 이벤트 폴러
 *
 * [이 클래스가 하는 일 — 한 줄 요약] 5초마다 "혹시 새로운 SSF 전송 있었어?" 하고 블록체인에 물어보는 역할.
 *
 * [왜 HTTP 폴링인가? (WebSocket 대신)] - WebSocket(eth_subscribe): 실시간이지만, 서버 다운 시 그
 * 사이 이벤트를 놓침 - HTTP 폴링(eth_getLogs): 5초 딜레이가 있지만, 체크포인트(event_sync_status)로 서버가
 * 다운됐다가 재시작해도 놓친 블록을 전부 다시 조회할 수 있음 → SSAFY 블록 생성 주기가 10초이므로, 5초 폴링이면 모든 블록 커버
 * 가능
 *
 * [전체 흐름] 1. event_sync_status 테이블에서 "마지막으로 처리한 블록"(체크포인트) 읽기 2. 체크포인트+1 ~ 현재
 * 최신 블록 범위로 eth_getLogs 호출 3. Transfer 이벤트가 있으면: a) txHash로 transaction 테이블 매칭
 * → SUBMITTED → CONFIRMED 업데이트 b) 수신 지갑의 ctkBalance 업데이트 4. 체크포인트를 현재 블록으로 갱신
 *
 * [ERC-20 Transfer 이벤트 구조] event Transfer(address indexed from, address indexed
 * to, uint256 value) * Transfer(address indexed from, address indexed to,
 * uint256 value) ↑ indexed ↑ indexed ↑ 안 붙음 topics[1] topics[2] data - indexed
 * 파라미터: 로그의 topics[] 배열에 들어감 * topics — "검색용 인덱스" (eth_getLogs에서 "from이 0x1234인
 * Transfer만 줘!" 같은 조건 가능) topics[0] = 이벤트 시그니처 토픽 해시
 * (keccak256("Transfer(address,address,uint256)")) (자동, "이게 Transfer 이벤트야" 식별용)
 * topics[1] = from 주소 (32바이트, 좌측 0 패딩) topics[2] = to 주소 (32바이트, 좌측 0 패딩) -
 * non-indexed 파라미터: 로그의 data 필드에 들어감 data = value (전송량, uint256) -> 검색 불가능 (읽기만
 * 가능, 크기 제한 없이 자유롭게 저장)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsfTransferEventListener {

    // 이 리스너의 고유 이름 — event_sync_status 테이블에서 체크포인트를 찾을 때 사용
    private static final String LISTENER_NAME = "SSF_TRANSFER";

    // 한 번에 최대 100블록만 스캔 (너무 많으면 RPC 서버에 부하)
    // 서버가 오래 다운됐다가 재시작하면, 100블록씩 나눠서 따라잡음
    private static final long MAX_BLOCK_RANGE = 100;

    /**
     * ERC-20 표준 Transfer 이벤트 정의 Transfer(address indexed from, address indexed to,
     * uint256 value)
     *
     * // SSF 토큰 컨트랙트 안에 이렇게 정의되어 있음 event Transfer( address indexed from, //
     * indexed 붙음 → topics에 저장 address indexed to, // indexed 붙음 → topics에 저장
     * uint256 value // indexed 안 붙음 → data에 저장 ); - new
     * TypeReference<Address>(true) → indexed = true (topics에 들어감) - new
     * TypeReference<Uint256>() → indexed = false (data에 들어감)
     */
    private static final Event TRANSFER_EVENT = new Event("Transfer", Arrays.asList(new TypeReference<Address>(true) {
    }, // from (indexed → topics[1])
        new TypeReference<Address>(true) {
        }, // to (indexed → topics[2])
        new TypeReference<Uint256>() {
        } // value (non-indexed → data)
    ));

    // Transfer 이벤트의 토픽 해시 (keccak256으로 자동 생성), 토픽 해시 = 이벤트의 "지문"
    // topics[0] = 0xddf252... ← "이 로그는 Transfer 이벤트야" (토픽 해시)
    // 이벤트 이름 + 파라미터 타입을 해시 함수에 넣어서 나온 고유 값
    // eth_getLogs 필터에 이 토픽을 넣으면, Transfer 이벤트만 골라서 조회됨
    private static final String TRANSFER_EVENT_TOPIC = EventEncoder.encode(TRANSFER_EVENT);

    private final Web3j web3j; // 블록체인 RPC 클라이언트
    private final EventSyncStatusRepository eventSyncStatusRepository; // 체크포인트 저장소
    private final TransactionRepository transactionRepository; // 트랜잭션 DB 조회
    private final TransactionService transactionService; // 트랜잭션 상태 업데이트
    private final WalletRepository walletRepository; // 지갑 잔액 업데이트

    // SSF 토큰 컨트랙트 주소 (application.yml에서 주입)
    @Value("${blockchain.ssf-contract-address}")
    private String ssfContractAddress;

    /**
     * 5초 간격으로 SSF Transfer 이벤트 폴링
     *
     * fixedDelay = 5000 → 이전 실행이 "끝나고" 5초 후에 다음 실행 (fixedRate와 다름: fixedRate는 이전 실행
     * "시작" 후 5초)
     *
     * SSAFY 블록 생성 주기: 10초 → 5초 간격이면 블록 하나당 최소 1번은 조회하므로 놓칠 일 없음
     */
    @Scheduled(fixedDelay = 5000)
    public void pollTransferEvents() {
        try {
            // ── 1단계: 현재 최신 블록 번호 조회 ──
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();

            // ── 2단계: 체크포인트(마지막 동기화 블록) 조회 ──
            // 첫 실행 시 DB에 레코드가 없으면 → 현재 블록-1로 초기화
            // (현재 블록부터 리스닝 시작하겠다는 의미)
            // 리스너 이름으로 "내 체크포인트"를 찾음
            EventSyncStatus syncStatus = eventSyncStatusRepository.findByListenerName(LISTENER_NAME) // DB에서 찾기
                .orElseGet(() -> { // 없으면 (첫 실행)
                    EventSyncStatus newStatus = EventSyncStatus.builder().listenerName(LISTENER_NAME)
                        .lastSyncedBlock(latestBlock.longValue() - 1) // 현재 블록 - 1
                        .build();
                    return eventSyncStatusRepository.save(newStatus); // DB에 저장
                });

            long fromBlock = syncStatus.getLastSyncedBlock() + 1; // 체크포인트 + 1부터 읽기
            long toBlock = latestBlock.longValue(); // 현재 최신 블록까지

            // 새 블록이 없으면 (아직 10초 안 지남) → 이번 사이클 스킵
            if (fromBlock > toBlock)
                return;

            // RPC 부하 방지: 한 번에 최대 100블록만
            // 서버가 오래 다운됐으면, 여러 사이클에 걸쳐 100블록씩 따라잡음
            if (toBlock - fromBlock > MAX_BLOCK_RANGE) {
                toBlock = fromBlock + MAX_BLOCK_RANGE;
            }

            // ── 3단계: eth_getLogs로 Transfer 이벤트 조회 ──
            // EthFilter: "fromBlock ~ toBlock 범위에서, ssfContractAddress의 로그 중,
            // topics[0]이 TRANSFER_EVENT_TOPIC인 것만 줘!"
            EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(BigInteger.valueOf(fromBlock)),
                DefaultBlockParameter.valueOf(BigInteger.valueOf(toBlock)), ssfContractAddress // SSF 컨트랙트 주소만 필터링
            );
            filter.addSingleTopic(TRANSFER_EVENT_TOPIC); // Transfer 이벤트만

            // "블록 1001~1010 사이에 SSF Transfer 이벤트 있었어?" 하고 물어보는 것
            // ethLog.getLogs()가 비어있으면 → 그 블록 범위에서 SSF 전송이 없었다는 뜻이고, 있으면 → 하나씩 꺼내서
            // processTransferLog()로 처리
            EthLog ethLog = web3j.ethGetLogs(filter).send();
            List<EthLog.LogResult> logs = ethLog.getLogs();

            if (logs != null && !logs.isEmpty()) {
                log.info("SSF Transfer 이벤트 감지: {}건 (블록 {}-{})", logs.size(), fromBlock, toBlock);

                for (EthLog.LogResult logResult : logs) {
                    if (logResult instanceof EthLog.LogObject) {
                        // 각 Transfer 이벤트를 하나씩 처리
                        // eth_getLogs는 블록에 '이미 포함된' 이벤트만 반환
                        // mempool에 있는(아직 대기 중인) tx는 안 나옴
                        // eth_getLogs에서 나옴 = 블록에 포함됨 = 확정!
                        processTransferLog(((EthLog.LogObject) logResult).get());
                    }
                }
            }

            // ── 4단계: 체크포인트 업데이트 ──
            // "여기까지 다 처리했어" → 다음 폴링은 toBlock+1부터 시작
            syncStatus.setLastSyncedBlock(toBlock);
            eventSyncStatusRepository.save(syncStatus);

        } catch (Exception e) {
            // 에러가 나도 체크포인트는 갱신 안 됨 → 다음 폴링에서 같은 범위 재시도
            // 에러 발생 시 여기로 점프 → 4단계를 안 거침 → 체크포인트 안 바뀜
            log.error("SSF Transfer 이벤트 폴링 에러", e);
        }
    }

    /**
     * 개별 Transfer 이벤트 로그 처리
     *
     * [이벤트 로그 구조] topics[0] = 이벤트 시그니처 (Transfer 이벤트 해시) topics[1] = from 주소
     * (32바이트, 앞 12바이트는 0 패딩)
     * "0x000000000000000000000000e303cba22b780b91f947917cd2ee951d066c0fb8" →
     * substring(26) → "e303cba22b780b91f947917cd2ee951d066c0fb8" → "0x" 붙이기 → 실제 주소
     * topics[2] = to 주소 (같은 방식) data = value (전송량, hex 인코딩된 uint256) * logEntry = {
     * transactionHash: "0xabc123...", // 이 전송의 블록체인 tx 해시 blockNumber: 1005, // 몇
     * 번째 블록에 들어갔는지
     *
     * topics: [ // indexed 파라미터들 "0xddf252...", // [0] 이벤트 시그니처 (Transfer 식별용)
     * "0x000...e303cba22b...", // [1] from 주소 (32바이트, 앞에 0 패딩)
     * "0x000...1234abcd56..." // [2] to 주소 (32바이트, 앞에 0 패딩) ],
     *
     * data: "0x0000...0064" // non-indexed: value = 100 SSF (hex) } logEntry =
     * "누가(from) 누구에게(to) 얼마(value) 보냈는지" + "어느 블록(blockNumber)에서, 어느
     * 트랜잭션(txHash)으로" 담긴 이벤트 로그 1건.
     */
    // 그래서 ethLog.getLogs()가 비어있으면 → 그 블록 범위에서 SSF 전송이 없었다는 뜻이고, 있으면 → 하나씩 꺼내서
    // processTransferLog()로 처리
    private void processTransferLog(Log logEntry) {
        try {
            String txHash = logEntry.getTransactionHash();
            BigInteger blockNumber = logEntry.getBlockNumber();

            // 이벤트 디코딩
            // topics[1]에서 주소 추출: 32바이트 hex에서 뒤 20바이트만 (앞 12바이트는 0패딩)
            // "0x" + 2글자 + 24글자(0패딩) + 40글자(주소) = 66글자
            // substring(26) → 40글자 주소부분만 추출
            String fromAddress = "0x" + logEntry.getTopics().get(1).substring(26);
            String toAddress = "0x" + logEntry.getTopics().get(2).substring(26);
            BigInteger value = Numeric.toBigInt(logEntry.getData());

            log.info("Transfer: {} → {} | {} SSF (tx: {}, block: {})", fromAddress, toAddress, value, txHash,
                blockNumber);

            // ── DB 매칭: txHash로 우리 시스템의 트랜잭션 찾기 ──
            // WalletServiceImpl이 tx 전송 시 txHash를 DB에 저장했었음 (SUBMITTED 상태)
            // 여기서 그 레코드를 찾아서 CONFIRMED로 업데이트
            transactionRepository.findByTxHash(txHash).ifPresent(transaction -> {
                if (transaction.getTxStatus() == TxStatus.SUBMITTED) {
                    transactionService.updateToConfirmed(transaction.getId(), blockNumber.longValue());
                    log.info("Transaction 확정: id={}, txHash={}", transaction.getId(), txHash);
                }
            });

            // ── 수신 지갑 잔액 업데이트 ──
            // 우리 시스템에 등록된 지갑이면 ctkBalance에 받은 금액 더해줌
            // 외부에서 직접 보낸 SSF도 여기서 잡힘 (우리가 보낸 것만이 아니라, 모든 Transfer)
            // SSAFY 네트워크 안이지만 우리 서버(API)를 안 거치고 직접 보낸 SSF도 반영됨
            walletRepository.findByAddressIgnoreCase(toAddress).ifPresent(wallet -> {
                int currentBalance = wallet.getCtkBalance() != null ? wallet.getCtkBalance() : 0;
                wallet.setCtkBalance(currentBalance + value.intValue());
                wallet.setBalanceSyncedAt(LocalDateTime.now());
                walletRepository.save(wallet);
                log.info("지갑 잔액 업데이트: {} → {} SSF", toAddress, wallet.getCtkBalance());
            });

        } catch (Exception e) {
            log.error("Transfer 이벤트 처리 에러: {}", logEntry.getTransactionHash(), e);
        }
    }
}
