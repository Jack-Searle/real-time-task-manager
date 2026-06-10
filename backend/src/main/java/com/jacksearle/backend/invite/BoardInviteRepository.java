package com.jacksearle.backend.invite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardInviteRepository extends JpaRepository<BoardInvite, Long> {
    Optional<BoardInvite> findByToken(String token);

    @Query("""
        SELECT COUNT(i) > 0
        FROM BoardInvite i
        WHERE i.board.id = :boardId
        AND i.invitedUser.id = :userId
        AND i.status = 'PENDING'
        AND i.expiresAt > CURRENT_TIMESTAMP
    """)
    boolean existsActiveInvite(@Param("boardId") Long boardId, @Param("userId") Long userId);

    List<BoardInvite> findByInvitedUserIdAndStatusOrderByCreatedAtDesc(Long invitedUserId, InviteStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardInvite i WHERE i.board.id = :boardId")
    void deleteAllByBoardId(@Param("boardId") Long boardId);
}
