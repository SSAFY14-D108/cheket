package com.ssafy.cheket.entity.show;

import com.ssafy.cheket.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "search_histories")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static SearchHistory create(User user, String keyword) {
        return SearchHistory.builder().user(user).keyword(keyword).createdAt(LocalDateTime.now()).build();
    }
}
