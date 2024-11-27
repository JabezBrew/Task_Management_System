package taskmanagement.service;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import taskmanagement.exception.AccessDeniedException;
import taskmanagement.exception.BadRequestException;
import taskmanagement.exception.NotFoundException;
import taskmanagement.model.*;
import taskmanagement.repository.CommentRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;

    public UserService(UserRepository userRepository, PasswordEncoder encoder, TaskRepository taskRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }

    public static String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return (String) jwt.getClaims().get("sub");
        }
        return auth.getName();
    }

    public void registerUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public TaskDTO createTask(Task task) {
        String uniqueName = String.valueOf(UUID.randomUUID());
        task.setIdentifier(uniqueName);
        task.setAuthor(getCurrentUser().toLowerCase());
        task.setStatus("CREATED");
        task.setAssignee("none");
        taskRepository.save(task);
        return TaskDTO.convertToTaskDTO(task);
    }


    public List<Map<String, Object>> getTasks(String author, String assignee) {
        Task taskFilter = new Task();
        List<Task> tasks;

        if (author != null) taskFilter.setAuthor(author);
        if (assignee != null) taskFilter.setAssignee(assignee);

        if(filtersHaveAtLeastOneNonNullField(taskFilter)) {
            Example<Task> example = Example.of(taskFilter);
            tasks = taskRepository.findAll(example, Sort.by(Sort.Direction.DESC, "id"));
        } else {
            tasks = taskRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }

        return tasks.stream().map(task -> {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getIdentifier());
            taskMap.put("title", task.getTitle());
            taskMap.put("description", task.getDescription());
            taskMap.put("status", task.getStatus());
            taskMap.put("author", task.getAuthor());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("total_comments", commentRepository.countByTaskId(task.getIdentifier()));
            return taskMap;
        }).collect(Collectors.toList());
    }

    public static boolean filtersHaveAtLeastOneNonNullField(Task task) {
        for (Field field : task.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.get(task) != null) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
        return false;
    }

    public TaskDTO assignTask(String taskId, Map<String, String> assignee) {
        String assigneeName = assignee.get("assignee");
        if (taskRepository.findByIdentifier(taskId).isPresent()) {
            boolean validAssignee = assigneeName.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") ||
                    Objects.equals(assigneeName, "none");
            if (validAssignee) {
                if (userRepository.findByEmail(assigneeName).isPresent() || Objects.equals(assigneeName, "none")) {
                    Task task = taskRepository.findByIdentifier(taskId).get();
                    boolean isAuthorCorrect = task.getAuthor().equals(getCurrentUser());
                    if (isAuthorCorrect) {
                        task.setAssignee(assigneeName);
                        taskRepository.save(task);
                        return TaskDTO.convertToTaskDTO(task);
                    } else {
                        throw new AccessDeniedException();
                    }
                } else {
                    throw new NotFoundException("user not found");
                }
            } else {
                throw new BadRequestException("not a valid email address or none");

            }

        } else {
            throw new NotFoundException("task doesn't exist");
        }

    }

    public TaskDTO changeStatus(String taskId, Map<String, String> statusChange) {
        String status = statusChange.get("status");
        List<String> allowedStatus = List.of("CREATED", "IN_PROGRESS", "COMPLETED");
        if (allowedStatus.contains(status)) {
            if(taskRepository.findByIdentifier(taskId).isPresent()) {
                Task task = taskRepository.findByIdentifier(taskId).get();
                boolean isUserAuthorOrAssignee = Objects.equals(task.getAuthor(), getCurrentUser()) ||
                        Objects.equals(task.getAssignee(), getCurrentUser());
                if (isUserAuthorOrAssignee) {
                    task.setStatus(status);
                    taskRepository.save(task);
                    return TaskDTO.convertToTaskDTO(task);
                } else {
                    throw new AccessDeniedException();
                }
            } else {
                throw new NotFoundException("task doesn't exist");
            }
        } else {
            throw new BadRequestException("wrong status");
        }
    }

    public void createComment(String taskId, Comment comment) {
        if (taskRepository.findByIdentifier(taskId).isPresent()) {
            userRepository.findByEmail(getCurrentUser()).orElseThrow(() -> new UsernameNotFoundException("not found"));
            Task task = taskRepository.findByIdentifier(taskId).get();
            String comment_id = String.valueOf(UUID.randomUUID());
            comment.setCommentId(comment_id);
            comment.setTaskId(taskId);
            comment.setAuthor(getCurrentUser());
            commentRepository.save(comment);
            taskRepository.save(task);

        } else {
            throw new NotFoundException("not found");
        }
    }

    public List<CommentDTO> getComments(String taskId) {
        List<Comment> comments;
        userRepository.findByEmail(getCurrentUser()).orElseThrow(() -> new UsernameNotFoundException("not found"));
        if (taskRepository.findByIdentifier(taskId).isPresent()) {
            comments = commentRepository.findByTaskIdOrderByIdDesc(taskId);
        } else {
            throw new NotFoundException("not found");
        }
        return comments.stream().map(CommentDTO::convertToDTO).toList();
    }
}
