package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.*;

public class User extends Endpoint {
	
    /**
     * GET /user/:uid
     * @param uid
     * @return 200, 400, 404, 500
     * Get basic information of user with the given uid
     */

	@Override
	public void handleGet(HttpExchange r) throws IOException, JSONException {
		
		// check if request url isn't malformed
		String[] splitUrl = r.getRequestURI().getPath().split("/");
		if (splitUrl.length != 3) {
			this.sendStatus(r, 400);
			return;
		}

		// check if uid given is integer, return 400 if not
		String uidString = splitUrl[2];
        int uid;
		try {
			uid = Integer.parseInt(uidString);
		} catch (Exception e) {
            e.printStackTrace();
			this.sendStatus(r, 400);
			return;
		}

		// make query and get required data, return 500 if error
		ResultSet rs;
		boolean resultHasNext;
		try {
			rs = this.dao.getUserData(uid);
			resultHasNext = rs.next();
		} 
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

		// check if user was found, return 404 if not found
		if (!resultHasNext) {
			this.sendStatus(r, 404);
			return;
		}

		// get data
		String name;
		String email;
		int rides;
		Boolean isDriver;
		try {
			name = rs.getString("name");
			email = rs.getString("email");
			rides = rs.getInt("rides");
			isDriver = rs.getBoolean("isdriver");
		}
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

		// making the response
		JSONObject resp = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("email", email);
		data.put("rides", rides);
		data.put("isDriver", isDriver);
		resp.put("data", data);

		this.sendResponse(r, resp, 200);
	}

    /**
     * PATCH /user/:uid
     * 
     * @param uid
     * @body at least one of email, password, rides, or isDriver
     * @return 200, 400, 404, 500
     * Update users information
     */

	@Override
	public void handlePatch(HttpExchange r) throws IOException, JSONException {

		// check if request url isn't malformed
		String[] splitUrl = r.getRequestURI().getPath().split("/");
		if (splitUrl.length != 3) {
			this.sendStatus(r, 400);
			return;
		}

		// check if uid given is integer, return 400 if not
		String uidString = splitUrl[2];
        int uid;
		try {
			uid = Integer.parseInt(uidString);
		} catch (Exception e) {
            e.printStackTrace();
			this.sendStatus(r, 400);
			return;
		}

		// make query to check if user with given uid exists, return 500 if error
		ResultSet rs1;
		boolean resultHasNext;
		try {
			rs1 = this.dao.getUsersFromUid(uid);
			resultHasNext = rs1.next();
		} 
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

		// check if user with given uid exists, return 404 if not
		if (!resultHasNext) {
			this.sendStatus(r, 404);
			return;
		}

		String body = Utils.convert(r.getRequestBody());
		JSONObject deserialized = new JSONObject(body);
		
		String email = null;
		String name = null;
		String password = null;
		Boolean isDriver = null;
		Integer rides = null;

		// check what values are present
		if (deserialized.has("email")) { 
			if (deserialized.get("email").getClass() != String.class) {
				this.sendStatus(r, 400);
				return;
			}
			email = deserialized.getString("email");
		}
		if (deserialized.has("name")) {
			if (deserialized.get("name").getClass() != String.class) {
				this.sendStatus(r, 400);
				return;
			}
			name = deserialized.getString("name");
		}
		if (deserialized.has("password")) {
			if (deserialized.get("password").getClass() != String.class) {
				this.sendStatus(r, 400);
				return;
			}
			password = deserialized.getString("password");
		}
		if (deserialized.has("isDriver")) { 
			if (deserialized.get("isDriver").getClass() != Boolean.class) {
				this.sendStatus(r, 400);
				return;
			}
			isDriver = deserialized.getBoolean("isDriver");
		}
		if (deserialized.has("rides")) {
			if (deserialized.get("rides").getClass() != Integer.class) {
				this.sendStatus(r, 400);
				return;
			}
			rides = deserialized.getInt("rides");
		}

		// if all the variables are still null then there's no variables in request so retrun 400
		if (email == null && name == null && password == null && isDriver == null && rides == null) {
			this.sendStatus(r, 400);
			return;
		}

		// update db, return 500 if error
		try {
			this.dao.updateUserAttributes(uid, email, password, name, rides, isDriver);
		}
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

		// return 200 if everything is updated without error
		this.sendStatus(r, 200);
	}
}
