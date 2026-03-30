package com.ssafy.cheket.entity.show;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "show_images")
public class ShowImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "show_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Show show;

    @Column(name = "image_url", nullable = false, length = 200)
    private String imageUrl;

    @Builder
    public ShowImage(Show show, String imageUrl) {
        this.show = show;
        this.imageUrl = imageUrl;
    }

    public static ShowImage of(Show show, String imageUrl) {
        return ShowImage.builder().show(show).imageUrl(imageUrl).build();
    }

}
