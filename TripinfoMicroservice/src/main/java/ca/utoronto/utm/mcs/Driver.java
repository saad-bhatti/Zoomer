package ca.utoronto.utm.mcs;

// request-related import
import com.sun.net.httpserver.HttpExchange;
// mongodb-related imports
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
// other imports
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class Driver extends Endpoint {
    /**
     * GET /trip/driver/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
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
        String uid = urlSegments[3];
        try {
            // Function call to retrieve information from database
            MongoCursor<Document> driverTrips = this.dao.getDriverTrips(uid);
            // User with the specified uid does not exist or has no trips as a driver
            if (!driverTrips.hasNext()){
                this.sendStatus(r, 404);
                return;
            }
            // User has at least one trip as a driver, retrieve the relevant information
            ArrayList<JSONObject> trips = new ArrayList<>();
            while (driverTrips.hasNext()){
                Document allTripInfo = driverTrips.next();
                allTripInfo.remove("driver");
                allTripInfo.remove("totalCost");
                JSONObject relevantTripInfo = new JSONObject(allTripInfo.toJson());
                trips.add(relevantTripInfo);
            }
            // Prepare the response body
            JSONObject data = new JSONObject().put("trips", trips);
            JSONObject res = new JSONObject().put("data", data);
            this.sendResponse(r, res, 200);
        }
        catch (MongoException e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
