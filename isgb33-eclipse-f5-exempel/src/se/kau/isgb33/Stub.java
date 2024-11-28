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
					
					String country = t.getText();
					Document myDoc = collection.find(Filters.eq("address.country", country)).first();
					//Bson filter = eq("address.country", "Turkey");
					
					//logger.info(myDoc.toJson());
					
					area.setText(myDoc.toJson());

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
