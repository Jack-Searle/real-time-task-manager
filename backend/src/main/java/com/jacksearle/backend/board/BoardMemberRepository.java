package com.jacksearle.backend.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    List<BoardMember> findByUserId(Long userId);

    List<BoardMember> findByBoardId(Long boardId);

    Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);

    boolean existsByBoardIdAndUserIdAndRole(Long boardId, Long userId, BoardRole role);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardMember bm WHERE bm.board.id = :boardId")
    void deleteAllByBoardId(@Param("boardId") Long boardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardMember bm WHERE bm.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
