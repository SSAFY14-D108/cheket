package com.ssafy.cheket.controller.show;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.service.show.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/shows/{showId}/likes")
    @Operation(summary = "공연 찜 추가")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> addLike(@PathVariable Long showId, @AuthenticationPrincipal Long userId) {

        likeService.addLike(userId, showId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "공연 찜 추가 완료", null));
    }
}
