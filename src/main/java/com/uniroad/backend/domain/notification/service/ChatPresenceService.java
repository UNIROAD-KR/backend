package com.uniroad.backend.domain.notification.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ChatPresenceService {
    private final ConcurrentMap<String, Long> sessionMembers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, Long>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<String>> memberSessions = new ConcurrentHashMap<>();

    public void connect(String sessionId, Long memberId) {
        sessionMembers.put(sessionId, memberId);
        memberSessions.computeIfAbsent(memberId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void subscribeRoom(String sessionId, String subscriptionId, Long roomId) {
        sessionSubscriptions
                .computeIfAbsent(sessionId, key -> new ConcurrentHashMap<>())
                .put(subscriptionId, roomId);
    }

    public void unsubscribe(String sessionId, String subscriptionId) {
        Optional.ofNullable(sessionSubscriptions.get(sessionId))
                .ifPresent(subscriptions -> subscriptions.remove(subscriptionId));
    }

    public void disconnect(String sessionId) {
        Long memberId = sessionMembers.remove(sessionId);
        sessionSubscriptions.remove(sessionId);
        if (memberId == null) {
            return;
        }

        Set<String> sessions = memberSessions.get(memberId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                memberSessions.remove(memberId);
            }
        }
    }

    public boolean isAppActive(Long memberId) {
        return memberSessions.containsKey(memberId);
    }

    public boolean isViewingRoom(Long memberId, Long roomId) {
        Set<String> sessions = memberSessions.get(memberId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        return sessions.stream()
                .map(sessionSubscriptions::get)
                .filter(subscriptions -> subscriptions != null)
                .map(Map::values)
                .anyMatch(roomIds -> roomIds.contains(roomId));
    }
}
