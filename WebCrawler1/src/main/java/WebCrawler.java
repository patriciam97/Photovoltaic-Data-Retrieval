import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	public static final DCTaskManager DCtaskManager = new DCTaskManager(32);
	public static final PVTaskManager PVtaskManager = new PVTaskManager(32);

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

	/**
	 * 
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException
	 */
	private static void DisplayMenu() throws IOException, ParseException, InterruptedException {
		Scanner in = new Scanner ( System.in );
		System.out.println("***********************************************\n*                   SUNNY-BOT                 *\n*               by Patricia Milou             *\n*                                             *\n***********************************************");
	    System.out.println ( "Menu: \n1) Directory Crawling \n2) Profile Crawling\n3) Full Directory and Profile Crawling(all countries)\n4) Full Directory and Profile Crawling(specific country)\n5) Exit" );
	    System.out.print ( ">>Selection: " );
	    int option=in.nextInt();
	    
	    if(option>0 && option<6){
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
		    	  exit=true;
		    	  break;
		    }
	    }else{
	    	System.out.println("Option not available.");
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
		PVSystemCrawler prof;
		//PVSystems Crawler starts here
  		List<DBObject> plants=RetrievePVPlants();
  		System.out.println("Only "+ plants.size()+" PV Systems are install in "+Country);
//  	 	for (int i=0;i<plants.size();i++){
//  			 	String url = plants.get(i).get("_id").toString();
//  				prof= new PVSystemCrawler(dbConn,plants.get(i));
//  				prof.getProfileInfo();
//  				String[] subpages = prof.getUrlOfSubpage();
//  				if (subpages[0] != "nosubpage") { //if subpage doesn't exist, then readings dont exist
//  					prof.getMonthlyReadings(url, subpages);
//  				}
//  				prof.SaveInfo(); //saves or updates the system's information
//  		}
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
		//max=Dc.getMaximumPages();
		max =1121;
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
	


}
