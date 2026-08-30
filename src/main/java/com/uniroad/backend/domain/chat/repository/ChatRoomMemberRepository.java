package com.uniroad.backend.domain.chat.repository;

import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByMember(Member member);
    List<ChatRoomMember> findByMemberAndLeftAtIsNull(Member member);
    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    Optional<ChatRoomMember> findByChatRoomAndMemberAndLeftAtIsNull(ChatRoom chatRoom, Member member);
    List<ChatRoomMember> findByChatRoomAndLeftAtIsNull(ChatRoom chatRoom);
    void deleteByMemberId(Long memberId);

    /** 방 목록용. 방을 함께 가져와 방마다 프록시를 다시 조회하지 않는다. */
    @Query("SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.chatRoom "
            + "WHERE crm.member = :member AND crm.leftAt IS NULL")
    List<ChatRoomMember> findActiveWithRoomByMember(@Param("member") Member member);

    /** 여러 방의 참여자를 한 번에 가져온다. 상대방을 방마다 조회하지 않기 위한 것이다. */
    @Query("SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.member JOIN FETCH crm.chatRoom "
            + "WHERE crm.chatRoom IN :rooms AND crm.leftAt IS NULL")
    List<ChatRoomMember> findActiveWithMemberByChatRoomIn(@Param("rooms") List<ChatRoom> rooms);

    /**
     * 내가 속한 모든 방의 안 읽은 메시지 수를 방별로 한 번에 센다.
     * 방마다 기준 시각(lastReadAt)이 다르므로 참여자 행에 메시지를 조인해서 계산한다.
     */
    @Query("SELECT crm.chatRoom.id, COUNT(msg.id) "
            + "FROM ChatRoomMember crm "
            + "LEFT JOIN ChatMessage msg "
            + "  ON msg.chatRoom = crm.chatRoom "
            + " AND msg.senderId <> :memberId "
            + " AND (crm.lastReadAt IS NULL OR msg.createdAt > crm.lastReadAt) "
            + "WHERE crm.member.id = :memberId AND crm.leftAt IS NULL "
            + "GROUP BY crm.chatRoom.id")
    List<Object[]> countUnreadPerRoom(@Param("memberId") Long memberId);
}
