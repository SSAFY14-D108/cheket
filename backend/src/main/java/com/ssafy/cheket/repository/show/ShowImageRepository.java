package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.ShowImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShowImageRepository extends JpaRepository<ShowImage, Long> {

    // 공연 이미지 조회
    List<ShowImage> findAllByShow_Id(Long showId);

    // 공연 이미지 삭제
    @Modifying
    @Query("DELETE FROM ShowImage si WHERE si.show.id = :showId")
    void deleteAllByShowId(Long showId);

}
