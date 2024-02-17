package ca.utoronto.utm.mcs;

// exchange-related import
import com.sun.net.httpserver.HttpExchange;
// neo4j-related imports
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
// other imports
import org.json.*;
import java.io.IOException;

public class Navigation extends Endpoint {
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */
    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        /////////// PART A: PREPARE THE PARAMETERS ///////////
        String driverUid, passengerUid;
        // Check the parameters have been provided properly
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        // Check that two parameters have been provided
        String[] paramValues = params[3].split("\\?passengerUid=");
        if (paramValues.length != 2 || paramValues[0].isEmpty() || paramValues[1].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        /////////// PART B: FIND & RETURN THE FASTEST PATH'S INFO ///////////
        try {
            // Assign the variables their values
            driverUid = paramValues[0];
            passengerUid = paramValues[1];
            // Function call to dao function
            Result shortestRouteResult = this.dao.getfastestRoute(driverUid, passengerUid);
            // No fastest route exists between the driver and passenger
            if (!shortestRouteResult.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }
            // Construct the route of the response body
            JSONArray route = new JSONArray();
            Record entry; String street; boolean is_traffic; int total_time = 0, time;
            while (shortestRouteResult.hasNext()) {
                entry = shortestRouteResult.next();
                // Retrieve the information
                street = entry.get("street").asString();
                is_traffic = entry.get("is_traffic").asBoolean();
                time = entry.get("time").asInt();
                total_time = total_time + time;
                // Put the driver's information into a JSONObject
                JSONObject entryInfo = new JSONObject()
                        .put("street", street).put("is_traffic", is_traffic).put("time", time);
                // Add the entry to the route
                route.put(entryInfo);
            }
            // Construct the data of the response body
            JSONObject data = new JSONObject().put("total_time", total_time).put("route", route);
            // Set the data and status of the response
            JSONObject res = new JSONObject().put("data", data);
            // Send the response
            this.sendResponse(r, res, 200);
        } catch (Exception e) {                 // Internal error occurred
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
