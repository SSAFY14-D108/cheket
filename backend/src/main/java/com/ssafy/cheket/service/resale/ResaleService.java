package com.ssafy.cheket.service.resale;

import com.ssafy.cheket.dto.resale.response.GetResaleTicketsResponse;
import com.ssafy.cheket.dto.resale.response.ResaleShowItem;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.enums.ResaleShowSort;
import com.ssafy.cheket.enums.ResaleTicketSort;

import java.util.List;

public interface ResaleService {
    GetShowListResponse<ResaleShowItem> getResaleShowList(List<Integer> regions, ResaleShowSort sort, String keyword,
        int page, int size);

    GetResaleTicketsResponse getResaleTickets(Long showId, ResaleTicketSort sort, Long sessionId, int page, int size);

    // 리세일 취소
    Long cancelResale(Long userId, Long ticketId);

    // 리세일 구매
    Long purchaseResale(Long userId, Long ticketId);
}
