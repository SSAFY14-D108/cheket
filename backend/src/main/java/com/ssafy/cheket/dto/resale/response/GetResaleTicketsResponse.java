package com.ssafy.cheket.dto.resale.response;

import java.util.List;

public record GetResaleTicketsResponse(List<ResaleTicketItem> tickets, int page, int size, long totalElements,
    int totalPages) {
}
