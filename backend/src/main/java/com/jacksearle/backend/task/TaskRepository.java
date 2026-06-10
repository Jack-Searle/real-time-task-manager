package com.jacksearle.backend.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByBoardId(Long boardId);

    List<Task> findByBoardColumnIdOrderByPositionAsc(Long columnId);

    @Query("SELECT COALESCE(MAX(t.position), -1) FROM Task t WHERE t.boardColumn.id = :columnId")
    int findMaxPositionByBoardColumnId(@Param("columnId") Long columnId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.board.id = :boardId")
    void deleteAllByBoardId(@Param("boardId") Long boardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.boardColumn.id = :columnId")
    void deleteAllByBoardColumnId(@Param("columnId") Long columnId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.assignedTo = null WHERE t.board.id = :boardId AND t.assignedTo.id = :userId")
    void unassignUserFromBoardTasks(@Param("boardId") Long boardId, @Param("userId") Long userId);
}
