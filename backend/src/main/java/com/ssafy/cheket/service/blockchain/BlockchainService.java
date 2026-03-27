package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.blockchain.contract.Escrow;
import com.ssafy.cheket.blockchain.contract.EventNFT;
import com.ssafy.cheket.blockchain.contract.Marketplace;
import com.ssafy.cheket.blockchain.contract.PurchaseRouter;
import com.ssafy.cheket.blockchain.contract.Settlement;
import com.ssafy.cheket.blockchain.contract.StakeholderNFT;
import com.ssafy.cheket.blockchain.contract.TicketNFT;
import com.ssafy.cheket.exception.common.BlockchainException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 블록체인 서비스 — 7개 컨트랙트 통합 관리
 *
 * 플랫폼 지갑 키, nonce, 가스비 등 공통 설정을 한 곳에서 관리하고, 각 컨트랙트 wrapper 인스턴스를 제공한다.
 */
@Slf4j
@Service
public class BlockchainService {

    private final Web3j web3j;

    @Value("${blockchain.platform-private-key}")
    private String platformPrivateKey;

    @Value("${blockchain.chain-id}")
    private long chainId;

    @Getter
    @Value("${blockchain.ssf-contract-address}")
    private String ssfContractAddress;

    @Value("${blockchain.stakeholder-nft-address}")
    private String stakeholderNftAddress;

    @Value("${blockchain.event-nft-address}")
    private String eventNftAddress;

    @Value("${blockchain.ticket-nft-address}")
    private String ticketNftAddress;

    @Value("${blockchain.settlement-address}")
    private String settlementAddress;

    @Value("${blockchain.purchase-router-address}")
    private String purchaseRouterAddress;

    @Value("${blockchain.marketplace-address}")
    private String marketplaceAddress;

    @Value("${blockchain.escrow-address}")
    private String escrowAddress;

    // 컨트랙트 인스턴스
    private StakeholderNFT stakeholderNFT;
    private EventNFT eventNFT;
    private TicketNFT ticketNFT;
    private PurchaseRouter purchaseRouter;
    private Settlement settlement;
    private Marketplace marketplace;
    private Escrow escrow;

    // 플랫폼 지갑 정보
    private Credentials platformCredentials;
    private String platformAddress;

    // 플랫폼 지갑 Nonce 충돌 방지 락 — 플랫폼 지갑에서 보내는 TX는 반드시 이 락을 통해 직렬화
    private final ReentrantLock nonceLock = new ReentrantLock();

    // SSAFY 네트워크: gasPrice = 0
    private static final StaticGasProvider GAS_PROVIDER = new StaticGasProvider(BigInteger.ZERO,
        BigInteger.valueOf(3000000));

    public BlockchainService(Web3j web3j) {
        this.web3j = web3j;
    }

    @PostConstruct
    public void init() {
        platformCredentials = Credentials.create(platformPrivateKey);
        platformAddress = platformCredentials.getAddress();

        RawTransactionManager txManager = new RawTransactionManager(web3j, platformCredentials, chainId);

        stakeholderNFT = StakeholderNFT.load(stakeholderNftAddress, web3j, txManager, GAS_PROVIDER);
        eventNFT = EventNFT.load(eventNftAddress, web3j, txManager, GAS_PROVIDER);
        ticketNFT = TicketNFT.load(ticketNftAddress, web3j, txManager, GAS_PROVIDER);
        purchaseRouter = PurchaseRouter.load(purchaseRouterAddress, web3j, txManager, GAS_PROVIDER);
        settlement = Settlement.load(settlementAddress, web3j, txManager, GAS_PROVIDER);
        marketplace = Marketplace.load(marketplaceAddress, web3j, txManager, GAS_PROVIDER);
        escrow = Escrow.load(escrowAddress, web3j, txManager, GAS_PROVIDER);

        log.info("BlockchainService 초기화 완료 — 플랫폼 지갑: {}", platformAddress);
    }

    // -- Getter: 컨트랙트 인스턴스 --
    public StakeholderNFT getStakeholderNFT() {
        return stakeholderNFT;
    }

    public EventNFT getEventNFT() {
        return eventNFT;
    }

    public TicketNFT getTicketNFT() {
        return ticketNFT;
    }

    public PurchaseRouter getPurchaseRouter() {
        return purchaseRouter;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public Escrow getEscrow() {
        return escrow;
    }

    // -- Getter: 플랫폼 지갑 정보 --
    public Credentials getPlatformCredentials() {
        return platformCredentials;
    }

    public String getPlatformAddress() {
        return platformAddress;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public long getChainId() {
        return chainId;
    }

    public String getPlatformWalletAddress() {
        return platformAddress;
    }

    /**
     * 플랫폼 지갑 TX 직렬화 실행기
     *
     * 플랫폼 지갑(단일 계정)에서 보내는 모든 상태 변경 TX는 이 메서드를 통해 실행해야 한다.
     * ReentrantLock으로 한 번에 하나의 TX만 전송 → Nonce 충돌 방지
     *
     * 사용자 지갑 TX(approve 등)는 지갑별로 별도 RawTransactionManager를 생성하므로 락 불필요.
     */
    public <T> T executePlatformTx(Callable<T> txCall) throws Exception {
        nonceLock.lock();
        try {
            return txCall.call();
        } finally {
            nonceLock.unlock();
        }
    }

    /**
     * SSF 잔액 온체인 조회 — balanceOf(address)
     */
    public BigInteger getSsfBalance(String walletAddress) {
        try {
            Function function = new Function("balanceOf", Arrays.asList(new Address(walletAddress)),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
            String encodedFunction = FunctionEncoder.encode(function);
            EthCall response = web3j
                .ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(platformAddress,
                    ssfContractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                .send();
            List<Type> results = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (results.isEmpty()) {
                return BigInteger.ZERO;
            }
            return (BigInteger) results.get(0).getValue();
        } catch (Exception e) {
            throw new BlockchainException("SSF 잔액 조회 실패: " + walletAddress + " — " + e.getMessage());
        }
    }
}
