package taskmanagement.model;

public record CommentDTO(String id, String task_id, String text, String author) {
    public static CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(comment.getCommentId(), comment.getTaskId(), comment.getText(), comment.getAuthor());
    }
}
