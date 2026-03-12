package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;

import java.util.List;

public interface HostShowService {
    List<GetTicketEffectsResponse> getTicketEffects();
}
