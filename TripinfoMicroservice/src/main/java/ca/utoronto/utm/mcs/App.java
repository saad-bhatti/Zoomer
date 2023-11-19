package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App {
    static int PORT = 8000;
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/trip/request", new Request());
        server.createContext("/trip/confirm", new Confirm());
        server.createContext("/trip", new Trip());
        server.createContext("/trip/passenger", new Passenger());
        server.createContext("/trip/driver", new Driver());
        server.createContext("/trip/driverTime", new Drivetime());

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
