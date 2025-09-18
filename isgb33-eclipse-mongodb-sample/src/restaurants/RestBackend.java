package restaurants;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestBackend {

	private static final Logger logger = LoggerFactory.getLogger(RestBackend.class);

	private static MongoCollection<Document> restaurantCollection;

	public static void main(String[] args) throws Exception {
		initMongo();
		startServer();
	}

	private static void initMongo() throws Exception {
		Properties prop = new Properties();

		try (InputStream input = new FileInputStream("connection.properties")) {
			prop.load(input);
		}
		String connString = prop.getProperty("db.connection_string");
		String dbName = prop.getProperty("db.name");
		ConnectionString connectionString = new ConnectionString(connString);
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase database = mongoClient.getDatabase(dbName);
		restaurantCollection = database.getCollection("restaurants");
		logger.info("Connected to MongoDB, using database '{}'", dbName);
	}

	private static void startServer() {
		Javalin app = Javalin.create().start(4567);

		app.get("/restaurant/{name}", RestBackend::getRestaurantByName);
		app.post("/restaurant", RestBackend::createRestaurant);
		app.get("/cuisine", RestBackend::getAllCuisines);
		app.get("/restaurant/cuisine/{cuisine}", RestBackend::getRestaurantsByCuisine);
	}

	private static void getRestaurantByName(Context ctx) {
		String inputName = ctx.pathParam("name");
		String name = capitalizeFully(inputName.toLowerCase(Locale.ROOT));
		Document doc = restaurantCollection.find(eq("name", name)).first();
		if (doc != null) {
			doc.remove("_id");
			JsonObject response = JsonParser.parseString(doc.toJson()).getAsJsonObject();
			ctx.status(200).contentType("application/json").result(response.toString());
		} else {
			ctx.status(404).contentType("application/json").result(jsonError("Restaurant not found.").toString());
		}
	}

	private static void createRestaurant(Context ctx) {
		try {
			String requestBody = ctx.body();
			Document newDoc = Document.parse(requestBody);

			restaurantCollection.insertOne(newDoc);

			ctx.status(202).contentType("application/json").result(jsonMessage("Restaurant accepted.").toString());

		} catch (Exception e) {
			logger.error("Insert failed: {}", e.getMessage());
			ctx.status(500).contentType("application/json")
					.result(jsonError("Failed to insert restaurant.").toString());
		}
	}

	private static void getAllCuisines(Context ctx) {
		JsonArray cuisinesArray = new JsonArray();

		for (String cuisine : restaurantCollection.distinct("cuisine", String.class)) {
			cuisinesArray.add(cuisine);
		}

		JsonObject response = new JsonObject();
		response.add("cuisines", cuisinesArray);

		ctx.status(200).contentType("application/json").result(response.toString());
	}

	private static void getRestaurantsByCuisine(Context ctx) {
		String inputCuisine = ctx.pathParam("cuisine");
		String cuisine = capitalizeFully(inputCuisine.toLowerCase(Locale.ROOT));

		int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(10);

		FindIterable<Document> results = restaurantCollection.find(eq("cuisine", cuisine)).projection(include("name"))
				.limit(limit);

		JsonArray array = new JsonArray();

		for (Document doc : results) {
			doc.remove("_id");
			array.add(JsonParser.parseString(doc.toJson()).getAsJsonObject());
		}

		JsonObject response = new JsonObject();
		response.add("restaurants", array);

		ctx.status(200).contentType("application/json").result(response.toString());
	}

	private static String capitalizeFully(String input) {
		String[] words = input.trim().split("\\s+");
		StringBuilder result = new StringBuilder();

		for (String word : words) {
			if (!word.isEmpty()) {
				char first = Character.toUpperCase(word.charAt(0));
				String rest = word.substring(1).toLowerCase();

				result.append(first).append(rest).append(" ");
			}
		}

		return result.toString().trim();
	}

	private static JsonObject jsonMessage(String msg) {
		JsonObject obj = new JsonObject();
		obj.addProperty("message", msg);
		return obj;
	}

	private static JsonObject jsonError(String error) {
		JsonObject obj = new JsonObject();
		obj.addProperty("error", error);
		return obj;
	}
}