package taskmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import taskmanagement.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByIdDesc(String task_id);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.taskId = ?1")
    long countByTaskId(String task_id);
}
