package se.kau.isgb33;

import java.io.FileInputStream;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Stub {

	public static void main(String[] args) {
		
		Logger logger = LoggerFactory.getLogger(Stub.class);
		
		JFrame f = new JFrame("FAIR BNB");
		f.setSize(400, 500);
		f.setLayout(null);
		
		JTextArea area = new JTextArea();
		area.setBounds(10, 10, 365, 400);
		area.setLineWrap(true);
		
		JTextField t = new JTextField("");
		t.setBounds(10, 415, 260, 40);
		
		JButton b = new JButton("SÖK!");
		b.setBounds(275, 415, 100, 40);
		
		b.addActionListener(new ActionListener() {
				
			public void actionPerformed(ActionEvent q) {
				
				logger.info("TJOOOOOHOPP!");
				
				try (InputStream input = new FileInputStream("connection.properties")) {

					Properties prop = new Properties();
					prop.load(input);
					String connString;
					connString = prop.getProperty("db.connection_string");
					logger.info(connString);

					ConnectionString connectionString = new ConnectionString(connString);
					MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
							.build();
					MongoClient mongoClient = MongoClients.create(settings);
					MongoDatabase database = mongoClient.getDatabase(prop.getProperty("db.name"));
					MongoCollection<Document> collection = database.getCollection("test");
					
					String tillb = t.getText();
					
					Bson filter = Filters.in("amenities", tillb);
					
					AggregateIterable<Document> myDocs = collection.aggregate(Arrays.asList(
							Aggregates.project(
										Projections.include("name", "address.country", "amenities")
									),
							Aggregates.match(filter),
							Aggregates.limit(3),
							Aggregates.sort(Sorts.descending("name"))
								
							));
					
					MongoCursor<Document> iterator = myDocs.iterator();
					
					area.setText("");
					
					while(iterator.hasNext()) {
						
						Document myDoc = iterator.next();
						area.append("Objektets namn: " + myDoc.getString("name") + "\n");
						area.append("Land: " + ((Document) myDoc.get("address")).getString("country") + "\n");
						
						List <String> tilbhlista = (List <String>) myDoc.get("amenities");
						area.append("Tillbehör: ");
						
						for(String til:tilbhlista) {
							area.append(til + ", ");
						}
						
						area.append("\n\n ----------------------------- \n");
						
						
					}
					
					
					
					
					
					
					//Document myDoc = collection.find(Filters.eq("address.country", country)).first();
					//logger.info(myDoc.toJson());
					//area.setText(myDoc.toJson());

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		});
		
		
		f.add(area);
		f.add(t);
		f.add(b);
		f.setVisible(true);
		
		
		
		
		
	}

}
