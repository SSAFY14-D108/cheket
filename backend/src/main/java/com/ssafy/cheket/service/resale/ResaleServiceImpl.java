package com.ssafy.cheket.service.resale;

import com.ssafy.cheket.dto.resale.response.GetResaleTicketsResponse;
import com.ssafy.cheket.dto.resale.response.ResaleShowItem;
import com.ssafy.cheket.dto.resale.response.ResaleTicketItem;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.entity.resale.Resale;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.enums.ResaleShowSort;
import com.ssafy.cheket.enums.ResaleStatus;
import com.ssafy.cheket.enums.ResaleTicketSort;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.resale.ResaleEntityRepository;
import com.ssafy.cheket.repository.resale.ResaleRepository;
import com.ssafy.cheket.repository.resale.projection.ResaleShowProjection;
import com.ssafy.cheket.repository.resale.projection.ResaleTicketProjection;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import com.ssafy.cheket.service.blockchain.BlockchainAsyncWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResaleServiceImpl implements ResaleService {
    private final ResaleRepository resaleRepository;
    private final ResaleEntityRepository resaleEntityRepository;
    private final TicketRepository ticketRepository;
    private final TransactionRepository transactionRepository;
    private final BlockchainAsyncWorker blockchainAsyncWorker;

    // 2차 거래 티켓이 존재하는 공연 목록 조회
    @Override
    public GetShowListResponse<ResaleShowItem> getResaleShowList(List<Integer> regions, ResaleShowSort sort,
        String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        List<Integer> normalizedRegions = (regions == null) ? List.of() : regions;
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        ResaleShowSort normalizedSort = (sort == null) ? ResaleShowSort.POPULAR : sort;

        Page<ResaleShowProjection> result = switch (normalizedSort) {
            case DEADLINE -> resaleRepository.searchListedShowsOrderByDeadline(normalizedRegions,
                (long) normalizedRegions.size(), normalized, pageable);
            case POPULAR -> resaleRepository.searchListedShowsOrderByPopular(normalizedRegions,
                (long) normalizedRegions.size(), normalized, LocalDateTime.now().minusDays(7), pageable);
        };

        List<ResaleShowItem> items = result.getContent().stream().map(this::toResaleShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    // 특정 공연의 2차거래 티켓 목록 조회
    @Override
    public GetResaleTicketsResponse getResaleTickets(Long showId, ResaleTicketSort sort, Long sessionId, int page,
        int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        ResaleTicketSort normalizedSort = (sort == null) ? ResaleTicketSort.LATEST : sort;

        Page<ResaleTicketProjection> result = switch (normalizedSort) {
            case LATEST -> resaleRepository.findListedTicketsByShowOrderByLatest(showId, sessionId, pageable);
            case PRICE -> resaleRepository.findListedTicketsByShowOrderByPrice(showId, sessionId, pageable);
        };

        List<ResaleTicketItem> items = result.getContent().stream().map(this::toResaleTicketItem).toList();

        return new GetResaleTicketsResponse(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    private ResaleTicketItem toResaleTicketItem(ResaleTicketProjection p) {
        return new ResaleTicketItem(p.getTicketId(),
            new ResaleTicketItem.SessionInfo(p.getSessionId(), p.getSessionDate().toLocalDate(),
                p.getSessionStartTime().toLocalTime()),
            p.getSectionName(), p.getSeatId(), p.getSeatNo(), p.getGrade(), p.getOriginalPrice(),
            p.getDiscountedPrice(), p.getDiscountRate());
    }

    private ResaleShowItem toResaleShowItem(ResaleShowProjection p) {
        return new ResaleShowItem(p.getShowId(), p.getTitle(), p.getShowStartDate().toLocalDate(),
            p.getShowEndDate().toLocalDate(), p.getVenue(), p.getRegion(), p.getPosterUrl(), p.getTicketCount());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    @Transactional
    public Long cancelResale(Long userId, Long ticketId) {
        log.info("[리세일 취소] 요청 — userId={}, ticketId={}", userId, ticketId);

        // ① 티켓 소유자 확인
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("존재하지 않는 티켓입니다."));

        if (!ticket.getUserId().equals(userId)) {
            throw new ForbiddenException("본인 소유 티켓만 취소할 수 있습니다.");
        }

        if (ticket.getResaleStatus() != ResaleStatus.LISTED) {
            throw new ConflictException("리세일 등록 상태가 아닙니다: " + ticket.getResaleStatus());
        }

        // ② ACTIVE Resale 조회
        Resale resale = resaleEntityRepository.findByTicketIdAndStatus(ticketId, Resale.ResaleListingStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException("활성 리세일 등록을 찾을 수 없습니다."));

        if (resale.getOnChainListingId() == null) {
            throw new ConflictException("온체인 등록이 완료되지 않은 리세일입니다.");
        }

        // ③ Transaction PENDING 생성
        Transaction transaction = Transaction.builder().type(Transaction.TransactionType.PURCHASE).amount(0L)
            .description("리세일 취소 대기").txStatus(Transaction.TxStatus.PENDING).buyerId(userId).build();
        transactionRepository.save(transaction);

        log.info("[리세일 취소] PENDING 생성 — txId={}, onChainDealId={}", transaction.getId(), resale.getOnChainListingId());

        // ④ afterCommit → @Async
        Long txId = transaction.getId();
        Long resaleId = resale.getId();
        int onChainDealId = resale.getOnChainListingId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                blockchainAsyncWorker.processOnChainResaleCancel(txId, ticketId, resaleId, onChainDealId);
            }
        });

        return txId;
    }
}
