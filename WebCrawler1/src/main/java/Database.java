import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
	static MongoClientURI uri;
	static MongoClient mongoClient ;
	static DB db ;
	static ArrayList<String> urls;
	public static void main(String[] args) throws IOException, InterruptedException {
		urls=ExtractInformation.main(args);
		Connect();
		SaveURLS();
		
//		DBCollection collection= db.getCollection("test");
//		List<Integer> books = Arrays.asList(27464, 747854);
//		DBObject person = new BasicDBObject("_id", "jo3")
//		                            .append("name", "Jo Bloggs")
//		                            .append("address", new BasicDBObject("street", "123 Fake St")
//		                                                         .append("city", "Faketon")
//		                                                         .append("state", "MA")
//		                                                         .append("zip", 12345))
//		                            .append("books", books);
//		collection.insert(person);
	}
	public static void Connect() throws IOException {
		// connecting to the database
		String line=null;
		String username=null,password=null,host=null,port=null,database=null;
		String segments[];
		BufferedReader br = new BufferedReader(new FileReader("configurations.txt"));
		 while((line = br.readLine()) != null) {
			 segments = line.split(":");

			 if (segments[0].equals("username")){
				 username=segments[1];
			 }else if (segments[0].equals("password")){
				 password=segments[1];
			 }else if (segments[0].equals("host")){
				 host=segments[1];
			 }else if (segments[0].equals("port")){
				 port=segments[1];
			 } else if (segments[0].equals("database")){
				 database=segments[1];
			 }
         }   
		 String dburi="mongodb://"+username+":"+password+"@"+host+":"+port+"/"+database;
		 br.close(); 
		 uri=new MongoClientURI(dburi);
		 mongoClient = new MongoClient(uri);
		 db = mongoClient.getDB("sunnyportal");
	}
	public static void SaveURLS() {
		DBCollection collection= db.getCollection("PVSystemUrls");
		for(int i=0;i<urls.size();i++) {
			DBObject url = new BasicDBObject("_id", i)
					.append("path", urls.get(i).toString());
			collection.insert(url);
		}
				
	}

}
