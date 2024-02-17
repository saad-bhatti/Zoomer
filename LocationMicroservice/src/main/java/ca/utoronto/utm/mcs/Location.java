package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;

public class Location extends Endpoint {

    /**
     * GET /location/:uid
     * @param uid
     * @return 200, 400, 404, 500
     * Get the current location for a certain user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 3 || params[2].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String uid = params[2];
            Result result = this.dao.getUserLocationByUid(uid);
            if (result.hasNext()) {
                JSONObject res = new JSONObject();

                Record user = result.next();
                Double longitude = user.get("n.longitude").asDouble();
                Double latitude = user.get("n.latitude").asDouble();
                String street = user.get("n.street").asString();

                JSONObject data = new JSONObject();
                data.put("longitude", longitude);
                data.put("latitude", latitude);
                data.put("street", street);
                res.put("status", "OK");
                res.put("data", data);

                this.sendResponse(r, res, 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

    /**
     * PATCH /location/:uid
     * @param uid
     * @body longitude, latitude, street
     * @return 200, 400, 404, 500
     * Update the user’s location information
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {

        String params[] = r.getRequestURI().toString().split("/");
        if (params.length != 3 || params[2].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"latitude", "longitude", "street"};
        Class<?> fieldClasses[] = {Double.class, Double.class, String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }

        String uid = params[2];
        double lat = body.getDouble("latitude");
        double longi = body.getDouble("longitude");
        String street = body.getString("street");

        try {
            Result res = this.dao.getUserByUid(uid);
            if (res.hasNext()) {
                Result update = this.dao.updateUserLocation(uid, longi, lat, street);
                if (update.hasNext()) {
                    this.sendStatus(r, 200);
                } else {
                    this.sendStatus(r, 500);
                }
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
