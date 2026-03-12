package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.service.host.HostShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hosts/shows")
public class HostShowController {
    private final HostShowService hostShowService;

    @GetMapping("/effect")
    @Operation(summary = "티켓 효과 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetTicketEffectsResponse>>> getTicketEffects() {
        List<GetTicketEffectsResponse> response = hostShowService.getTicketEffects();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "티켓 효과 목록 조회 완료", response));
    }
}
