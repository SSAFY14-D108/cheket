package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}
