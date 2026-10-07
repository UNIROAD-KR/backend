package com.uniroad.backend.domain.community.freepost.entity;

public enum FreePostCategory {
    QUESTION("질문글"),
    CHAT("사담글"),
    WORRY("고민글");

    private final String description;

    FreePostCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
