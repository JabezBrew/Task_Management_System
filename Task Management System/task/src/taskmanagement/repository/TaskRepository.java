package taskmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taskmanagement.model.Task;

import java.util.Optional;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByIdentifier(String identifier);
}
