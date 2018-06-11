import java.util.Arrays;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class Database {

	public static void main(String[] args) {
		// connecting to the database
		MongoClient mongoClient = new MongoClient("ds255260.mlab.com", 55260);
		MongoDatabase db = mongoClient.getDatabase("sunnyportal");
		 Document doc = new Document("name", "MongoDB")
         .append("type", "database")
         .append("count", 1)
         .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
         .append("info", new Document("x", 203).append("y", 102));
		 
		 MongoCollection<Document> collection = db.getCollection("test");
		 collection.insertOne(doc);

	}

}
