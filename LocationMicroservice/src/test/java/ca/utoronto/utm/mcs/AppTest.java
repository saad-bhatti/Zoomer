package ca.utoronto.utm.mcs;

// Testing Imports
import org.junit.jupiter.api.*;

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
 * Step 2: Enter the terminal of the 'locationmicroservice' container within Docker Desktop
 * Step 3: Run 'mvn test' in the terminal
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {
    final static String API_URL = "http://apigateway:8000/location/";

    /** HELPER FUNCTIONS **/
    // Send request, with a JSON body, to the server and return the response
    public static HttpResponse<String> sendRequestWithBody(String method, String endpoint,String reqBody)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    // Send request without a body to the server and return the response
    public static HttpResponse<String> sendRequestWithoutBody(String method, String endpoint)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** TEST SET-UP & CLEAN-UP **/
    // Add 3 drivers, 3 roads, and 2 routes to the database
    @BeforeAll
    static public void beforeTests() throws JSONException, IOException, InterruptedException {
        // Create users
        JSONObject user1Body = new JSONObject().put("uid", "1001").put("is_driver", false);
        sendRequestWithBody("PUT", "user/", user1Body.toString());
        JSONObject user2Body = new JSONObject().put("uid", "1002").put("is_driver", true);
        sendRequestWithBody("PUT", "user/", user2Body.toString());
        JSONObject user3Body = new JSONObject().put("uid", "1003").put("is_driver", true);
        sendRequestWithBody("PUT", "user/", user3Body.toString());
        // Create roads
        JSONObject road1Body = new JSONObject().put("roadName", "Military Trail").put("hasTraffic", false);
        sendRequestWithBody("PUT", "road/", road1Body.toString());
        JSONObject road2Body = new JSONObject().put("roadName", "Ellesmere Road").put("hasTraffic", true);
        sendRequestWithBody("PUT", "road/", road2Body.toString());
        JSONObject road3Body = new JSONObject().put("roadName", "Neilson Road").put("hasTraffic", true);
        sendRequestWithBody("PUT", "road/", road3Body.toString());
        // Add the routes
        JSONObject route1Body = new JSONObject()
                .put("roadName1", "Ellesmere Road").put("roadName2", "Military Trail")
                .put("hasTraffic", false).put("time", 15);
        sendRequestWithBody("POST", "hasRoute/", route1Body.toString());
        JSONObject route2Body = new JSONObject()
                .put("roadName1", "Ellesmere Road").put("roadName2", "Neilson Road")
                .put("hasTraffic", true).put("time", 30);
        sendRequestWithBody("POST", "hasRoute/", route2Body.toString());
        // Update the location of the users
        JSONObject user1LocationBody = new JSONObject()
                .put("longitude", 40.53).put("latitude", 60.75).put("street", "Military Trail");
        sendRequestWithBody("PATCH", "1001", user1LocationBody.toString());
        JSONObject user2LocationBody = new JSONObject()
                .put("longitude", 60.32).put("latitude", 20.35).put("street", "Ellesmere Road");
        sendRequestWithBody("PATCH", "1002", user2LocationBody.toString());
        JSONObject user3LocationBody = new JSONObject()
                .put("longitude", 101.17).put("latitude", 99.49).put("street", "Neilson Road");
        sendRequestWithBody("PATCH", "1003", user3LocationBody.toString());
    }
    // Remove all the instances added for testing
    @AfterAll
    static public void afterTests() throws JSONException, IOException, InterruptedException {
        // Delete users
        JSONObject user1Body = new JSONObject().put("uid", "1001");
        sendRequestWithBody("DELETE", "user/", user1Body.toString());
        JSONObject user2Body = new JSONObject().put("uid", "1002");
        sendRequestWithBody("DELETE", "user/", user2Body.toString());
        JSONObject user3Body = new JSONObject().put("uid", "1003");
        sendRequestWithBody("DELETE", "user/", user3Body.toString());
        // Delete roads
        JSONObject road1Body = new JSONObject().put("roadName", "Military Trail");
        sendRequestWithBody("DELETE", "road/", road1Body.toString());
        JSONObject road2Body = new JSONObject().put("roadName", "Ellesmere Road");
        sendRequestWithBody("DELETE", "road/", road2Body.toString());
        JSONObject road3Body = new JSONObject().put("roadName", "Neilson Road");
        sendRequestWithBody("DELETE", "road/", road3Body.toString());
    }

    /** NEARBY-DRIVER TESTS **/
    // There are drivers within the given radius
    @Test
    @Order(1)
    public void nearbyDriverPass() throws JSONException, IOException, InterruptedException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", "nearbyDriver/1001?radius=50");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        JSONObject expectedUserInfo = new JSONObject()
                .put("longitude", 60.32).put("latitude", 20.35).put("street", "Ellesmere Road");
        JSONObject expectedData = new JSONObject().put("1002", expectedUserInfo);
        JSONObject expectedBody = new JSONObject().put("data", expectedData).put("status", "OK");
        assertEquals(expectedBody.toString(), res.body());
    }
    // The user does not exist
    @Test
    @Order(2)
    public void nearbyDriverFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", "nearbyDriver/0?radius=50");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "NOT FOUND");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** NAVIGATION TESTS **/
    // There exists a path between the passenger & driver
    @Test
    @Order(3)
    public void navigationPass() throws JSONException, IOException, InterruptedException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", "navigation/1002?passengerUid=1001");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        JSONObject[] expectedRoute = { new JSONObject()
                .put("street", "Ellesmere Road")
                .put("time", 15)
                .put("is_traffic", true)
        };
        JSONObject expectedData = new JSONObject()
                .put("total_time", 15)
                .put("route", expectedRoute);
        JSONObject expectedBody = new JSONObject().put("status", "OK").put("data", expectedData);
        assertEquals(expectedBody.toString(), res.body());
    }
    // There does not exist a path between the passenger & driver
    @Test
    @Order(4)
    public void navigationFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", "navigation/1003?passengerUid=1001");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "NOT FOUND");
        assertEquals(expectedBody.toString(), res.body());
    }
}
