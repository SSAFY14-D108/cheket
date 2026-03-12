package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.entity.ticket.TicketEffect;
import com.ssafy.cheket.repository.ticket.TicketEffectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostShowServiceImpl implements HostShowService {
    private final TicketEffectRepository ticketEffectRepository;

    @Override
    public List<GetTicketEffectsResponse> getTicketEffects() {
        List<TicketEffect> effects = ticketEffectRepository.findAll();

        return effects.stream().map(effect -> new GetTicketEffectsResponse(effect.getId(), effect.getEffect()))
            .toList();
    }
}
