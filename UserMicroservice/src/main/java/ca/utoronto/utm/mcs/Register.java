package ca.utoronto.utm.mcs;

// request-related imports
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
// sql-related imports
import java.io.IOException;
import java.sql.SQLException;

public class Register extends Endpoint {
    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
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
        String fields[] = {"name", "email", "password"};
        Class<?> fieldClasses[] = {String.class, String.class, String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
        // Retrieve the arguments from the body
        String name = body.getString("name");
        String email = body.getString("email");
        String password = body.getString("password");
        // Function call to register the user
        try {
            this.dao.registerUser(name, email, password);
            this.sendStatus(r, 200); // Successful registration
        }
        catch (SQLException e) { // Error upon registration
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}