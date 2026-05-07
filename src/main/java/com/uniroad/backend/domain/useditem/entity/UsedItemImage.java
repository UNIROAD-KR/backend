package com.uniroad.backend.domain.useditem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UsedItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_item_id")
    private UsedItem usedItem;

    public void setUsedItem(UsedItem usedItem) {
        this.usedItem = usedItem;
    }
}
