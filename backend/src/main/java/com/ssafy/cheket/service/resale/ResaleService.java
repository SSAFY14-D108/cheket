package com.ssafy.cheket.service.resale;

import com.ssafy.cheket.dto.resale.response.ResaleShowItem;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ResaleShowSort;

public interface ResaleService {
    GetShowListResponse<ResaleShowItem> getResaleShowList(Region region, ResaleShowSort sort, String keyword, int page,
        int size);
}
