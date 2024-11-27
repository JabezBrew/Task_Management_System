import com.google.gson.Gson;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.expect.json.builder.JsonArrayBuilder;
import org.hyperskill.hstest.testing.expect.json.builder.JsonObjectBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isArray;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isString;

public class ApplicationTests extends SpringTest {
    private static final String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String accountsUrl = "/api/accounts";
    private static final String tasksUrl = "/api/tasks";
    private static final String tokenUrl = "/api/auth/token";
    private final Gson gson = new Gson();

    public ApplicationTests() {
        super("../tms_db.mv.db");
    }

    CheckResult testCreateUser(TestUser user, int expectedCode) {
        var content = gson.toJson(user);
        var response = post(accountsUrl, content).send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        return CheckResult.correct();
    }

    CheckResult testLogin(TestUser user, int expectedCode) {
        var response = post(tokenUrl, Map.of())
                .basicAuth(user.getEmail(), user.getPassword())
                .send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("token", isString())
            );

            var token = response.getJson().getAsJsonObject().get("token").getAsString();
            user.setToken(token);
        }

        return CheckResult.correct();
    }

    CheckResult testCreateTask(TestTask task, TestUser author, int expectedCode) {
        var content = gson.toJson(task);
        var response = post(tasksUrl, content)
                .addHeader("Authorization", "Bearer " + author.getToken())
                .send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("id", isString())
                            .value("title", task.getTitle())
                            .value("description", task.getDescription())
                            .value("status", "CREATED")
                            .value("author", author.getEmail())
                            .value("assignee", task.getAssignee() == null ? "none" : task.getAssignee())
            );
            var id = response.getJson().getAsJsonObject().get("id").getAsString();
            task.setAuthor(author.getEmail());
            task.setId(id);
            task.setStatus("CREATED");
        }

        return CheckResult.correct();
    }

    CheckResult testAssignTask(TestUser author, String assignee, TestTask task, int expectedCode) {
        var content = assignee != null ? "{\"assignee\":\"" + assignee + "\"}" : "{\"assignee\":\"none\"}";
        var endpoint = tasksUrl + "/" + task.getId() + "/assign";
        var response = put(endpoint, content)
                .addHeader("Authorization", "Bearer " + author.getToken())
                .send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("id", task.getId())
                            .value("title", task.getTitle())
                            .value("description", task.getDescription())
                            .value("status", "CREATED")
                            .value("author", author.getEmail())
                            .value("assignee", assignee == null ? "none" : assignee)
            );

            task.setAssignee(assignee);
        }

        return CheckResult.correct();
    }

    CheckResult testChangeStatus(TestUser user, TestTask task, String status, int expectedCode) {
        var content = "{\"status\":\"" + status + "\"}";
        var endpoint = tasksUrl + "/" + task.getId() + "/status";
        var response = put(endpoint, content)
                .addHeader("Authorization", "Bearer " + user.getToken())
                .send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("id", task.getId())
                            .value("title", task.getTitle())
                            .value("description", task.getDescription())
                            .value("status", status)
                            .value("author", task.getAuthor())
                            .value("assignee", task.getAssignee() == null ? "none" : task.getAssignee())
            );

            task.setStatus(status);
        }

        return CheckResult.correct();
    }

    CheckResult testPostComment(TestUser author, TestTask task, TestComment comment, int expectedCode) {
        var content = "{\"text\":\"" + comment.getText() + "\"}";
        var endpoint = tasksUrl + "/" + task.getId() + "/comments";
        var response = post(endpoint, content)
                .addHeader("Authorization", "Bearer " + author.getToken())
                .send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            comment.setTaskId(task.getId());
            comment.setAuthor(author.getEmail());
            task.incrementTotalComments();
        }

        return CheckResult.correct();
    }

    CheckResult testGetComments(TestUser user, TestTask task, List<TestComment> expectedComments, int expectedCode) {
        var endpoint = tasksUrl + "/" + task.getId() + "/comments";
        var response = get(endpoint)
                .addHeader("Authorization", "Bearer " + user.getToken())
                .send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            JsonArrayBuilder arrayBuilder = isArray(expectedComments.size());
            for (var comment : expectedComments) {
                JsonObjectBuilder objectBuilder = isObject()
                        .value("id", isString())
                        .value("task_id", comment.getTaskId())
                        .value("text", comment.getText())
                        .value("author", comment.getAuthor());
                arrayBuilder = arrayBuilder.item(objectBuilder);
            }
            expect(response.getContent()).asJson().check(arrayBuilder);
        }

        return CheckResult.correct();
    }

    CheckResult testGetAllTasks(TestUser user, List<TestTask> expectedTasks, int expectedCode) {
        return testGetTasksByAuthorAndAssignee(user, null, null, expectedTasks, expectedCode);
    }

    CheckResult testGetTasksByAuthorAndAssignee(TestUser user, String author, String assignee, List<TestTask> expectedTasks, int expectedCode) {
        var request = get(tasksUrl).addHeader("Authorization", "Bearer " + user.getToken());
        if (author != null) {
            request = request.addParam("author", author);
        }
        if (assignee != null) {
            request = request.addParam("assignee", assignee);
        }

        var response = request.send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            JsonArrayBuilder arrayBuilder = isArray(expectedTasks.size());
            for (var task : expectedTasks) {
                JsonObjectBuilder objectBuilder = isObject()
                        .value("id", task.getId())
                        .value("title", task.getTitle())
                        .value("description", task.getDescription())
                        .value("status", task.getStatus())
                        .value("author", task.getAuthor())
                        .value("assignee", task.getAssignee() == null ? "none" : task.getAssignee())
                        .value("total_comments", task.getTotalComments() == null ? 0 : task.getTotalComments());
                arrayBuilder = arrayBuilder.item(objectBuilder);
            }
            expect(response.getContent()).asJson().check(arrayBuilder);
        }

        return CheckResult.correct();
    }

    private String getRequestDetails(HttpResponse response) {
        var uri = response.getRequest().getUri();
        var method = response.getRequest().getMethod();
        var requestBody = response.getRequest().getContent();
        return "\nRequest: %s %s\nRequest body: %s\n".formatted(method, uri, requestBody);
    }

    private CheckResult reloadServer() {
        try {
            reloadSpring();
        } catch (Exception e) {
            throw new WrongAnswer("Failed to reload application");
        }
        return CheckResult.correct();
    }

    TestUser alice = TestUser.alice();
    TestUser bob = TestUser.bob();

    TestTask task1 = TestTask.task1();
    TestTask task2 = TestTask.task2();
    TestTask task3 = TestTask.task3();

    TestComment comment1 = TestComment.comment1();
    TestComment comment2 = TestComment.comment2();
    TestComment comment3 = TestComment.comment3();
    TestComment comment4 = TestComment.comment4();
    TestComment comment5 = TestComment.comment5();

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // register user
            () -> testCreateUser(alice, 200), // #1
            () -> testCreateUser(alice, 409),
            () -> testCreateUser(alice.withEmail("ALICE@email.com"), 409),
            () -> testCreateUser(bob, 200),
            () -> testCreateUser(TestUser.withBadEmail(" "), 400), // #5
            () -> testCreateUser(TestUser.withBadEmail(null), 400),
            () -> testCreateUser(TestUser.withBadEmail("malformed@email."), 400),
            () -> testCreateUser(TestUser.withBadPassword(null), 400),
            () -> testCreateUser(TestUser.withBadPassword("      "), 400),
            () -> testCreateUser(TestUser.withBadPassword("12345"), 400), // #10

            // test login
            () -> testLogin(alice, 200),
            () -> testLogin(bob, 200),
            () -> testLogin(alice.withPassword("badpassword"), 401),
            () -> testLogin(alice.withEmail("test@test.com"), 401),

            // create task
            () -> testCreateTask(task1, alice, 200), // #15
            () -> testCreateTask(task2, alice, 200),
            () -> testCreateTask(task3, bob, 200),
            () -> testCreateTask(task3.withTitle(null), bob, 400),
            () -> testCreateTask(task1.withTitle(" "), bob, 400),
            () -> testCreateTask(task1.withDescription(null), bob, 400),   // #20
            () -> testCreateTask(task1.withDescription(" "), bob, 400),
            () -> testCreateTask(task1, bob.withToken(fakeToken), 401),

            // test assignment
            () -> testAssignTask(alice, bob.getEmail(), task1, 200),
            () -> testAssignTask(alice, bob.getEmail(), task2, 200),
            () -> testAssignTask(alice, null, task1, 200), // #25
            () -> testAssignTask(bob, alice.getEmail(), task3, 200),
            () -> testAssignTask(bob, bob.getEmail(), task1, 403),
            () -> testAssignTask(alice, UUID.randomUUID() + "@test.com", task1, 404),
            () -> testAssignTask(alice, bob.getEmail(), task1.withId("987654321"), 404),
            () -> testAssignTask(alice.withToken(fakeToken), bob.getEmail(), task1, 401), // #30

            // test change status
            () -> testChangeStatus(alice, task1, "IN_PROGRESS", 200),
            () -> testChangeStatus(alice, task2, "IN_PROGRESS", 200),
            () -> testChangeStatus(bob, task2, "COMPLETED", 200),
            () -> testChangeStatus(bob, task1, "COMPLETED", 403),
            () -> testChangeStatus(alice, task1.withId("98765432"), "COMPLETED", 404), // #35
            () -> testChangeStatus(alice.withToken(fakeToken), task1, "COMPLETED", 401),

            // post comments
            () -> testPostComment(alice, task1, comment1, 200),
            () -> testPostComment(alice, task1, comment2, 200),
            () -> testPostComment(bob, task1, comment3, 200),
            () -> testPostComment(bob, task3, comment4, 200), // #40
            () -> testPostComment(alice, task3, comment5, 200),
            () -> testPostComment(alice, task3, comment5.withText("   "), 400),
            () -> testPostComment(alice.withToken(fakeToken), task3, comment5, 401),
            () -> testPostComment(alice, task3.withId("999558881"), comment5, 404),

            // get comments
            () -> testGetComments(alice, task1, List.of(comment3, comment2, comment1), 200), // #45
            () -> testGetComments(alice, task2, List.of(), 200),
            () -> testGetComments(alice, task3, List.of(comment5, comment4), 200),
            () -> testGetComments(alice.withToken(fakeToken), task3, List.of(), 401),
            () -> testGetComments(alice, task3.withId("123456789"), List.of(), 404),

            // get all tasks
            () -> testGetAllTasks(alice, List.of(task3, task2, task1), 200), // #50
            () -> testGetAllTasks(bob, List.of(task3, task2, task1), 200),
            () -> testGetAllTasks(alice.withToken(fakeToken), List.of(), 401),

            // get tasks by author and assignee
            () -> testGetTasksByAuthorAndAssignee(alice, alice.getEmail(), null, List.of(task2, task1), 200),
            () -> testGetTasksByAuthorAndAssignee(bob, alice.getEmail(), null, List.of(task2, task1), 200),
            () -> testGetTasksByAuthorAndAssignee(alice, bob.getEmail(), null, List.of(task3), 200), // #55
            () -> testGetTasksByAuthorAndAssignee(alice, "unknown", null, List.of(), 200),
            () -> testGetTasksByAuthorAndAssignee(alice, null, "unknown", List.of(), 200),
            () -> testGetTasksByAuthorAndAssignee(alice, alice.getEmail(), bob.getEmail(), List.of(task2), 200),
            () -> testGetTasksByAuthorAndAssignee(alice, bob.getEmail(), alice.getEmail(), List.of(task3), 200),

            // test persistence
            this::reloadServer, // #60
            () -> testCreateUser(alice, 409),
            () -> testLogin(alice, 200),
            () -> testGetAllTasks(alice, List.of(task3, task2, task1), 200),
    };
}
