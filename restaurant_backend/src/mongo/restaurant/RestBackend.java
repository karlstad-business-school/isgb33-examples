package mongo.restaurant;



import static spark.Spark.get;
import static spark.Spark.post;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.text.WordUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;



public class RestBackend {

	public static void main(String[] args) throws IOException {
		String connString;
		Logger logger = LoggerFactory.getLogger(RestBackend.class);
		InputStream input = new FileInputStream("connection.properties");

			Properties prop = new Properties();
			prop.load(input);
			connString = prop.getProperty("db.connection_string");
			logger.info(connString);


			ConnectionString connectionString = new ConnectionString(connString);
			MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.build();
			MongoClient mongoClient = MongoClients.create(settings);
			MongoDatabase database = mongoClient.getDatabase(prop.getProperty("db.name"));
			logger.info(prop.getProperty("db.name"));


	      
	      get("/restaurant/:name", (req,res)->{
		      MongoCollection<Document> collection = database.getCollection("restaurants");
		      String filter=req.params("name").toLowerCase();
		      filter=WordUtils.capitalizeFully(filter);
		      logger.info("Filtering Restaurants for name: " + filter);
	    	  Document myDoc = collection.find(Filters.eq("name", filter)).first();
	    	  if (myDoc != null) {
	    	      myDoc.remove("_id");
	    	    
	    	  } else {
	    		  res.status(404);
	    		  return ("<html><body><h1>Title not found. </h1></body></html>");
	    	  }
	          return myDoc.toJson();
	      });
	    post("/restaurant", (request, response) -> {
	    	    response.type("application/json");
	    	    try {
	    	    	MongoCollection<Document> collection = database.getCollection("restaurants");
	               collection.insertOne(new Document(Document.parse(request.body())));
	 		      logger.info("Adding Restaurant ");

	                       
	            } catch (MongoException me) {
	                System.err.println("Unable to insert due to an error: " + me);
	            }
	    	    
	    	    response.status(202);
	    		  return ("<html><body><h1>Restaurant Accepted. </h1></body></html>");
	    	});
	      get("/cuisine", (req,res)->{
		      MongoCollection<Document> collection = database.getCollection("restaurants");
		     logger.info("Finding all different cuisines ");
	    	  DistinctIterable<String> col = collection.distinct("cuisine", String.class);
	    	  JsonArray list = new JsonArray();
	    	  JsonObject myDoc = new JsonObject();
	    	  if (col != null) {
	    		  for (String tmp : col) { 
	    			  list.add(tmp);	 
	    		  }
	    	  myDoc.add("cuisines", list);	  
	    	  } else {
	    		  res.status(404);
	    		  return ("<html><body><h1>No choices of cuisine for you!! </h1></body></html>");
	    	  }
	          return myDoc;
	      });
	      get("/restaurant/cuisine/:cuisine", (req,res)->{
		      MongoCollection<Document> collection = database.getCollection("restaurants");
		      String filter=req.params("cuisine").toLowerCase();
		      int limit=10;
		      if (req.queryParams("limit")!=null) {
		    	  try {
		    	      limit=Integer.parseInt(req.queryParams("limit"));
		    	  } catch (NumberFormatException e) { }
		      }
		      filter=WordUtils.capitalizeFully(filter);
		      logger.info("Finding restaurants serving: " + filter);
		      MongoCursor<Document> cursor = collection.find(Filters.eq("cuisine", filter)).projection(Projections.include("name")).limit(limit).iterator();
	    	  JsonArray ja=new JsonArray();
		      Document tmp;
		      try {
		          while (cursor.hasNext()) {
		        	  tmp=cursor.next();
		        	  tmp.remove("_id");
		        	  ja.add(tmp.toJson());
		          }
		      } finally {
		          cursor.close();
		      }
		      JsonObject response=new JsonObject();
			     response.add("restaurants",ja);
              return response;
	      });
	}

}
