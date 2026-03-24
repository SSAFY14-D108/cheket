package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.blockchain.contract.TicketNFT;
import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.ticket.response.CheckInResponse;
import com.ssafy.cheket.dto.ticket.response.QrTokenResponse;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.redis.QrTokenRedisRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.ticket.projection.CheckInTicketProjection;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.blockchain.BlockchainService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrTokenServiceImpl implements QrTokenService {

    // QR 토큰 만료 시간 (30초) — 캡처/공유 방지를 위해 짧게 설정
    private static final int QR_TOKEN_EXPIRY_SECONDS = 30;

    private final JwtTokenProvider jwtTokenProvider;
    private final QrTokenRedisRepository qrTokenRedisRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BlockchainService blockchainService;

    // ===== QR 토큰 발급 (사용자 앱용) =====

    @Override
    public QrTokenResponse generateQrToken(Long userId, Long ticketId) {

        // 1. 티켓 존재 여부 확인
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new NotFoundException("티켓을 찾을 수 없습니다."));

        // 2. 본인 티켓인지 확인 — 타인의 티켓으로 QR 발급 방지
        if (!ticket.getUserId().equals(userId)) {
            throw new ForbiddenException("본인의 티켓만 QR 코드를 발급할 수 있습니다.");
        }

        // 3. 이미 입장한 티켓인지 확인 — 중복 입장 방지
        if (ticket.getCheckedInAt() != null) {
            throw new BadRequestException("이미 입장 처리된 티켓입니다.");
        }

        // 4. QR 전용 JWT 생성 (ticketId + userId 포함, 30초 만료)
        String qrToken = jwtTokenProvider.generateQrToken(ticketId, userId);

        // 5. Redis에 저장 — 기존 토큰 덮어쓰기 (매번 새로 발급)
        //    TTL 30초 후 자동 삭제 → 검증 시 Redis에 없으면 만료 처리
        qrTokenRedisRepository.save(
            ticketId, qrToken, Duration.ofSeconds(QR_TOKEN_EXPIRY_SECONDS)
        );

        // 6. 응답 — 프론트에서 이 qrToken 문자열로 QR 코드 이미지 생성
        return new QrTokenResponse(qrToken, QR_TOKEN_EXPIRY_SECONDS);
    }

    // ===== QR 토큰 검증 + 온체인 체크인 (검증 앱용) =====

    @Override
    @Transactional
    public CheckInResponse verifyAndCheckIn(String qrToken) {

        // ── 1단계: JWT 서명 + 만료 검증 ──
        Claims claims;
        try {
            claims = jwtTokenProvider.parseQrToken(qrToken);
        } catch (ExpiredJwtException e) {
            throw new BadRequestException("QR 코드가 만료되었습니다. 재발급 받아주세요.");
        } catch (Exception e) {
            throw new BadRequestException("유효하지 않은 QR 코드입니다.");
        }

        // JWT에서 type 확인 — Access Token으로 위장 방지
        String type = claims.get("type", String.class);
        if (!"QR_TOKEN".equals(type)) {
            throw new BadRequestException("유효하지 않은 QR 코드입니다.");
        }

        // JWT에서 ticketId, userId 추출
        Long ticketId = Long.parseLong(claims.getSubject());
        Long userId = claims.get("userId", Long.class);

        // ── 2단계: Redis 일회성 확인 + 삭제 ──
        String savedToken = qrTokenRedisRepository.find(ticketId)
            .orElseThrow(() -> new BadRequestException("이미 사용되었거나 만료된 QR 코드입니다."));

        // Redis에 저장된 토큰과 일치하는지 확인
        if (!savedToken.equals(qrToken)) {
            throw new BadRequestException("유효하지 않은 QR 코드입니다.");
        }

        // 일회성 보장: 즉시 삭제 (같은 QR로 재사용 불가)
        qrTokenRedisRepository.delete(ticketId);

        // ── 3단계: DB에서 티켓 조회 ──
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new NotFoundException("티켓을 찾을 수 없습니다."));

        if (ticket.getCheckedInAt() != null) {
            throw new BadRequestException("이미 입장 처리된 티켓입니다.");
        }

        // ── 4단계: 온체인 NFT 소유권 검증 ──
        // DB가 아닌 블록체인에서 직접 확인 → 위변조 불가능
        verifyOnChainOwnership(ticket, userId);

        // ── 5단계: 온체인에서 티켓 정보 조회 (상태 + 좌석 정보 한 번에) ──
        TicketNFT.TicketInfo onChainTicket = getOnChainTicketInfo(ticket);

        // ── 6단계: 온체인 좌석 정보와 DB 좌석 정보 비교 ──
        CheckInTicketProjection dbInfo = ticketRepository.findCheckInInfoByTicketId(ticketId);
        verifyTicketInfoConsistency(onChainTicket, dbInfo);

        // ── 7단계: DB 체크인 기록 ──
        ticket.setCheckedInAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        // ── 8단계: 온체인 체크인 (VALID → USED) ──
        executeOnChainCheckIn(ticket);

        // ── 9단계: 응답 (온체인 좌석 정보 기반) ──
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        return new CheckInResponse(
            true,
            ticket.getId(),
            user.getUsername(),
            dbInfo != null ? dbInfo.getShowTitle() : null,
            onChainTicket.section,                                    // 온체인 구역
            onChainTicket.row + "열 " + onChainTicket.seat + "번",    // 온체인 열+좌석
            onChainTicket.grade,                                      // 온체인 등급
            ticket.getCheckedInAt().toString()
        );
    }

    // ===== private 메서드: 온체인 검증 =====

    /**
     * 블록체인에서 NFT 소유자 조회 → 사용자 지갑 주소와 비교
     * DB를 신뢰하지 않고 체인에서 직접 확인 → 블록체인의 핵심 가치
     */
    private void verifyOnChainOwnership(Ticket ticket, Long userId) {
        try {
            BigInteger tokenId = BigInteger.valueOf(ticket.getTicketNftId());

            // 블록체인에서 NFT 소유자 주소 조회 (view 함수, 가스비 0)
            String onChainOwner = blockchainService.getTicketNFT()
                .ownerOf(tokenId).send();

            // 사용자의 지갑 주소 조회 (DB → User → Wallet)
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
            Wallet wallet = walletRepository.findById(user.getWalletId())
                .orElseThrow(() -> new NotFoundException("지갑 정보를 찾을 수 없습니다."));

            // 온체인 소유자와 사용자 지갑 주소 비교 (대소문자 무시)
            if (!onChainOwner.equalsIgnoreCase(wallet.getAddress())) {
                throw new ForbiddenException("온체인 소유권 검증에 실패했습니다.");
            }

            log.info("온체인 소유권 검증 성공 — ticketNftId: {}, owner: {}", tokenId, onChainOwner);

        } catch (ForbiddenException e) {
            throw e; // 소유권 불일치는 그대로 전파
        } catch (Exception e) {
            log.error("온체인 소유권 검증 중 오류: {}", e.getMessage());
            throw new BadRequestException("블록체인 검증에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 온체인에서 티켓 전체 정보 조회 (상태 + 좌석 정보)
     * getTicket() 1번 호출로 status, section, row, seat, grade 모두 확인
     */
    private TicketNFT.TicketInfo getOnChainTicketInfo(Ticket ticket) {
        try {
            BigInteger tokenId = BigInteger.valueOf(ticket.getTicketNftId());
            TicketNFT.TicketInfo info = blockchainService.getTicketNFT()
                .getTicket(tokenId).send();

            // 상태 검증: 0 = VALID만 입장 가능
            if (info.status.intValue() != 0) {
                throw new BadRequestException(
                    "온체인 티켓 상태가 유효하지 않습니다. (상태: " + info.status + ")");
            }

            log.info("온체인 티켓 정보 조회 성공 — ticketNftId: {}, section: {}, row: {}, seat: {}",
                tokenId, info.section, info.row, info.seat);

            return info;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("온체인 티켓 정보 조회 중 오류: {}", e.getMessage());
            throw new BadRequestException("블록체인 검증에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 온체인 좌석 정보와 DB 좌석 정보 비교
     * 불일치 시 → DB 조작 가능성 → 입장 거부
     */
    private void verifyTicketInfoConsistency(TicketNFT.TicketInfo onChain, CheckInTicketProjection db) {
        if (db == null) {
            throw new NotFoundException("DB에서 좌석 정보를 찾을 수 없습니다.");
        }

        // 온체인 구역명과 DB 구역명 비교
        if (!onChain.section.equals(db.getSectionName())) {
            log.error("좌석 정보 불일치 — 온체인 section: {}, DB section: {}",
                onChain.section, db.getSectionName());
            throw new BadRequestException("온체인과 DB의 좌석 정보가 일치하지 않습니다.");
        }

        // 온체인 등급과 DB 등급 비교
        if (!onChain.grade.equals(db.getGrade())) {
            log.error("좌석 정보 불일치 — 온체인 grade: {}, DB grade: {}",
                onChain.grade, db.getGrade());
            throw new BadRequestException("온체인과 DB의 좌석 정보가 일치하지 않습니다.");
        }

        log.info("온체인-DB 좌석 정보 일치 확인 완료");
    }

    /**
     * 온체인 체크인 — TicketNFT.checkIn(tokenId) 호출
     * VALID → USED 상태 변경 (블록체인에 영구 기록)
     * 실패해도 DB 체크인은 이미 완료 → 로그만 남기고 진행
     * (공연 후 batchCheckIn으로 보정 가능)
     */
    private void executeOnChainCheckIn(Ticket ticket) {
        try {
            BigInteger tokenId = BigInteger.valueOf(ticket.getTicketNftId());

            // 온체인 checkIn TX 전송 (onlyOwner → 플랫폼 지갑으로 서명)
            blockchainService.getTicketNFT()
                .checkIn(tokenId).send();

            log.info("온체인 체크인 완료 — ticketNftId: {}", tokenId);

        } catch (Exception e) {
            // 온체인 실패해도 DB 체크인은 유지 (최종 일관성)
            // 공연 후 batchCheckIn으로 보정 가능
            log.error("온체인 체크인 실패 (DB 체크인은 유지) — ticketId: {}, error: {}",
                ticket.getId(), e.getMessage());
        }
    }
}
