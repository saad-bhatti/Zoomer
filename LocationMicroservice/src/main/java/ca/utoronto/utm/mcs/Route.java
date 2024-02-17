package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;

public class Route extends Endpoint {

    /**
     * POST /location/hasRoute/
     * @body roadName1, roadName2, hasTraffic, time
     * @return 200, 400, 404, 500 
     * Create a connection from a road to another; making
     * a relationship in Neo4j.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {

        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"roadName1", "roadName2", "hasTraffic", "time"};
        Class<?> fieldClasses[] = {String.class, String.class, Boolean.class, Integer.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
            
        String road1 = body.getString("roadName1");
        String road2 = body.getString("roadName2");
        Boolean is_traffic = body.getBoolean("hasTraffic");
        int time = body.getInt("time");

        try {
            Result result = this.dao.createRoute(road1, road2, time, is_traffic);
            if (!result.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }
            this.sendResponse(r, new JSONObject(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

    /**
     * DELETE /location/route/
     * @body roadName1, roadName2
     * @return 200, 400, 404, 500 
     * Disconnect a road with another; remove the
     * relationship in Neo4j.
     */

    @Override
    public void handleDelete(HttpExchange r) throws IOException, JSONException {
        
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"roadName1", "roadName2"};
        Class<?> fieldClasses[] = {String.class, String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }

        String road1 = body.getString("roadName1");
        String road2 = body.getString("roadName2");
        
        try {
            Result result = this.dao.deleteRoute(road1, road2);
            if (!result.hasNext()) {
                this.sendStatus(r, 500);
                return;
            }
            int numDeletedRoutes = result.next().get("numDeletedRoutes").asInt();
            if (numDeletedRoutes == 0) {
                this.sendStatus(r, 404);
                return;
            }
            this.sendStatus(r, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
