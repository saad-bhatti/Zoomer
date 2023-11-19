package ca.utoronto.utm.mcs;

// request-related import
import com.sun.net.httpserver.HttpExchange;
// mongodb-related imports
import com.mongodb.MongoException;
// other imports
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class Confirm extends Endpoint {
    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400, 500
     * Adds trip info into the database after trip has been requested.
     * Assumption: The driverUid and passengerUid are valid, because we are not allowed to send requests to the
     * location-microservice within this file
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
        String fields[] = {"driver", "passenger", "startTime"};
        Class<?> fieldClasses[] = {String.class, String.class, Integer.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
        // Retrieve the arguments from the body
        String driverUid = body.getString("driver");
        String passengerUid = body.getString("passenger");
        Integer startTime = body.getInt("startTime");
        // Function call to add the trip to the database
        try {
            String tripUid = this.dao.confirmTrip(driverUid, passengerUid, startTime);
            // Function call was successful, prepare body of the response
            JSONObject data = new JSONObject().put("_id", tripUid);
            JSONObject res = new JSONObject().put("data", data);
            this.sendResponse(r, res, 200);
        }
        catch (MongoException e) { // An error occurred within the function call
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
