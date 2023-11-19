package ca.utoronto.utm.mcs;

// request-related imports
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
// sql-related imports
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends Endpoint {
    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the information of the user in the database.
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
        String fields[] = {"email", "password"};
        Class<?> fieldClasses[] = {String.class, String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
        // Retrieve the arguments from the body
        String email = body.getString("email");
        String password = body.getString("password");
        // Function call to login in the user
        try {
            ResultSet rs = this.dao.loginUser(email, password);
            boolean resultHasNext = rs.next();
            if (resultHasNext) this.sendStatus(r, 200); // Login was successful
            else this.sendStatus(r, 404); // Credentials were incorrect
        }
        catch (SQLException e) { // Error occurred upon logging in
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}