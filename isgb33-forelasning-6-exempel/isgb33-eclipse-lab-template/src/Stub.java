import static com.mongodb.client.model.Filters.eq;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Stub {

    private static final Logger logger = LoggerFactory.getLogger(Stub.class);

    private static MongoCollection<Document> myCollection;

    public static void main(String[] args) throws Exception {
        initMongo();
        buildGui();
        
        //startServer();
    }
    
    private static void buildGui() {
    	JFrame f = new JFrame("FAIR BNB");
    	f.setSize(400,500);
    	f.setLayout(null);
    	
    	JTextArea area = new JTextArea();
    	area.setBounds(10,10,365,400);
    	area.setLineWrap(true);
    	
    	JTextField t = new JTextField("");
    	t.setBounds(10,415,260,40);
    	
    	JButton b = new JButton("SÖK");
    	b.setBounds(275, 415, 100, 40);
    	
    	b.addActionListener(new ActionListener() {
    		
    		public void actionPerformed(ActionEvent q) {
    			
    			String land = t.getText();
    			
    			AggregateIterable<Document> myDocs = myCollection.aggregate(Arrays.asList(
    					Aggregates.project(
    								Projections.include("name", "address.country", "amenities")
    							),
    					Aggregates.match(Filters.in("amenities", land)),
    					Aggregates.sort(Sorts.descending("name")),
    					Aggregates.limit(5)
    					
    					));
    			area.setText("");
    			
    			MongoCursor<Document> iterator = myDocs.iterator();
    			
    			while(iterator.hasNext()) {
    				Document myDoc = iterator.next();
    				area.append("Objektets namn: " + myDoc.getString("name") + "\n");
    				area.append("Land: " + ((Document) myDoc.get("address")).getString("country") + "\n");
    				
    				List<String> tbhLista = ((List <String>) myDoc.get("amenities"));
    				
    				area.append("Tillbehör: ");
    				
    				for(String til:tbhLista) {
    					area.append(til + ", ");
    				}
    				
    				area.append("\n");
    				
    				area.append("\n*********************************************************\n\n");
    			}
    			//Document myDoc = myCollection.find(Filters.eq("address.country", land)).first();
    			
    			//area.setText(myDoc.toString());
    			
    			logger.info("TJO!");
    		}
    	});
    	
    	f.add(area);
    	f.add(t);
    	f.add(b);
    	f.setVisible(true);
    }

    private static void initMongo() throws Exception {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream("connection.properties")) {
            prop.load(input);
        }

        String connString = prop.getProperty("db.connection_string");
        String dbName = prop.getProperty("db.name");

        ConnectionString connectionString = new ConnectionString(connString);

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();

        MongoClient mongoClient = MongoClients.create(settings);

        MongoDatabase database = mongoClient.getDatabase(dbName);

        myCollection = database.getCollection("listingsAndReviews");
        
        //Document myDoc = myCollection.find(Filters.eq("address.country", "Spain")).first();
        
        //logger.info(myDoc.toString());

        //logger.info("Connected to MongoDB, using database '{}'", dbName);
    }

    private static void startServer() {
        Javalin app = Javalin.create().start(4567);

        app.get("/foo/{name}", Stub::getFooByName);
       
    }

    private static void getFooByName(Context ctx) {
        String inputName = ctx.pathParam("name");
        String name = capitalizeFully(inputName.toLowerCase(Locale.ROOT));

        Document doc = myCollection.find(eq("name", name)).first();

        if (doc != null) {
            doc.remove("_id");
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(doc.toJson());
        } else {
            ctx.status(404);
            ctx.contentType("application/json");
            ctx.result(jsonError("Foo not found.").toString());
        }
    }

  
    private static String capitalizeFully(String input) {
        String[] words = input.trim().split("\\s+");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                char first = Character.toUpperCase(word.charAt(0));
                String rest = word.substring(1).toLowerCase();

                result.append(first);
                result.append(rest);
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

 

    private static JsonObject jsonError(String error) {
        JsonObject obj = new JsonObject();
        obj.addProperty("error", error);
        return obj;
    }
}