package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.GetBookingRateResponse;
import com.ssafy.cheket.dto.host.response.GetReservationsResponse;
import com.ssafy.cheket.dto.host.response.GetRevenueSplitOnchainResponse;
import com.ssafy.cheket.dto.host.response.GetRevenueSplitResponse;
import com.ssafy.cheket.dto.host.response.GetTotalSalesResponse;

public interface DashboardService {
    GetTotalSalesResponse getTotalSales(Long hostId, Long showId);

    GetBookingRateResponse getBookingRate(Long hostId, Long showId);

    GetRevenueSplitResponse getRevenueSplit(Long loginId, String role, Long showId);

    GetRevenueSplitOnchainResponse getRevenueSplitOnchain(Long loginId, String role, Long showId);

    GetReservationsResponse getReservations(Long hostId, Long showId);
}
