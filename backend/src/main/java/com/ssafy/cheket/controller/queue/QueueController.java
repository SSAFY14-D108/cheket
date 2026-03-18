package com.ssafy.cheket.controller.queue;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.queue.response.QueueEnterResponse;
import com.ssafy.cheket.service.queue.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows/{showId}/sessions/{sessionId}/queue")
public class QueueController {
    private final QueueService queueService;

    @PostMapping
    @Operation(summary = "대기열 입장")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<QueueEnterResponse>> enterQueue(@AuthenticationPrincipal Long userId,
        @PathVariable Long showId, @PathVariable Long sessionId) {
        QueueEnterResponse response = queueService.enterQueue(userId, showId, sessionId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "대기열 진입에 성공했습니다.", response));
    }

}
