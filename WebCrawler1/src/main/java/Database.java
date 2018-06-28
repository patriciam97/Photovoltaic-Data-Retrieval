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

/**
 * 
 * @author Patricia M
 *
 */
public class Database {
	public String host;
	public String port;
	public String username;
	public String password;
	public String database;
	
/**
 * 
 * @param configtxt textfile with the details
 * @throws IOException
 */
	public Database(String configtxt) throws IOException{
		ReadConfig(configtxt);
	}
	public void ReadConfig(String configtxt) throws IOException{
		String line=null;
		String segments[];
		BufferedReader br = new BufferedReader(new FileReader(configtxt));
		 while((line = br.readLine()) != null) {
			 segments = line.split(":");

			 if (segments[0].equals("username")){
				 this.username=segments[1];
			 }else if (segments[0].equals("password")){
				 this.password=segments[1];
			 }else if (segments[0].equals("host")){
				 this.host=segments[1];
			 }else if (segments[0].equals("port")){
				 this.port=segments[1];
			 } else if (segments[0].equals("database")){
				 this.database=segments[1];
			 }
         }   
		 br.close(); 
	}
	/**
	 * 
	 * @return the connection string
	 */
	public String getConnection(){
		return "mongodb://"+username+":"+password+"@"+host+":"+port+"/"+database;
	}
}
