package main;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.path.xml.element.NodeChildren;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
	/**#
	 * 
	 * @author Patricia M.
	 *
	 */
public class WebCrawler {
	public static String Country;  //desired country selected by the user
	public static String maxPages; //maximumPages to retrieve,specifies by the user
	public static String dbConn;
	public static String url="https://www.sunnyportal.com/Templates/publicPagesPlantList.aspx?";
	public static Document doc;
	public static boolean exit=false;
	public static ArrayList<String> urls=null;
	public static HashMap<String,String> corrections=new HashMap<String,String>();
	public static final DCTaskManager DCtaskManager = new DCTaskManager(8);
	public static final PVTaskManager PVtaskManager = new PVTaskManager(16);
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, ParseException, InterruptedException {
		
		ArrayList<String> countrylist = null;
		long startTime = System.nanoTime(); //used in order to measure the running time
		String configtxt="configurations.txt";
		Database db= new Database(configtxt);
		dbConn=db.getConnection();
		//get all corrections from the database
		getCorrections();
		//show the menu until the user selects the exit option
		while(exit==false){
			DisplayMenu();
		}
		//calculates the elapsed time
		long estimatedTime = (System.nanoTime() - startTime);
		estimatedTime=TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);
		long mins= estimatedTime / 60;
		long secs= estimatedTime % 60;
		System.out.format("Elapsed Time: %d minutes and %d seconds.",mins, secs);
	}			

	private static void getCorrections() {
		MongoClientURI uri = new MongoClientURI(dbConn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DBCollection collection = db.getCollection("CorrectionsCollection");
		BasicDBObject query = new BasicDBObject();
		DBCursor results= collection.find(query);
		List<DBObject> locations= results.toArray();
		for (DBObject l : locations){
			if (l.containsField("Correction")){
				String id= l.get("_id").toString();
				String city= l.get("Correction").toString();
				corrections.put(id,city);
				
			}
		}
		
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException
	 */
	private static void DisplayMenu() throws IOException, ParseException, InterruptedException {
		Scanner in = new Scanner ( System.in );
		System.out.println("***********************************************\n*                   SUNNY-BOT                 *\n*               by Patricia Milou             *\n*                                             *\n***********************************************");
	    System.out.println ( "Menu: \n1) Directory Crawling \n2) Profile Crawling\n3) Full Directory and Profile Crawling(all countries)\n4) Full Directory and Profile Crawling(specific country)\n5)Coordinates \n6)Edit the Corrections Collection \n7) Exit" );
	    System.out.print ( ">>Selection: " );
	    int option=in.nextInt();
	    
	    if(option>0 && option<9){
		    switch (option) {
		      case 1:
		    	//Directory Crawler starts here
		     	System.out.println(">>How many pages (enter 'all' for all pages)? ");
		    	maxPages= in.next();
		    	runDirectoryCrawler(maxPages);
		        break;
		        
		      case 2:
		    	  System.out.println(">>For which country (enter 'all' for all countries)? ");
		    	 Country= in.next();
		    	 runPVSystemCrawler(Country);
		    	 break;
		      case 3:
		    	  runDirectoryCrawler("all");
		    	  runPVSystemCrawler("all");
		    	  break;
		      case 4:
		    	  System.out.println(">>For which country? ");
		    	  Country= in.next();
		    	  runDirectoryCrawler("all");
		    	  runPVSystemCrawler(Country);
		    	  break;
		      case 5:
		    	  break;
		      case 6:
		    	  ApplyCorrections();
		    	  break;
		      case 7:
		    	  exit=true;
		    	  break;
		      case 8:
		    	  updateDirectory();
		    	  break;
		    }
	    }else{
	    	System.out.println("Option not available.");
	    }
	}
	private static void ApplyCorrections() throws IOException {
		Scanner in;
		String correction,city;
		List<DBObject> locations;
		DBCollection collection,collection2;
		DBCursor results;
		
		MongoClientURI uri = new MongoClientURI(dbConn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		collection = db.getCollection("CorrectionsCollection");
		BasicDBObject query = new BasicDBObject();
		results= collection.find(query);
		locations= results.toArray();
			for (DBObject l:locations){
				if(l.containsField("Correction")==false){
					String plant= l.get("plant").toString();
					city=l.get("_id").toString();
					System.out.println("Enter a correction for "+city+":");
					in = new Scanner ( System.in );
					correction= in.nextLine();
					DBObject prof = new BasicDBObject("_id",city).append("Correction",correction).append("Plant",plant);
					collection.update(l,prof);
					System.out.println("Correction added.");
				}
			}
			System.out.println("Corrections are up to date.");
			results= collection.find(query);
			locations= results.toArray();
			for (DBObject l:locations){
				String plant= l.get("Plant").toString();
				String City= l.get("Correction").toString();
				collection = db.getCollection("ALLPVS");
				System.out.println(plant);
				DBObject prof=collection.findOne(plant);
				String link="http://api.geonames.org/search?q="+City+"&username=patriciam97";
				String lat=Jsoup.connect(link).timeout(20000).get().selectFirst("geonames geoname lat").text();
				String lng=Jsoup.connect(link).timeout(20000).get().selectFirst("geonames geoname lng").text();
				ArrayList<String> coordinates= new ArrayList<String>();
				coordinates.add(lat);
				coordinates.add(lng);
				DBObject updated=prof;
				updated.put("Coordinates",coordinates);
				collection.update(prof,updated);
				System.out.println((prof.get("System").toString())+" updated.");
			}

	}

	/**
	 * 
	 * @param Country country specified by the user
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException 
	 */
	private static void runPVSystemCrawler(String Country) throws IOException, ParseException, InterruptedException {
		ArrayList<Thread> threads=new ArrayList<Thread>(); 
		ArrayList<String> threadplants= new ArrayList<String>();
		CopyOfPVSystemCrawler prof;
		//PVSystems Crawler starts here
  		List<DBObject> plants=RetrievePVPlants();
  		System.out.println("Only "+ plants.size()+" PV Systems are install in "+Country);
  		//multithreading trial below
  		if(plants.size()>0){
	  		int limit=1; //page limit
			int threadssize=plants.size();
			int counter=0;
			for (int i=0;i<threadssize;i++){ 
				String plant= plants.get(i).get("_id").toString();
				counter++;
				PVtaskManager.submitJob((new CopyOfPVSystemCrawler("Thread "+i,dbConn,plant)));
			}
			while(PVtaskManager.howManyAreRunning()>0){
				Thread.sleep(100);
			}
  		}
  		
  	
	}
	/**
	 * 
	 * @param maxPages maximum pages the user selects
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static  void runDirectoryCrawler(String maxPages) throws IOException, InterruptedException{
		int mxPgs;
		boolean done=true;
		int max = 0;
		//creates a new Directory Crawler in order to extract the number of maximum pages
		DirectoryCrawler1 Dc= new DirectoryCrawler1(url);
		max=Dc.getMaximumPages();
		if (maxPages.toLowerCase().equals("all")){
			mxPgs=max;
		}else{
			mxPgs=Integer.parseInt(maxPages);
		}

		//if the user asks for more pages than the maximum pages
		if(mxPgs>max){
			return;
		}
			
		//if within the boundaries
		int limit=1; //page limit
		int threadssize=(mxPgs / limit);

		int c=0; //pagecounter
		
		if(mxPgs>=limit){
			for (int i=0;i<threadssize;i++){ 
				DCtaskManager.submitJob((new DirectoryCrawler1("Thread "+i,dbConn,url,c+limit,c)));
				c=c+limit;
				}
		}
		if (mxPgs>c){   //used for any remainder pages
			System.out.println("Thread: "+threadssize+" FromPg: "+c+" To Page: "+mxPgs);
			DCtaskManager.submitJob((new DirectoryCrawler1("Thread "+threadssize,dbConn,url,mxPgs,c)));
        }
		while(DCtaskManager.howManyAreRunning()>0){
			Thread.sleep(1000);
		}
	
		
		
	}

	public static List<DBObject> RetrievePVPlants(){
		DBCursor results;
		MongoClientURI uri = new MongoClientURI(dbConn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DBCollection collection = db.getCollection("DirectoryCollection");
		BasicDBObject fields = new BasicDBObject().append("_id", 1); // SELECT id
		if(Country.toLowerCase().equals("all")){
			BasicDBObject query =new BasicDBObject();
			results = collection.find(query,fields); // FROM DirectoryCollection 
			
		}else{
			BasicDBObject query = new BasicDBObject().append("Country", Country.toUpperCase()); // WHERE country= Country selected
			results = collection.find(query, fields); // FROM DirectoryCollecttion
		}
		return results.toArray();
		
	}

	//this method was used just once to update the Dictionary Collection with the appropriate coordinates
	//it was then added to DirectoryCrawler1.
	private static void updateDirectory() {
		String link;
		String username1="patriciam97";
		String username2="s1616316";
		String username3="patriciam1997";
		String username = username1;
		int counter=0;
		   ArrayList<String>  checkcoordinates= new ArrayList<String>();
					checkcoordinates.add(null);
					checkcoordinates.add(null);
		while(true) {
		if (username.equals(username1)){
			username=username2;
		}else if (username.equals(username2)){
			username=username3;
		}else {
			username=username1;
		}
		MongoClientURI uri = new MongoClientURI(dbConn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DBCollection collection = db.getCollection("DirectoryCollection");
		DBCursor cursor = collection.find();
		
		while(cursor.hasNext()) {
		   DBObject obj = cursor.next();
		   if(obj.get("Coordinates")==null ||obj.get("Coordinates").equals(checkcoordinates) ) {
		   DBObject updated=collection.findOne(obj.get("_id"));
		   String country,city;
		   country= obj.get("Country").toString();
		   city=obj.get("City").toString();
		   link="http://api.geonames.org/search?q="+city+"+"+country+"&username="+username;
			try {
				if(Jsoup.connect(link).timeout(20000000).get().selectFirst("totalResultsCount").text().equals("0")){
					link="http://api.geonames.org/search?q="+country+"&username="+username;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				e.printStackTrace();
			}
			String lat = null;
			try {
				lat = Jsoup.connect(link).timeout(2000000).get().selectFirst("geonames geoname lat").text();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				e.printStackTrace();
			}
			String lng = null;
			try {
				lng = Jsoup.connect(link).timeout(2000000).get().selectFirst("geonames geoname lng").text();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				e.printStackTrace();
			}
		   try {
				if(lat.equals(null)==false && (lng).equals(null)==false) {
			
				ArrayList<String>  coordinates= new ArrayList<String>();
				coordinates.add(lat);
				coordinates.add(lng);
				updated.put("Coordinates",coordinates);
				collection.update(obj, updated);
				counter++;
				System.out.println(counter);
				System.out.println(lat+" "+lng);
			}
		   }catch (NullPointerException e) {
				e.printStackTrace();
			}
		   }
		}
		}
	}

}
