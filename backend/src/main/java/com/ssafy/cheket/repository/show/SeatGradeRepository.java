package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.SeatGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {

    List<SeatGrade> findByShowId(Long showId);

    @Modifying
    @Query("DELETE FROM SeatGrade sg WHERE sg.showId = :showId")
    void deleteAllByShowId(Long showId);
}
