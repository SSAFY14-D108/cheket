package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.GetHostShowDetailResponse;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.ShowItem;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.entity.ticket.TicketEffect;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.*;
import com.ssafy.cheket.repository.ticket.TicketEffectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostShowServiceImpl implements HostShowService {
    private final TicketEffectRepository ticketEffectRepository;
    private final ShowRepository showRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final StakeholderRepository stakeholderRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final SessionRepository sessionRepository;
    private final LikeRepository likeRepository;
    private final SessionSeatRepository sessionSeatRepository;

    // 티켓 효과 목록 조회
    @Override
    public List<GetTicketEffectsResponse> getTicketEffects() {
        List<TicketEffect> effects = ticketEffectRepository.findAll();

        return effects.stream().map(effect -> new GetTicketEffectsResponse(effect.getId(), effect.getEffect()))
            .toList();
    }

    // 내 공연 목록 조회
    @Override
    public GetShowListResponse<ShowItem> getMyShows(Long hostId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        Page<Show> result = showRepository.findByHost_id(hostId, pageable);
        List<ShowItem> items = result.getContent().stream().map(this::toShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    // 공연 상세 조회
    @Override
    public GetHostShowDetailResponse getHostShowDetail(Long hostId, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 조회할 수 있습니다.");

        List<SeatGrade> seatGrades = seatGradeRepository.findByShowId(showId);
        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(showId);
        List<RefundPolicy> refundPolicies = refundPolicyRepository.findByShowIdOrderByDaysRemainingDesc(showId);
        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);

        List<Long> sessionIds = sessions.stream().map(Session::getId).toList();

        Map<Long, Integer> capacityMap = sessionSeatRepository.countGroupedBySessionIds(sessionIds).stream()
            .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Long) row[1]).intValue()));

        return new GetHostShowDetailResponse(show.getId(), show.getTitle(), show.getPosterUrl(),
            new GetHostShowDetailResponse.VenueInfo(show.getVenue().getId(), show.getVenue().getName(),
                show.getVenue().getAddress()),
            new GetHostShowDetailResponse.ShowPeriod(show.getShowStartDate().toLocalDate(),
                show.getShowEndDate().toLocalDate()),
            new GetHostShowDetailResponse.ReservationPeriod(show.getReservationStartDate(),
                show.getReservationEndDate()),
            show.getDescription(), show.getPurchaseLimit(), likeRepository.countByShowId(showId),
            seatGrades.stream()
                .map(grade -> new GetHostShowDetailResponse.GradeInfo(grade.getSectionId(), grade.getGradeName(),
                    grade.getPrice(), grade.getColorCode()))
                .toList(),
            stakeholders.stream()
                .map(stakeholder -> new GetHostShowDetailResponse.StakeholderInfo(stakeholder.getRole(),
                    stakeholder.getUserId(), stakeholder.getShareBps()))
                .toList(),
            refundPolicies.stream()
                .map(policy -> new GetHostShowDetailResponse.RefundPolicyInfo(policy.getDaysRemaining(),
                    policy.getRefundRate()))
                .toList(),
            sessions.stream()
                .map(session -> new GetHostShowDetailResponse.SessionInfo(session.getId(),
                    session.getSessionDate().toLocalDate(), session.getSessionStartTime().toLocalTime(),
                    capacityMap.getOrDefault(session.getId(), 0)))
                .toList(),
            show.getStatus(), show.getCreatedAt(), show.getUpdatedAt());
    }

    private ShowItem toShowItem(Show s) {
        return new ShowItem(s.getId(), s.getTitle(), s.getPosterUrl(), s.getVenue().getName(), s.getPurchaseLimit(),
            s.getVenue().getRegion().getName(),
            new ShowItem.ShowPeriod(s.getShowStartDate().toLocalDate(), s.getShowEndDate().toLocalDate()),
            new ShowItem.ReservationPeriod(s.getReservationStartDate(), s.getReservationEndDate()),
            s.getStatus().name());
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
