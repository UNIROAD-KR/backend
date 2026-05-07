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
public class UsedItem extends BaseTimeEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;

    @Builder.Default
    @OneToMany(mappedBy = "usedItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsedItemImage> images = new ArrayList<>();

    public void addImage(UsedItemImage image) {
        images.add(image);
        image.setUsedItem(this);
    }

    public void update(String title, String content, Long price, String region, String semester) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.region = region;
        this.semester = semester;
    }
}
