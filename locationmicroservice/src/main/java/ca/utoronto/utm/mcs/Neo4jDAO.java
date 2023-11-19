package ca.utoronto.utm.mcs;

// neo4j-related import
import org.neo4j.driver.*;
// environmental-variable-related import
import io.github.cdimascio.dotenv.Dotenv;

public class Neo4jDAO {
    private final Session session;
    private final Driver driver;
    private final String username = "neo4j";
    private final String password = "123456";

    public Neo4jDAO() {
        Dotenv dotenv = Dotenv.load();
        String addr = dotenv.get("NEO4J_ADDR");
        String uriDb = "bolt://" + addr + ":7687";
        this.driver = GraphDatabase.driver(uriDb, AuthTokens.basic(this.username, this.password));
        this.session = this.driver.session();
    }

    public Result addUser(String uid, boolean is_driver) {
        String query = "CREATE (n: user {uid: '%s', is_driver: %b, longitude: 0, latitude: 0, street: ''}) RETURN n";
        query = String.format(query, uid, is_driver);
        return this.session.run(query);
    }

    public Result deleteUser(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) DETACH DELETE n RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserLocationByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n.longitude, n.latitude, n.street";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result updateUserIsDriver(String uid, boolean isDriver) {
        String query = "MATCH (n:user {uid: '%s'}) SET n.is_driver = %b RETURN n";
        query = String.format(query, uid, isDriver);
        return this.session.run(query);
    }

    public Result updateUserLocation(String uid, double longitude, double latitude, String street) {
        String query = "MATCH(n: user {uid: '%s'}) SET n.longitude = %f, n.latitude = %f, n.street = \"%s\" RETURN n";
        query = String.format(query, uid, longitude, latitude, street);
        return this.session.run(query);
    }

    public Result getRoad(String roadName) {
        String query = "MATCH (n :road) where n.name='%s' RETURN n";
        query = String.format(query, roadName);
        return this.session.run(query);
    }

    public Result createRoad(String roadName, boolean has_traffic) {
        String query = "CREATE (n: road {name: '%s', has_traffic: %b}) RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result deleteRoad(String roadName) {
        String query = "MATCH (n: road {name: '%s'}) DETACH DELETE n RETURN n";
        query = String.format(query, roadName);
        return this.session.run(query);
    }

    public Result updateRoad(String roadName, boolean has_traffic) {
        String query = "MATCH (n:road {name: '%s'}) SET n.has_traffic = %b RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result createRoute(String roadname1, String roadname2, int travel_time, boolean has_traffic) {
        String query = "MATCH (r1:road {name: '%s'}), (r2:road {name: '%s'}) CREATE (r1) -[r:ROUTE_TO {travel_time: %d, has_traffic: %b}]->(r2) RETURN type(r)";
        query = String.format(query, roadname1, roadname2, travel_time, has_traffic);
        return this.session.run(query);
    }

    public Result deleteRoute(String roadname1, String roadname2) {
        String query = "MATCH (r1:road {name: '%s'})-[r:ROUTE_TO]->(r2:road {name: '%s'}) DELETE r RETURN COUNT(r) AS numDeletedRoutes";
        query = String.format(query, roadname1, roadname2);
        return this.session.run(query);
    }

    public Result getNearbyDrivers(String uid, int radius) {
        String query = """
             MATCH (driver:user {is_driver: true})
             MATCH (user:user {uid: "%s"})
             WHERE
                driver <> user
                AND
                ceil(sqrt((driver.longitude - user.longitude)^2 + (driver.latitude - user.latitude)^2)) <= %d
             RETURN driver.uid AS uid, driver.longitude AS longitude, driver.latitude AS latitude, driver.street AS street
             ORDER BY uid
        """;
        query = String.format(query, uid, radius);
        return this.session.run(query);
    }

    public Result getfastestRoute(String driverUid, String passengerUid) {
        String query = """
             MATCH (r1:road)
             MATCH (driver:user {uid: "%s"})
             WHERE r1.name = driver.street
             MATCH (r2:road)
             MATCH (passenger:user {uid: "%s"})
             WHERE r2.name = passenger.street
             MATCH p = (r1:road {name: driver.street})-[r:ROUTE_TO*]->(r2:road {name: passenger.street})
             WHERE NONE (road IN nodes(p) WHERE size([current_road IN nodes(p) WHERE road = current_road]) > 1 )
             WITH relationships(p) AS data, [route IN relationships(p) | route.travel_time] AS travel_time
             ORDER BY reduce(total_time = 0, time in travel_time | total_time + time) ASC
             LIMIT 1
             UNWIND data as route
             RETURN startNode(route).name AS street, route.travel_time AS time, startNode(route).has_traffic AS is_traffic
        """;
        query = String.format(query, driverUid, passengerUid);
        return this.session.run(query);
    }
} 