package com.ssafy.cheket.controller.show;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.show.response.GetShowDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shows")
public class ShowController {

    @GetMapping
    @Operation(summary = "공연 상세 조회")
    public ResponseEntity<ApiResponse<GetShowDetailResponse>> getShowDetail() {
        GetShowDetailResponse response = new GetShowDetailResponse();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 상세 조회 성공", response));
    }
}
