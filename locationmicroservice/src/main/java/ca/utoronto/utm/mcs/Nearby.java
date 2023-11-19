package ca.utoronto.utm.mcs;

// exchange-related import
import com.sun.net.httpserver.HttpExchange;
// neo4j-related imports
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
// other imports
import org.json.*;
import java.io.IOException;

public class Nearby extends Endpoint {
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */
    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        /////////// PART A: PREPARE THE PARAMETERS ///////////
        String uid; int radius;
        // Check the parameters have been provided properly
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        // Check that two parameters have been provided
        String[] paramValues = params[3].split("\\?radius=");
        if (paramValues.length != 2 || paramValues[0].isEmpty() || paramValues[1].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        /////////// PART B: FIND & RETURN THE DRIVERS WITHIN THE RADIUS ///////////
        try {
            // Assign the variables their values
            uid = paramValues[0];
            radius = Integer.parseInt(paramValues[1]);
            // Function call to dao function
            Result result = this.dao.getNearbyDrivers(uid, radius);
            // No driver exist nearby within the radius
            if (!result.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }
            // Construct the data of the response body
            JSONObject data = new JSONObject();
            Record driver; String driverUid, street; Double longitude, latitude;
            while (result.hasNext()) {
                driver = result.next();
                // Retrieve the information from the query
                driverUid = driver.get("uid").asString();
                longitude = driver.get("longitude").asDouble();
                latitude = driver.get("latitude").asDouble();
                street = driver.get("street").asString();
                // Put the driver's information into a JSONObject
                JSONObject driverInfo = new JSONObject()
                        .put("longitude", longitude).put("latitude", latitude).put("street", street);
                // Add the driver to the data
                data.put(driverUid, driverInfo);
            }
            // Set the data and status of the response
            JSONObject res = new JSONObject().put("data", data);
            // Send the response
            this.sendResponse(r, res, 200);
        } catch (NumberFormatException e) {     // Improper radius provided
            e.printStackTrace();
            this.sendStatus(r, 400);
        } catch (Exception e) {                 // Internal error occurred
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
