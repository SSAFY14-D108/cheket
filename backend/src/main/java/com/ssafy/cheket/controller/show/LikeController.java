package com.ssafy.cheket.controller.show;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.show.response.GetLikesResponse;
import com.ssafy.cheket.service.show.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/shows/{showId}/likes")
    @Operation(summary = "공연 찜 삭제")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> removeLike(@PathVariable Long showId,
        @AuthenticationPrincipal Long userId) {
        likeService.removeLike(userId, showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 찜 삭제 완료", null));
    }

    @GetMapping("/users/likes")
    @Operation(summary = "찜 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetLikesResponse>>> getLikes(@AuthenticationPrincipal Long userId) {
        List<GetLikesResponse> response = likeService.getLikes(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "찜 목록 조회 완료", response));
    }

}
