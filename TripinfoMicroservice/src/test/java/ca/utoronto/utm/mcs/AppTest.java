package ca.utoronto.utm.mcs;

// Testing Imports
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * Step 2: Enter the terminal of the 'tripinfomicroservice' container within Docker Desktop
 * Step 3: Run 'mvn test' in the terminal
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {
    final static String tripHost = "http://apigateway:8000/trip/";
    final static String locationHost = "http://apigateway:8000/location/";
    public static String tripId = null;

    /** HELPER FUNCTIONS **/
    // Send request, with a JSON body, to the server and return the response
    public static HttpResponse<String> sendRequestWithBody
        (String method, String uri, String reqBody)
        throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    // Send request without a body to the server and return the response
    public static HttpResponse<String> sendRequestWithoutBody
        (String method, String uri)
        throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
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
        sendRequestWithBody("PUT", locationHost + "user/", user1Body.toString());
        JSONObject user2Body = new JSONObject().put("uid", "1002").put("is_driver", true);
        sendRequestWithBody("PUT", locationHost + "user/", user2Body.toString());
        JSONObject user3Body = new JSONObject().put("uid", "1003").put("is_driver", true);
        sendRequestWithBody("PUT", locationHost + "user/", user3Body.toString());
        // Create roads
        JSONObject road1Body = new JSONObject().put("roadName", "Military Trail").put("hasTraffic", false);
        sendRequestWithBody("PUT", locationHost + "road/", road1Body.toString());
        JSONObject road2Body = new JSONObject().put("roadName", "Ellesmere Road").put("hasTraffic", true);
        sendRequestWithBody("PUT", locationHost + "road/", road2Body.toString());
        JSONObject road3Body = new JSONObject().put("roadName", "Neilson Road").put("hasTraffic", true);
        sendRequestWithBody("PUT", locationHost + "road/", road3Body.toString());
        // Add the routes
        JSONObject route1Body = new JSONObject()
                .put("roadName1", "Ellesmere Road").put("roadName2", "Military Trail")
                .put("hasTraffic", false).put("time", 15);
        sendRequestWithBody("POST", locationHost + "hasRoute/", route1Body.toString());
        JSONObject route2Body = new JSONObject()
                .put("roadName1", "Ellesmere Road").put("roadName2", "Neilson Road")
                .put("hasTraffic", true).put("time", 30);
        sendRequestWithBody("POST", locationHost + "hasRoute/", route2Body.toString());
        // Update the location of the users
        JSONObject user1LocationBody = new JSONObject()
                .put("longitude", 40.53).put("latitude", 60.75).put("street", "Military Trail");
        sendRequestWithBody("PATCH", locationHost + "1001", user1LocationBody.toString());
        JSONObject user2LocationBody = new JSONObject()
                .put("longitude", 60.32).put("latitude", 20.35).put("street", "Ellesmere Road");
        sendRequestWithBody("PATCH", locationHost + "1002", user2LocationBody.toString());
        JSONObject user3LocationBody = new JSONObject()
                .put("longitude", 101.17).put("latitude", 99.49).put("street", "Neilson Road");
        sendRequestWithBody("PATCH", locationHost + "1003", user3LocationBody.toString());

        // Add a trip for a user
        JSONObject confirmTrip = new JSONObject()
                .put("driver", "1002").put("passenger", "1001").put("startTime", 1015);
        HttpResponse<String> res = sendRequestWithBody("POST", tripHost + "confirm",
                confirmTrip.toString());
        String data = new JSONObject(res.body()).getString("data");
        tripId = new JSONObject(data).getString("_id");

    }
    // Remove all the instances added for testing
    @AfterAll
    static public void afterTests() throws JSONException, IOException, InterruptedException {
        // Delete users
        JSONObject user1Body = new JSONObject().put("uid", "1001");
        sendRequestWithBody("DELETE", locationHost + "user/", user1Body.toString());
        JSONObject user2Body = new JSONObject().put("uid", "1002");
        sendRequestWithBody("DELETE", locationHost + "user/", user2Body.toString());
        JSONObject user3Body = new JSONObject().put("uid", "1003");
        sendRequestWithBody("DELETE", locationHost + "user/", user3Body.toString());
        // Delete roads
        JSONObject road1Body = new JSONObject().put("roadName", "Military Trail");
        sendRequestWithBody("DELETE", locationHost + "road/", road1Body.toString());
        JSONObject road2Body = new JSONObject().put("roadName", "Ellesmere Road");
        sendRequestWithBody("DELETE", locationHost + "road/", road2Body.toString());
        JSONObject road3Body = new JSONObject().put("roadName", "Neilson Road");
        sendRequestWithBody("DELETE", locationHost + "road/", road3Body.toString());
    }

    /** REQUEST TESTS **/
    // There are drivers within the given radius
    @Test
    @Order(1)
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {
        // Send request to the server
        JSONObject reqBody = new JSONObject().put("uid", "1001").put("radius", 50);
        HttpResponse<String> res = sendRequestWithBody("POST", tripHost + "request",
                reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        String[] expectedData = { "1002" };
        JSONObject expectedBody = new JSONObject().put("data", expectedData).put("status", "OK");
        assertEquals(expectedBody.toString(), res.body());
    }
    // The user does not exist
    @Test
    @Order(2)
    public void tripRequestFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        JSONObject reqBody = new JSONObject().put("uid", "0").put("radius", 50);
        HttpResponse<String> res = sendRequestWithBody("POST", tripHost + "request", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "NOT FOUND");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** CONFIRM TESTS **/
    // Passenger successfully creates a trip
    @Test
    @Order(3)
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        // Send request to the server
        JSONObject reqBody = new JSONObject().put("driver", "1002")
                .put("passenger", "0").put("startTime", 123);
        HttpResponse<String> res = sendRequestWithBody("POST", tripHost + "confirm", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Check the response's body contains _id
        assertTrue(res.body().contains("_id"));
        assertTrue(res.body().contains("status"));
    }
    // Request is unsuccessful due to missing parameter in request body
    @Test
    @Order(4)
    public void tripConfirmFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        JSONObject reqBody = new JSONObject().put("driver", "1002").put("passenger", "0");
        HttpResponse<String> res = sendRequestWithBody("POST", tripHost + "confirm", reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "BAD REQUEST");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** TRIP TESTS **/
    // Adds extra information to the trip when the trip is done
    @Test
    @Order(5)
    public void patchTripPass() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        JSONObject reqBody = new JSONObject().put("distance", 20.25).put("endTime", 1234)
                .put("timeElapsed", 1500).put("discount", 67.12).put("totalCost", 92.32)
                .put("driverPayout", 35.35);
        // Send request to the server
        HttpResponse<String> res = sendRequestWithBody("PATCH", tripHost + tripId, reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "OK");
        assertEquals(expectedBody.toString(), res.body());
    }
    // Request unsuccessful due to wrong request body parameter's type
    @Test
    @Order(6)
    public void patchTripFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        JSONObject reqBody = new JSONObject().put("distance", 20.25).put("endTime", "1234")
                .put("timeElapsed", 1500).put("discount", 67.12).put("totalCost", 92.32)
                .put("driverPayout", "35.35");
        // Send request to the server
        HttpResponse<String> res = sendRequestWithBody("PATCH", tripHost + tripId, reqBody.toString());
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "BAD REQUEST");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** PASSENGER TESTS **/
    // Gets all the trips of a passenger (Both completed
    @Test
    @Order(7)
    public void tripsForPassengerPass() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", tripHost + "passenger/" + "1001");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body (can only test status as the data will vary each time the test is run)
        assertTrue(res.body().contains("\"status\":\"OK\""));
    }

    // // Request is unsuccessful due to missing parameter in request url
    @Test
    @Order(8)
    public void tripsForPassengerFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", tripHost + "passenger");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "BAD REQUEST");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** DRIVER TESTS **/
    @Test
    @Order(9)
    public void tripsForDriverPass() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", tripHost + "driver/" + "1002");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body (can only test status as the data will vary each time the test is run)
        assertTrue(res.body().contains("\"status\":\"OK\""));
    }
    // Request is unsuccessful due to missing parameter in request url
    @Test
    @Order(10)
    public void tripsForDriverFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", tripHost + "driver");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "BAD REQUEST");
        assertEquals(expectedBody.toString(), res.body());
    }

    /** DRIVER-TIME TESTS **/
    @Test
    @Order(11)
    public void driverTimePass() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", tripHost + "driverTime/" + tripId);
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_OK, res.statusCode());
        // Test the response's body
        JSONObject expectedData = new JSONObject().put("arrival_time", "15");
        JSONObject expectedBody = new JSONObject().put("data", expectedData).put("status", "OK");
        assertEquals(expectedBody.toString(), res.body());
    }
    // Request is unsuccessful due to missing parameter in request url
    @Test
    @Order(12)
    public void driverTimeFail() throws IOException, InterruptedException, JSONException {
        // Send request to the server
        HttpResponse<String> res = sendRequestWithoutBody("GET", tripHost + "driverTime");
        // Test the response's status code
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, res.statusCode());
        // Test the response's body
        JSONObject expectedBody = new JSONObject().put("status", "BAD REQUEST");
        assertEquals(expectedBody.toString(), res.body());
    }

}
