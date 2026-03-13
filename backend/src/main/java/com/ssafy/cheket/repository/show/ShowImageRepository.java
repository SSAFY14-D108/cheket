package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.ShowImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowImageRepository extends JpaRepository<ShowImage, Long> {

    // 공연 이미지 목록 가져오기
    List<ShowImage> findAllByShow_Id(Long showId);

}
