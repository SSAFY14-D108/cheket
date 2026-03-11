package com.ssafy.cheket.dto.show.response;

import java.util.List;

public record GetShowListResponse<T>(List<T> shows, int page, int size, long totalElements, int totalPages) {
}
