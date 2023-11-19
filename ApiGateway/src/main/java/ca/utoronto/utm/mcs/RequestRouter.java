package ca.utoronto.utm.mcs;

// receiving-request-related
import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

// sending-request-related
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

// returning-request-related
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class RequestRouter implements HttpHandler {
	// Strings representing the host of microservice
	public String locationHost, userHost, tripHost;
	// Key = status code. Value = corresponding message
	public HashMap<Integer, String> errorMap;
	// Key = endpoint, Value = supported methods for that endpoint
	public HashMap<String, ArrayList<String>> locationServiceMap, userServiceMap, tripServiceMap;

	public RequestRouter() {
		locationHost = "http://locationmicroservice:8000";
		userHost = "http://usermicroservice:8000";
		tripHost = "http://tripinfomicroservice:8000";
		errorMap = new HashMap<>() {{
			put(200, "OK"); put(400, "BAD REQUEST"); put(401, "UNAUTHORIZED"); put(404, "NOT FOUND");
			put(405, "METHOD NOT ALLOWED"); put(409, "CONFLICT"); put(500, "INTERNAL SERVER ERROR");
		}};
		locationServiceMap = new HashMap<>() {{
			put("user", new ArrayList<>() {{add("PUT");add("DELETE");}});
			put(":uid", new ArrayList<>() {{add("GET");add("PATCH");}});
			put("road", new ArrayList<>() {{add("PUT");add("DELETE");}});
			put("hasRoute", new ArrayList<>() {{add("POST");}});
			put("route", new ArrayList<>() {{add("DELETE");}});
			put("nearbyDriver", new ArrayList<>() {{add("GET");}});
			put("navigation", new ArrayList<>() {{add("GET");}});
		}};
		userServiceMap = new HashMap<>() {{
			put(":uid", new ArrayList<>() {{add("GET");add("PATCH");}});
			put("register", new ArrayList<>() {{add("POST");}});
			put("login", new ArrayList<>() {{add("POST");}});
		}};
		tripServiceMap = new HashMap<>() {{
			put("request", new ArrayList<>() {{add("POST");}});
			put("confirm", new ArrayList<>() {{add("POST");}});
			put(":id", new ArrayList<>() {{add("PATCH");}});
			put("passenger/:id", new ArrayList<>() {{add("GET");}});
			put("driver/:id", new ArrayList<>() {{add("GET");}});
			put("driverTime/:id", new ArrayList<>() {{add("GET");}});
		}};
	}

	//////////////////////////// MAIN HANDLE FUNCTION ////////////////////////////
	@Override
	public void handle(HttpExchange r) throws IOException {
		r.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // For CORS
		// The request is related to CORS
		if (r.getRequestMethod().equals("OPTIONS")) {
			this.handleCors(r);
			return;
		}
		// Direct the requests to the corresponding microservice
		String url = r.getRequestURI().toString();
		try {
			if (url.startsWith("/location/")) this.handleLocation(r);
			else if (url.startsWith("/user/")) this.handleUser(r);
			else this.handleTrip(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//////////////////////////// HANDLE METHODS ////////////////////////////
	/** Function to handle requests with method OPTIONS **/
	public void handleCors(HttpExchange r) throws IOException {
		r.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
		r.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
		r.sendResponseHeaders(204, -1);
	}

	//////////////////////////// HANDLE ENDPOINTS ////////////////////////////
	/** Function to route requests to location microservice **/
	public void handleLocation(HttpExchange r) throws IOException, JSONException {
		/** PART A: Check the endpoint and method are valid **/
		String endpoint, url = r.getRequestURI().toString();
		if (url.endsWith("/")) url = url.substring(0, url.length() - 1); // Remove the trailing '/'
		// Check the endpoint is valid
		try {
			endpoint = url.split("/location/")[1]; // Get the endpoint
			boolean isValidEndpoint = locationServiceMap.containsKey(endpoint);	// Check if the endpoint is in the hashmap
			if (!isValidEndpoint) endpoint = ":uid"; // Endpoint is not in the hashmap, so it must be the uid
		}
		catch (Exception e) {
			this.sendStatus(r, 400);	// No endpoint was provided
			return;
		}
		// Check the method is valid
		ArrayList<String> validMethods = locationServiceMap.get(endpoint);
		boolean isValidMethod = validMethods.contains(r.getRequestMethod());
		if (!isValidMethod) {
			this.sendStatus(r, 400);	// Invalid method provided for the endpoint
			return;
		}

		// PART B: Prepare the parameters to route the request
		String method = r.getRequestMethod();
		String uri = locationHost + url;

		// PART C: Reroute the request
		HttpResponse<String> res;
		try {
			res = routeRequest(method, uri, r.getRequestBody());
		} catch (Exception e) {
			e.printStackTrace();
			this.sendStatus(r, 400);	// The provided body has an error
			return;
		}

		// PART D: Return the response of the routed request
		JSONObject noDataBody = new JSONObject().put("status", "OK");
		if (res.statusCode() != 200) this.sendStatus(r, res.statusCode());
		else if (res.body() == noDataBody.toString()) this.sendStatus(r, res.statusCode());
		else this.sendResponse(r, new JSONObject(res.body()), res.statusCode());
	}

	/** Function to route requests to user microservice **/
	public void handleUser(HttpExchange r) throws IOException, InterruptedException, JSONException {
		/** PART A: Check the endpoint and method are valid **/
		String endpoint, url = r.getRequestURI().toString();
		if (url.endsWith("/")) url = url.substring(0, url.length() - 1); // Remove the trailing '/'
		// Check the endpoint is valid
		try {
			endpoint = url.split("/user/")[1];	// Get the endpoint
			boolean isValidEndpoint = userServiceMap.containsKey(endpoint);	// Check if the endpoint is in the hashmap
			if (!isValidEndpoint) endpoint = ":uid";	// Endpoint is not in the hashmap, so it must be the uid
		}
		catch (Exception e) {
			this.sendStatus(r, 400);	// No endpoint was provided
			return;
		}
		// Check the method is valid
		ArrayList<String> validMethods = userServiceMap.get(endpoint);
		boolean isValidMethod = validMethods.contains(r.getRequestMethod());
		if (!isValidMethod) {
			this.sendStatus(r, 400);	// Invalid method provided for the endpoint
			return;
		}

		// PART B: Prepare the parameters to route the request
		String method = r.getRequestMethod();
		String uri = userHost + url;

		// PART C: Reroute the request
		HttpResponse<String> res;
		try {
			res = routeRequest(method, uri, r.getRequestBody());
		} catch (Exception e) {
			e.printStackTrace();
			this.sendStatus(r, 400);	// The provided body has an error
			return;
		}

		// PART D: Return the response of the routed request
		JSONObject noDataBody = new JSONObject().put("status", "OK");
		if (res.statusCode() != 200) this.sendStatus(r, res.statusCode());
		else if (res.body() == noDataBody.toString()) this.sendStatus(r, res.statusCode());
		else this.sendResponse(r, new JSONObject(res.body()), res.statusCode());
	}

	/** Function to route requests to trip microservice **/
	public void handleTrip(HttpExchange r) throws IOException, InterruptedException, JSONException {
		/** PART A: Check the endpoint and method are valid **/
		String endpoint, url = r.getRequestURI().toString();
		if (url.endsWith("/")) url = url.substring(0, url.length() - 1); // Remove the trailing '/'
		// Check the endpoint is valid
		try {
			endpoint = url.split("/trip/")[1]; // Get the endpoint
			boolean isValidEndpoint = tripServiceMap.containsKey(endpoint);	// Check if the endpoint is in the hashmap
			if (!isValidEndpoint) {	// The endpoint is not request/confirm
				if (endpoint.startsWith("passenger/")) endpoint = "passenger/:id";
				else if (endpoint.startsWith("driver/")) endpoint = "driver/:id";
				else if (endpoint.startsWith("driverTime/")) endpoint = "driverTime/:id";
				else endpoint = ":id";
			}
		}
		catch (Exception e) {
			this.sendStatus(r, 400);	// No endpoint was provided
			return;
		}
		// Check the method is valid
		ArrayList<String> validMethods = tripServiceMap.get(endpoint);
		boolean isValidMethod = validMethods.contains(r.getRequestMethod());
		if (!isValidMethod) {
			this.sendStatus(r, 400);	// Invalid method provided for the endpoint
			return;
		}

		// PART B: Prepare the parameters to route the request
		String method = r.getRequestMethod();
		String uri = tripHost + url;

		// PART C: Reroute the request
		HttpResponse<String> res;
		try {
			res = routeRequest(method, uri, r.getRequestBody());
		} catch (Exception e) {
			e.printStackTrace();
			this.sendStatus(r, 400);	// The provided body has an error
			return;
		}

		// PART D: Return the response of the routed request
		JSONObject noDataBody = new JSONObject().put("status", "OK");
		if (res.statusCode() != 200) this.sendStatus(r, res.statusCode());
		else if (res.body() == noDataBody.toString()) this.sendStatus(r, res.statusCode());
		else this.sendResponse(r, new JSONObject(res.body()), res.statusCode());
	}

	//////////////////////////// HELPER FUNCTIONS ////////////////////////////
	/** Create and send a request with the provided specifications **/
	public static HttpResponse<String> routeRequest(String method, String uri, InputStream reqBody)
			throws IOException, InterruptedException {
		Supplier<InputStream> streamSupplier = () -> reqBody;
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.method(method, HttpRequest.BodyPublishers.ofInputStream(streamSupplier))
				.build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	/** Write the response to the response's body **/
	public void writeOutputStream(HttpExchange r, String response) throws IOException {
		OutputStream os = r.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	/** Send a response with obj as the body and with statusCode **/
	public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
		obj.put("status", errorMap.get(statusCode));
		String response = obj.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}

	/** Send a response with statusCode **/
	public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
		JSONObject res = new JSONObject();
		res.put("status", errorMap.get(statusCode));
		String response = res.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}
}
