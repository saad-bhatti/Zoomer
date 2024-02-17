package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;

public class Road extends Endpoint {

    /**
     * PUT /location/road/
     * @body roadName, hasTraffic
     * @return 200, 400, 404, 500 
     * Add a road into the database. If the road name already exists in the database, update the rest of the info in the
     * database with the road name.
     */
    @Override
    public void handlePut(HttpExchange r) throws IOException, JSONException {

        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"roadName", "hasTraffic"};
        Class<?> fieldClasses[] = {String.class, Boolean.class}; 
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
        }

        String roadName = body.getString("roadName");
        boolean hasTraffic = body.getBoolean("hasTraffic");

        try {
            Result getRoad = this.dao.getRoad(roadName);
            if (getRoad.hasNext()) {
                // Road found, update the info
                Result updateRoad = this.dao.updateRoad(roadName, hasTraffic);
                if (!updateRoad.hasNext()) {
                    this.sendStatus(r, 500);
                }
                this.sendResponse(r, new JSONObject(), 200);
                return;
            } else {
                // no road found, add the info as a new road
                Result createRoad = this.dao.createRoad(roadName, hasTraffic);
                if (!createRoad.hasNext()) {
                    this.sendStatus(r, 500);
                }
                this.sendResponse(r, new JSONObject(), 200);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

    /**
     * DELETE /location/road/
     * @body roadName
     * @return 200, 400, 404, 500
     * Delete a road from the database, including deleting all routes towards and from the road.
     */
    @Override
    public void handleDelete(HttpExchange r) throws IOException, JSONException {
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"roadName"};
        Class<?> fieldClasses[] = {String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }

        String roadName = body.getString("roadName");

        try {
            Result roadCheck = this.dao.getRoad(roadName);
            if (roadCheck.hasNext()) {
                Result deleteRes = this.dao.deleteRoad(roadName);
                if (!deleteRes.hasNext()) {
                    this.sendStatus(r, 500);
                    return;
                }
                this.sendResponse(r, new JSONObject(), 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
