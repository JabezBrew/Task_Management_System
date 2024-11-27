public class TestComment {
    private String text;
    private String taskId;
    private String author;

    private TestComment(String text) {
        this.text = text;
    }

    public static TestComment comment1() {
        return new TestComment("comment 1");
    }

    public static TestComment comment2() {
        return new TestComment("comment 2");
    }

    public static TestComment comment3() {
        return new TestComment("comment 3");
    }

    public static TestComment comment4() {
        return new TestComment("comment 4");
    }

    public static TestComment comment5() {
        return new TestComment("comment 5");
    }

    public TestComment withText(String text) {
        var copy = new TestComment(text);
        copy.setTaskId(this.taskId);
        copy.setAuthor(this.author);
        return copy;
    }

    public String getText() {
        return text;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
