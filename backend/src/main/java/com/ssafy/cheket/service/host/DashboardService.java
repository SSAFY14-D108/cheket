package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.GetTotalSales;

public interface DashboardService {
    GetTotalSales getTotalSales(Long hostId, Long showId);
}
