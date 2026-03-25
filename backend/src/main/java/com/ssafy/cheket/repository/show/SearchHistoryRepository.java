package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findTop10ByUser_IdOrderByCreatedAtDesc(Long userId);
}
