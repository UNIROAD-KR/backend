package com.uniroad.backend.domain.member.entity;

public enum     Role {
    USER("ROLE_USER"),
    VERIFIED("ROLE_VERIFIED"),
    ADMIN("ROLE_ADMIN");

    private final String key;

    Role(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
