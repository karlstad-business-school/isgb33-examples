package se.kau.isgb33;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class Stub {

	public static void main(String[] args) {
		String connString;
		Logger logger = LoggerFactory.getLogger(Stub.class);
		try (InputStream input = new FileInputStream("connection.properties")) {

			Properties prop = new Properties();
			prop.load(input);
			connString = prop.getProperty("db.connection_string");
			logger.info(connString);

			ConnectionString connectionString = new ConnectionString(connString);
			MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.build();
			MongoClient mongoClient = MongoClients.create(settings);
			MongoDatabase database = mongoClient.getDatabase(prop.getProperty("db.name"));
			MongoCollection<Document> collection = database.getCollection("movies");
			Document myDoc = collection.find(Filters.eq("title", "Peter Pan")).first();
			logger.info(myDoc.toJson());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
