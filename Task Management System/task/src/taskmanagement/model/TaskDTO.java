package taskmanagement.model;

public record TaskDTO(String id, String title, String description, String status, String author, String assignee) {
    public static TaskDTO convertToTaskDTO(Task task) {
        return new TaskDTO(task.getIdentifier(), task.getTitle(), task.getDescription(),
                task.getStatus(), task.getAuthor(), task.getAssignee());
    }
}
