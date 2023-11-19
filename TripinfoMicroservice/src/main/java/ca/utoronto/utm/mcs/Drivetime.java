package ca.utoronto.utm.mcs;

// request-related imports
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
// routing-related imports
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
// mongodb-related imports
import com.mongodb.client.MongoCursor;
import org.bson.Document;
// other imports
import java.io.IOException;

public class Drivetime extends Endpoint {
    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with the given _id. Time should be obtained from
     * navigation endpoint in location microservice.
     */
    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // Validate the url
        String[] urlSegments = r.getRequestURI().getPath().split("/");
        if (urlSegments.length != 4 || urlSegments[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        // Retrieve the argument from the url
        String _id = urlSegments[3];
        // Check the trip exists, and if so, send request to location-microservice
        try {
            MongoCursor<Document> trip = this.dao.getTripById(_id);
            // Trip with specified uid does not exist in the database
            if (!trip.hasNext()){
                this.sendStatus(r, 404);
                return;
            }
            // Trip does exist, so retrieve the relevant information
            Document tripInfo = trip.next();
            String driverUid = tripInfo.getString("driver");
            String passengerUid = tripInfo.getString("passenger");
            // Send the request to the location microservice
            HttpResponse<String> navigationRes = this.sendRequestWithoutBody(
                "GET",
                "navigation/" + driverUid +"?passengerUid=" + passengerUid
            );
            // The request was not successful
            if (navigationRes.statusCode() != 200) this.sendStatus(r, navigationRes.statusCode());
            // Request was successful, so retrieve information from response
            else {
                JSONObject resBody = new JSONObject(navigationRes.body());
                String arrival_time = resBody.getJSONObject("data").getString("total_time");
                JSONObject data = new JSONObject().put("arrival_time", arrival_time);
                JSONObject res = new JSONObject().put("data", data);
                this.sendResponse(r, res, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

    /** Helper function to send a request without a body **/
    private static HttpResponse<String> sendRequestWithoutBody
        (String method, String endpoint)
        throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://locationmicroservice:8000/location/" + endpoint))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
