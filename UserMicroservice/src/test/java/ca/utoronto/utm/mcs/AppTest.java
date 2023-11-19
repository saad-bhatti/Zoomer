package ca.utoronto.utm.mcs;

// Testing Imports
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
// Client Imports
import java.net.http.HttpClient;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
// Other & Exception Imports
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;

/**
 * HOW TO RUN THE TESTS:
 * Step 1: Run docker-compose up --build -d
 * Step 2: Enter the terminal of the 'usermicroservice' container within Docker Desktop
 * Step 3: Run 'mvn test' in the terminal
 * Note: All tests will run successfully the first time ONLY as the tests does not remove the registered user from the
 * database afterwards
 */

@TestMethodOrder(OrderAnnotation.class)
public class AppTest {
    final static String API_URL = "http://apigateway:8000/user/";

    /** HELPER FUNCTIONS **/
    // Send request to the server and return the response
    private static HttpResponse<String> sendRequest(String method, String endpoint, String reqBody)
            throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** REGISTER TESTS **/
    // User registration successful
    @Test
    @Order(1)
    public void userRegisterPass() throws JSONException, IOException, InterruptedException {
        // Set up request body
        JSONObject reqBody = new JSONObject()
                .put("name", "cscc01")
                .put("email", "cscc01@utsc.ca")
                .put("password", "123456");
        // Send request to the server
        HttpResponse<String> res = sendRequest("POST", "register", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "OK");
        assertEquals(expectedBody.toString(), res.body());
    }
    // User registration unsuccessful due to incomplete request body
    @Test
    @Order(2)
    public void userRegisterFail() throws JSONException, IOException, InterruptedException {
        // Set up request body
        JSONObject reqBody = new JSONObject()
                .put("name", "cscc01")
                .put("email", "cscc01@utsc.ca");
        // Send request to the server
        HttpResponse<String> res = sendRequest("POST", "register", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "BAD REQUEST");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** LOGIN TESTS **/
    // User login successful
    @Test
    @Order(3)
    public void userLoginPass() throws JSONException, IOException, InterruptedException {
        // Set up request body
        JSONObject reqBody = new JSONObject()
                .put("email", "cscc01@utsc.ca")
                .put("password", "123456");
        // Send request to the server
        HttpResponse<String> res = sendRequest("POST", "login", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "OK");
        assertEquals(expectedBody.toString(), res.body());
    }
    // User login unsuccessful due to wrong password
    @Test
    @Order(4)
    public void userLoginFail() throws JSONException, IOException, InterruptedException {
        // Set up request body
        JSONObject reqBody = new JSONObject()
                .put("email", "cscc01@utsc.ca")
                .put("password", "123");
        // Send request to the server
        HttpResponse<String> res = sendRequest("POST", "login", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "NOT FOUND");
        assertEquals(expectedBody.toString(), res.body());
    }
}