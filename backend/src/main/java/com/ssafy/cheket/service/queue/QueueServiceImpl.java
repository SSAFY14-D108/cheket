package com.ssafy.cheket.service.queue;

import com.ssafy.cheket.config.queue.QueueConstants;
import com.ssafy.cheket.config.queue.QueueTokenGenerator;
import com.ssafy.cheket.dto.queue.QueueTokenMeta;
import com.ssafy.cheket.dto.queue.response.QueueEnterResponse;
import com.ssafy.cheket.dto.queue.response.QueueSeatEnterResponse;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.QueueStatus;
import com.ssafy.cheket.exception.common.*;
import com.ssafy.cheket.repository.queue.QueueRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.ssafy.cheket.config.queue.QueueConstants.ACTIVE_LIMIT;
import static com.ssafy.cheket.config.queue.QueueConstants.ACTIVE_TTL_SECONDS;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRepository queueRepository;
    private final ShowRepository showRepository;
    private final SessionRepository sessionRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 대기열 진입
    @Transactional
    @Override
    public QueueEnterResponse enterQueue(Long userId, Long showId, Long sessionId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연 또는 회차입니다."));
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 공연 또는 회차입니다."));
        if (!session.getShowId().equals(showId)) {
            throw new NotFoundException("존재하지 않는 공연 또는 회차입니다.");
        }

        LocalDateTime now = LocalDateTime.now(KST);
        if (now.isBefore(show.getReservationStartDate())) {
            throw new BadRequestException("아직 예매 가능한 시간이 아닙니다.");
        }

        Long nowEpochSec = Instant.now().getEpochSecond();

        if (queueRepository.hasQueueToken(userId, sessionId)) {
            String existingQueueToken = queueRepository.getQueueToken(userId, sessionId);

            QueueEnterResponse existingResponse = handleExistingQueueToken(userId, showId, sessionId,
                existingQueueToken, nowEpochSec);

            if (existingResponse != null) {
                return existingResponse;
            }
        }

        String queueToken = QueueTokenGenerator.generateQueueToken();

        Long joinSeq = queueRepository.incrementSequence(sessionId);
        queueRepository.addWaiting(sessionId, queueToken, joinSeq);

        QueueTokenMeta queueTokenMeta = QueueTokenMeta.builder().userId(userId).showId(showId).sessionId(sessionId)
            .status(QueueStatus.WAITING).joinSeq(joinSeq).joinedAt(nowEpochSec).admitExpiresAt(null).enteredAt(null)
            .leftAt(null).build();

        queueRepository.saveQueueMeta(queueToken, queueTokenMeta);

        queueRepository.saveUserQueueToken(userId, sessionId, queueToken);
        queueRepository.addActiveSession(sessionId);
        promoteWaitingToActive(showId, sessionId, nowEpochSec);

        return buildQueueEnterResponse(queueToken, sessionId);
    }

    private QueueEnterResponse handleExistingQueueToken(Long userId, Long showId, Long sessionId, String queueToken,
        Long nowEpochSec) {
        QueueTokenMeta queueTokenMeta = queueRepository.findQueueTokenMeta(queueToken);

        if (queueTokenMeta == null) {
            clearUserQueueMapping(userId, sessionId);
            return null;
        }

        if (!queueTokenMeta.getUserId().equals(userId) || !queueTokenMeta.getShowId().equals(showId)
            || !queueTokenMeta.getSessionId().equals(sessionId)) {
            cleanupQueueToken(userId, showId, sessionId, queueToken, queueTokenMeta.getStatus(), false);
            return null;
        }

        QueueStatus status = queueTokenMeta.getStatus();

        if (status == QueueStatus.WAITING) {
            Long rank = queueRepository.getWaitingRank(sessionId, queueToken);

            if (rank == null) {
                cleanupQueueToken(userId, showId, sessionId, queueToken, QueueStatus.WAITING, true);
                return null;
            }

            return buildQueueEnterResponse(queueToken, sessionId);
        }

        if (status == QueueStatus.ACTIVE) {
            Long admitExpiresAt = queueTokenMeta.getAdmitExpiresAt();

            if (admitExpiresAt == null || nowEpochSec > admitExpiresAt) {
                // ACTIVE 만료 처리
                queueRepository.updateStatus(queueToken, QueueStatus.EXPIRED);
                cleanupQueueToken(userId, showId, sessionId, queueToken, QueueStatus.EXPIRED, true);
                promoteWaitingToActive(showId, sessionId, nowEpochSec);
                return null;
            }

            return buildQueueEnterResponse(queueToken, sessionId);
        }

        if (status == QueueStatus.EXPIRED || status == QueueStatus.LEFT) {
            cleanupQueueToken(userId, showId, sessionId, queueToken, status, true);
            promoteWaitingToActive(showId, sessionId, nowEpochSec);
            return null;
        }

        if (status == QueueStatus.COMPLETED) {
            throw new BadRequestException("진입할 수 없는 대기열입니다.");
        }

        cleanupQueueToken(userId, showId, sessionId, queueToken, status, true);
        return null;
    }

    private void promoteWaitingToActive(Long showId, Long sessionId, Long nowEpochSec) {
        Long activeCount = queueRepository.getActiveCount(sessionId);
        if (activeCount >= QueueConstants.ACTIVE_LIMIT)
            return;

        Long available = QueueConstants.ACTIVE_LIMIT - activeCount;

        for (long i = 0; i < available; i++) {
            String nextQueueToken = queueRepository.pollNextWaitingToken(sessionId);
            if (nextQueueToken == null)
                return;

            QueueTokenMeta queueTokenMeta = queueRepository.findQueueTokenMeta(nextQueueToken);
            if (queueTokenMeta == null)
                continue;

            Long nextUserId = queueTokenMeta.getUserId();
            if (nextUserId == null) {
                queueRepository.deleteQueueTokenMeta(nextQueueToken);
                continue;
            }

            Long admitExpiresAt = nowEpochSec + QueueConstants.ACTIVE_TTL_SECONDS;

            queueRepository.addToActiveSet(sessionId, nextQueueToken, admitExpiresAt);
            queueRepository.updateStatus(nextQueueToken, QueueStatus.ACTIVE);
            queueRepository.updateAdmitExpiresAt(nextQueueToken, admitExpiresAt);
            queueRepository.saveActiveUserKey(showId, sessionId, nextUserId, QueueConstants.ACTIVE_TTL_SECONDS);
        }
    }

    private QueueEnterResponse buildQueueEnterResponse(String queueToken, Long sessionId) {
        QueueStatus status = queueRepository.getQueueStatus(queueToken);
        if (status == null) {
            throw new NotFoundException("대기열 정보를 찾을 수 없습니다.");
        }

        if (status == QueueStatus.ACTIVE) {
            Long admitExpiresAtEpochSec = queueRepository.getAdmitExpiresAt(queueToken);

            return QueueEnterResponse.builder().queueToken(queueToken).status(QueueStatus.ACTIVE).position(0L)
                .aheadCount(0L).estimatedWaitSeconds(0L).pollingIntervalSeconds(QueueConstants.POLLING_INTERVAL_SECONDS)
                .admitExpiresAt(toLocalDatTime(admitExpiresAtEpochSec)).build();
        }

        if (status == QueueStatus.WAITING) {
            Long rank = queueRepository.getWaitingRank(sessionId, queueToken);
            if (rank == null) {
                throw new NotFoundException("대기열 정보를 찾을 수 없습니다.");
            }

            long aheadCount = rank;
            long position = rank + 1;
            long estimatedWaitSeconds = aheadCount * QueueConstants.ESTIMATED_WAIT_PER_PERSON_SECONDS;

            return QueueEnterResponse.builder().queueToken(queueToken).status(QueueStatus.WAITING).position(position)
                .aheadCount(aheadCount).estimatedWaitSeconds(estimatedWaitSeconds)
                .pollingIntervalSeconds(QueueConstants.POLLING_INTERVAL_SECONDS).admitExpiresAt(null).build();
        }

        throw new BadRequestException("유효하지 않은 대기열 상태입니다.");
    }

    private void cleanupQueueToken(Long userId, Long showId, Long sessionId, String queueToken, QueueStatus status,
        boolean removeMeta) {
        if (status == QueueStatus.WAITING) {
            queueRepository.removeFromWaitingQueue(sessionId, queueToken);
        }

        if (status == QueueStatus.ACTIVE || status == QueueStatus.EXPIRED
            || queueRepository.isActiveMember(sessionId, queueToken)) {
            queueRepository.removeFromActiveSet(sessionId, queueToken);
            queueRepository.deleteActiveUserKey(showId, sessionId, userId);
        }

        queueRepository.deleteUserTokenMapping(userId, sessionId);

        if (removeMeta) {
            queueRepository.deleteQueueTokenMeta(queueToken);
        }
    }

    private void clearUserQueueMapping(Long userId, Long sessionId) {
        queueRepository.deleteUserTokenMapping(userId, sessionId);
    }

    private LocalDateTime toLocalDatTime(Long epochSec) {
        if (epochSec == null)
            return null;

        return Instant.ofEpochSecond(epochSec).atZone(KST).toLocalDateTime();
    }

    // 대기열 이탈
    @Transactional
    @Override
    public void leaveQueue(Long userId, Long showId, Long sessionId, String queueToken) {
        QueueTokenMeta meta = queueRepository.findQueueTokenMeta(queueToken);

        validateQueueToken(meta, userId, showId, sessionId);

        QueueStatus status = meta.getStatus();

        if (status == QueueStatus.LEFT || status == QueueStatus.EXPIRED)
            return;

        if (status == QueueStatus.WAITING) { // WAITING 상태면 대기열에서 제거
            queueRepository.removeFromWaitingQueue(sessionId, queueToken);
        } else if (status == QueueStatus.ACTIVE) { // ACTIVE 상태면 활성 사용자 key와 active set에서 제거
            queueRepository.deleteActiveUserKey(showId, sessionId, userId);
            queueRepository.removeFromActiveSet(sessionId, queueToken);
        } else if (status == QueueStatus.COMPLETED) { // 이미 좌석 선택 페이지에 진입한 사용자는 이탈 불가
            throw new ConflictException("이미 좌석 선택 페이지에 진입한 사용자는 대기열 이탈 대상이 아닙니다.");
        }

        // 토큰 상태를 LEFT로 바꾸고 이탈 시각 기록
        queueRepository.updateStatus(queueToken, QueueStatus.LEFT);
        queueRepository.updateLeftAt(queueToken, currentEpochSec());

        // 사용자-토큰 매핑 삭제
        queueRepository.deleteUserTokenMapping(sessionId, userId);

        // 빈 ACTIVE 자리가 생겼다면 WAITING 사용자를 승급
        promoteWaitingToActive(sessionId);
    }

    // 좌석 선택 진입
    @Transactional
    @Override
    public QueueSeatEnterResponse enterSeatSelection(Long userId, Long showId, Long sessionId, String queueToken) {
        QueueTokenMeta meta = queueRepository.findQueueTokenMeta(queueToken);
        validateQueueToken(meta, userId, showId, sessionId);
        QueueStatus status = meta.getStatus();

        if (status == QueueStatus.WAITING)
            throw new ForbiddenException("아직 입장 차례가 아닙니다.");
        if (status == QueueStatus.EXPIRED)
            throw new GoneException("입장 가능 시간이 만료되었습니다.");
        if (status == QueueStatus.LEFT)
            throw new ForbiddenException("이미 이탈한 대기열입니다.");
        if (status == QueueStatus.COMPLETED)
            throw new ConflictException("이미 좌석 선택 화면에 진입했습니다.");
        if (status != QueueStatus.ACTIVE)
            throw new ForbiddenException("진입할 수 없는 대기열 싱태입니다.");

        long nowEpochSec = currentEpochSec();
        Long admitExpiresAt = meta.getAdmitExpiresAt();

        if (admitExpiresAt == null || nowEpochSec >= admitExpiresAt) {
            queueRepository.updateStatus(queueToken, QueueStatus.EXPIRED);
            queueRepository.removeFromActiveSet(sessionId, queueToken);
            queueRepository.deleteActiveUserKey(showId, sessionId, userId);
            queueRepository.deleteUserTokenMapping(sessionId, userId);

            promoteWaitingToActive(sessionId);

            throw new GoneException("입장 가능 시간이 만료되었습니다.");
        }

        if (!queueRepository.existsActiveUserKey(showId, sessionId, userId)
            || !queueRepository.isActiveMember(sessionId, queueToken))
            throw new GoneException("입장 권한이 만료되었거나 이미 사용되었습니다.");

        String seatAccessToken = QueueTokenGenerator.generateSeatAccessToken();
        queueRepository.saveSeatAccessToken(seatAccessToken, userId, showId, sessionId,
            QueueConstants.SEAT_ACCESS_TTL_SECONDS);

        queueRepository.updateStatus(queueToken, QueueStatus.COMPLETED);
        queueRepository.updateEnteredAt(queueToken, nowEpochSec);

        queueRepository.removeFromActiveSet(sessionId, queueToken);
        queueRepository.deleteActiveUserKey(showId, sessionId, userId);
        queueRepository.deleteUserTokenMapping(sessionId, userId);

        promoteWaitingToActive(sessionId);

        return QueueSeatEnterResponse.builder().seatAccessToken(seatAccessToken)
            .seatAccessExpiresAt(toLocalDatTime(nowEpochSec + QueueConstants.SEAT_ACCESS_TTL_SECONDS)).build();
    }

    // 대기열 승격
    private void promoteWaitingToActive(Long sessionId) {
        long currentActiveCount = queueRepository.getActiveCount(sessionId);
        long availableSlots = ACTIVE_LIMIT - currentActiveCount;

        if (availableSlots <= 0)
            return;

        // 비어 있는 자리 수만큼 WAITING -> ACTIVE
        for (int i = 0; i < availableSlots; i++) {
            String nextQueueToken = queueRepository.pollNextWaitingToken(sessionId);

            if (nextQueueToken == null)
                return;

            QueueTokenMeta meta = queueRepository.findQueueTokenMeta(nextQueueToken);
            if (meta == null)
                continue;

            if (meta.getStatus() != QueueStatus.WAITING)
                continue;

            long admitExpiresAt = currentEpochSec() + ACTIVE_TTL_SECONDS;

            queueRepository.updateStatus(nextQueueToken, QueueStatus.ACTIVE);
            queueRepository.updateAdmitExpiresAt(nextQueueToken, admitExpiresAt);
            queueRepository.saveActiveUserKey(meta.getShowId(), meta.getSessionId(), meta.getUserId(),
                ACTIVE_TTL_SECONDS);
            queueRepository.addToActiveSet(meta.getSessionId(), nextQueueToken, admitExpiresAt);
        }
    }

    // queueToken 검증
    private void validateQueueToken(QueueTokenMeta meta, Long userId, Long showId, Long sessionId) {
        if (meta == null)
            throw new NotFoundException("유효하지 않은 queueToken 입니다.");

        if (!meta.getUserId().equals(userId))
            throw new ForbiddenException("queueToken 소유자와 요청 사용자가 일치하지 않습니다.");

        if (!meta.getShowId().equals(showId) || !meta.getSessionId().equals(sessionId))
            throw new ConflictException("요청 정보와 queueToken 정보가 일치하지 않습니다.");
    }

    private long currentEpochSec() {
        return System.currentTimeMillis() / 1000;
    }

}
