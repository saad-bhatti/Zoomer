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
// other imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Request extends Endpoint {
    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius using location microservice. List should be obtained
     * from navigation endpoint in location microservice.
     */
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // Validate the url
        String[] urlSegments = r.getRequestURI().getPath().split("/");
        if (urlSegments.length != 3) {
            this.sendStatus(r, 400);
            return;
        }
        // Validate the body of the request
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"uid", "radius"};
        Class<?> fieldClasses[] = {String.class, Integer.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
        // Retrieve the arguments from the body
        String uid = body.getString("uid");
        Integer radius = body.getInt("radius");
        // Send a request to location-microservice to retrieve the necessary information
        try {
            HttpResponse<String> nearbyDriverRes =
                    this.sendRequestWithoutBody("GET", "nearbyDriver/" + uid +"?radius=" + radius);
            // The request was not successful
            if (nearbyDriverRes.statusCode() != 200) {
                this.sendStatus(r, nearbyDriverRes.statusCode());
                return;
            }
            // The request was successful, so prepare the data from its response
            JSONObject nearbyDriverResBody = new JSONObject(nearbyDriverRes.body());
            Iterator keys = nearbyDriverResBody.getJSONObject("data").keys();
            ArrayList<String> data = new ArrayList<>();
            while (keys.hasNext()) {
                String currKey = keys.next().toString();
                data.add(currKey);
            }
            // Send the response back for the original request
            JSONObject responseBody = new JSONObject().put("data", data);
            this.sendResponse(r, responseBody, 200);
        } catch (Exception e) { // An error occurred
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
