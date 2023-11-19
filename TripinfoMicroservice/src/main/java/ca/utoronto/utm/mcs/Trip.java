package ca.utoronto.utm.mcs;

// request-related import
import com.mongodb.client.result.UpdateResult;
import com.sun.net.httpserver.HttpExchange;
// mongodb-related imports
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
// other imports
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class Trip extends Endpoint {
    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, discount, totalCost, driverPayout
     * @return 200, 400, 404, 500
     * Adds extra information to the trip with the given id when the trip is done.
     * Assumption: All the values are correctly calculated prior to the request
     */
    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        // Validate the url
        String[] urlSegments = r.getRequestURI().getPath().split("/");
        if (urlSegments.length != 3 || urlSegments[2].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        // Validate the body of the request
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"distance", "endTime", "timeElapsed", "discount", "totalCost", "driverPayout"};
        Class<?> fieldClasses[] = {Double.class, Integer.class, Integer.class, Double.class, Double.class, Double.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
        // Retrieve the arguments from the url and body
        String _id = urlSegments[2];
        Double distance = body.getDouble("distance");
        Integer endTime = body.getInt("endTime");
        Integer timeElapsed = body.getInt("timeElapsed");
        Double discount = body.getDouble("discount");
        Double totalCost = body.getDouble("totalCost");
        Double driverPayout = body.getDouble("driverPayout");
        // Function call to update the trip in the database
        try {
            MongoCursor<Document> trip = this.dao.getTripById(_id);
            // Trip with specified _id does not exist in the database
            if (!trip.hasNext()){
                this.sendStatus(r, 404);
                return;
            }
            // Trip does exist, so update it
            UpdateResult updateResult =
                    this.dao.updateTripInfo(_id, distance, endTime, timeElapsed, discount, totalCost, driverPayout);
            // Check the document was actually updated
            if (updateResult.getModifiedCount() == 1) this.sendStatus(r, 200);
            else this.sendStatus(r, 500);
        }
        catch (MongoException e) { // An error occurred within MongoDB
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
