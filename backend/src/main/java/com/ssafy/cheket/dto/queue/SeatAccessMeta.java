package com.ssafy.cheket.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatAccessMeta {
    private Long userId;
    private Long showId;
    private Long sessionId;
}
