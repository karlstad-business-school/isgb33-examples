package se.kau.isgb33;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

public class Stub {

	public static void main(String[] args) {
		
				
		JFrame f = new JFrame("BairBNB");
		f.setSize(400,500);
		f.setLayout(null);
		
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setBounds(10,10,365,400);
		
		JTextField t = new JTextField("");
		t.setBounds(10, 415, 260, 40);
		
		JButton b = new JButton("sök");
		b.setBounds(275,415,100,40);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent q) {
				
				String connString;
				//Logger logger = LoggerFactory.getLogger(Stub.class);
				try (InputStream input = new FileInputStream("connection.properties")) {

					Properties prop = new Properties();
					prop.load(input);
					connString = prop.getProperty("db.connection_string");
					//logger.info(connString);

					ConnectionString connectionString = new ConnectionString(connString);
					MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
							.build();
					MongoClient mongoClient = MongoClients.create(settings);
					MongoDatabase database = mongoClient.getDatabase(prop.getProperty("db.name"));
					MongoCollection<Document> collection = database.getCollection("airbnb");
					
					//Document myDoc = collection.find(Filters.eq("address.country", t.getText())).first();
					Bson filter = Filters.in("amenities", t.getText()); //Filters.empty();
					AggregateIterable<Document> myDocs = collection.aggregate(Arrays.asList(
							Aggregates.match(filter),
							Aggregates.project(
									Projections.include("name", "address.country", "amenities")
									),
							Aggregates.sort(Sorts.descending("name")),
							Aggregates.limit(2)
							));
					//logger.info(myDoc.toJson());
					area.setText("");
					
					MongoCursor<Document> iterator = myDocs.iterator();
					
					while(iterator.hasNext()) {
						Document myDoc = iterator.next();
						//System.out.print(myDoc.toJson());
						area.append("Obejktets namn: " + myDoc.getString("name") + "\n");
						area.append("Land: " + ((Document) myDoc.get("address")).getString("country") + "\nTillbehör: ");
						//area.append(myDoc.get("amenities"));
						
						List <String> tillblista = (List <String>) myDoc.get("amenities");
						
						for(String tillb : tillblista) {
							area.append(tillb + ", ");
						}
						area.append("\n\n");
					}
					

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		});
		
		
		
		f.add(b);
		f.add(area);
		f.add(t);
		f.setVisible(true);
		
		
	}

}
