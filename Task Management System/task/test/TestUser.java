public class TestUser {
    private final String email;
    private final String password;
    private String token;

    private TestUser(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public static TestUser alice() {
        return new TestUser("alice@email.com", "password");
    }

    public static TestUser bob() {
        return new TestUser("bob@example.com", "123456");
    }

    public static TestUser withBadEmail(String badEmail) {
        return new TestUser(badEmail, "password");
    }

    public static TestUser withBadPassword(String badPassword) {
        return new TestUser("test@test.com", badPassword);
    }

    public TestUser withEmail(String email) {
        return new TestUser(email, this.password);
    }

    public TestUser withPassword(String password) {
        return new TestUser(this.email, password);
    }

    public TestUser withToken(String token) {
        var copy = new TestUser(this.email, this.password);
        copy.setToken(token);
        return copy;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
