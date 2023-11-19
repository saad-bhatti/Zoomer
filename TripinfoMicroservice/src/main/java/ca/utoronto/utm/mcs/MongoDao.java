package ca.utoronto.utm.mcs;


import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import io.github.cdimascio.dotenv.Dotenv;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDao {

	public MongoCollection<Document> collection;
	public MongoDatabase db;
	private final String username = "root";
	private final String password = "123456";
	private final String dbName = "trips";
	private final String collectionName = "trips";

	public MongoDao() {
		try {
			Dotenv dotenv = Dotenv.load();
			String addr = dotenv.get("MONGODB_ADDR");
			String uriDb = String.format("mongodb://%s:%s@%s:27017", username, password, addr);
			MongoClient mongoClient = MongoClients.create(uriDb);
			this.db = mongoClient.getDatabase(this.dbName);
			this.collection = this.db.getCollection(this.collectionName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String confirmTrip(String driverUid, String passengerUid, Integer startTime){
		String oid = new ObjectId().toString();
		Document trip = new Document()
			.append("_id", oid)
			.append("driver", driverUid)
			.append("passenger", passengerUid)
			.append("startTime", startTime);
		this.collection.insertOne(trip);
		return oid;
	}

	public MongoCursor<Document> getTripById(String tripId){
		return this.collection.find(eq("_id", tripId)).cursor();
	}

	public MongoCursor<Document> getDriverTrips(String driverUid) {
		return this.collection.find(eq("driver", driverUid)).cursor();
	}

	public MongoCursor<Document> getPassengerTrips(String passengerUid) {
		return this.collection.find(eq("passenger", passengerUid)).cursor();
	}

	public UpdateResult updateTripInfo(
			String tripId, Double distance, Integer endTime, Integer timeElapsed,
			Double discount, Double totalCost, Double driverPayout)
	{
		return this.collection.updateOne(
			Filters.eq("_id", tripId),
			Updates.combine(
					Updates.set("distance", distance), Updates.set("endTime", endTime),
					Updates.set("timeElapsed", timeElapsed), Updates.set("discount", discount),
					Updates.set("totalCost", totalCost), Updates.set("driverPayout", driverPayout)
			)
		);
	}

}
