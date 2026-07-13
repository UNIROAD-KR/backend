package com.uniroad.backend.domain.useditem.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UsedItemPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String semester;

    private String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;

    @Builder.Default
    @OneToMany(mappedBy = "usedItemPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeCategoryImage> images = new ArrayList<>();

    private String thumbnailImageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeItem> items = new ArrayList<>();

    public void update(String title, String content, Long price, String region, String semester, String country, String thumbnailImageUrl) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.region = region;
        this.semester = semester;
        this.country = country;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public void addImage(TradeCategoryImage image) {
        images.add(image);
        image.setUsedItemPost(this);
    }

    public void addItem(TradeItem item) {
        items.add(item);
        item.setPost(this);
    }


}
