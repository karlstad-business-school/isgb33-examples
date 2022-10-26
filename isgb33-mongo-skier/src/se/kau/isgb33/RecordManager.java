package se.kau.isgb33;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class RecordManager {
	private LinkedList<Record> v;
	private int index = 0;
	private Record curr;
	private String connString;
	private Logger logger;
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<Document> collection;

	public RecordManager() {
		logger = LoggerFactory.getLogger(RecordManager.class);
		try (InputStream input = new FileInputStream("connection.properties")) {

			Properties prop = new Properties();
			prop.load(input);
			connString = prop.getProperty("db.connection_string");
			logger.debug("Connection properties are read:" + connString);

			ConnectionString connectionString = new ConnectionString(connString);
			MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.build();
			mongoClient = MongoClients.create(settings);
			database = mongoClient.getDatabase(prop.getProperty("db.name"));
			collection = database.getCollection("skier");
			logger.debug("Collection skier is opened.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void openFile(String s) {
		v = new LinkedList<Record>();
		FindIterable<Document> myDoc = collection.find();
		for (Document tmp : myDoc) {
			curr = new Record();
			curr.setPnr(tmp.getString("pnr"));
			curr.setName(tmp.getString("name"));
			curr.setClub(tmp.getString("club"));
			v.add(curr);
			logger.debug("Adding to linkedlist: " + curr.getPnr());
		}

	}

	public void closeFile() {
		try {
			collection.deleteMany(new Document());
			Document d = new Document();
			logger.debug("Collection cleared.");
			for (Record tmp : v) {
				d = new Document();
				d.append("pnr", tmp.getPnr());
				d.append("name", tmp.getName());
				d.append("club", tmp.getClub());
				collection.insertOne(d);
				logger.debug("Adding skier to collection: " + tmp.getPnr());
			}

		} catch (Exception e) {
			logger.error("Fatal Error:  Failue saving collection....exiting");
			System.exit(1);
		}
	}

	public void addRecord(Record r) {
		try {
			v.addLast(r);
		} catch (Exception e) {
			logger.error("Fatal Error:  The collection has not been opened so exiting (add)");
			System.exit(1);
		}
	}

	public void removeRecord(Record r) {
		try {

			v.remove(r);
		} catch (Exception e) {
			logger.error(
					"Fatal Error:  The collection has not been opened or less than two records exists so exiting (remove)");
			System.exit(1);
		}
	}

	public Record firstRecord() {
		try {

			curr = (Record) v.get(0);
			index = 0;
			return curr;
		} catch (Exception e) {
			logger.error("Fatal Error:  The collection has not been opened or no records exists so exiting (first)");
			System.exit(1);
			return null;
		}
	}

	public Record lastRecord() {
		try {
			curr = (Record) v.getLast();
			index = v.size() - 1;
			return curr;
		} catch (Exception e) {
			logger.error("Fatal Error:  The collection has not been opened or no records exists so exiting (last)");
			System.exit(1);
			return null;
		}
	}

	public Record nextRecord() {
		index++;
		try {
			int tmp = v.size();
			if (index == tmp) {
				index--;
				return null;

			} else {
				curr = (Record) v.get(index);
				return curr;
			}
		} catch (Exception e) {
			logger.error("Fatal Error:  The collection has not been opened or no records exists so exiting (next)");
			System.exit(1);
			return null;
		}
	}

	public Record prevRecord() {
		index--;
		if (index <= -1) {
			index = 0;
		}
		try {

			if (index == 0) {
				return null;

			} else {
				curr = (Record) v.get(index);
				return curr;
			}
		} catch (Exception e) {
			logger.error("Fatal Error:  The collection has not been opened or no records exists so exiting (prev)");
			System.exit(1);
			return null;
		}
	}

	public Record createRecord() {
		return new Record();
	}

	public Record searchRecord(String s) {
		try {
			Iterator<Record> i = v.iterator();
			Record tmp = null;
			index = 0;
			while (i.hasNext()) {
				curr = (Record) i.next();
				if (s.equals(curr.getPnr())) {
					index = v.indexOf(curr);
					tmp = curr;
				}
			}
			return tmp;
		} catch (Exception e) {
			logger.error("Fatal Error:  The collection has not been opened or no records exists so exiting (search)");
			System.exit(1);
			return null;
		}
	}
}
