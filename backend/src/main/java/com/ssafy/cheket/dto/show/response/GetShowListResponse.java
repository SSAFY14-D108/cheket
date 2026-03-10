package com.ssafy.cheket.dto.show.response;

import java.util.List;

public record GetShowListResponse(List<ShowItem> shows, int page, int size, long totalElements, int totalPages) {
}
