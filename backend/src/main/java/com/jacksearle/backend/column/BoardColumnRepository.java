package com.jacksearle.backend.column;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {

    List<BoardColumn> findByBoardIdOrderByColumnOrder(Long boardId);

    @Query("SELECT COALESCE(MAX(bc.columnOrder), -1) FROM BoardColumn bc WHERE bc.board.id = :boardId")
    int findMaxColumnOrderByBoardId(@Param("boardId") Long boardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardColumn bc WHERE bc.board.id = :boardId")
    void deleteAllByBoardId(@Param("boardId") Long boardId);
}
