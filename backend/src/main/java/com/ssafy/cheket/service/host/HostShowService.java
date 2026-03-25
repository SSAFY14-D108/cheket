package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.CreateShowResponse;
import com.ssafy.cheket.dto.host.response.GetHostShowDetailResponse;
import com.ssafy.cheket.dto.settlement.response.GetApprovalListResponse;
import com.ssafy.cheket.dto.show.request.AddShowRequest;
import com.ssafy.cheket.dto.show.request.UpdateShowRequest;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.ShowItem;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HostShowService {
    List<GetTicketEffectsResponse> getTicketEffects();

    GetShowListResponse<ShowItem> getMyShows(Long hostId, int page, int size);

    GetHostShowDetailResponse getHostShowDetail(Long hostId, Long showId);

    CreateShowResponse createShow(Long hostId, AddShowRequest request, MultipartFile posterImage,
        List<MultipartFile> descriptionImages);

    void deleteShow(Long hostId, Long showId);

    void updateShow(Long hostId, Long showId, UpdateShowRequest request, MultipartFile posterImage,
        List<MultipartFile> descriptionImages);

    List<GetApprovalListResponse> getContracts(Long hostId, Long showId);

    Long confirmShow(Long hostId, Long showId);
}
