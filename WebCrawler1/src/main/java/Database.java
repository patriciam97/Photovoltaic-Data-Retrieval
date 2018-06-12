import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class Database {

	public static void main(String[] args) throws IOException {
		// connecting to the database
		BufferedReader br = new BufferedReader(new FileReader("configurations.txt"));
		String dburi=br.readLine(); //reads the first line of the configuration.txt
		MongoClientURI uri=new MongoClientURI(dburi);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		DBCollection collection= db.getCollection("test");
		List<Integer> books = Arrays.asList(27464, 747854);
		DBObject person = new BasicDBObject("_id", "jo3")
		                            .append("name", "Jo Bloggs")
		                            .append("address", new BasicDBObject("street", "123 Fake St")
		                                                         .append("city", "Faketon")
		                                                         .append("state", "MA")
		                                                         .append("zip", 12345))
		                            .append("books", books);
		collection.insert(person);
	}

}
