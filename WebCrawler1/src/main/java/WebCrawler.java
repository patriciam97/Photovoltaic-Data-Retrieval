import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, ParseException, InterruptedException {
		ArrayList<String> countrylist = null;
		long startTime = System.nanoTime();
		String configtxt="configurations.txt";
		Database db= new Database(configtxt);
		dbConn=db.getConnection();
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

	private static void runPVSystemCrawler(String Country) throws IOException, ParseException {
		PVSystemCrawler prof;
		//PVSystems Crawler starts here
  		List<DBObject> plants=RetrievePVPlants();
  		System.out.println("Only "+ plants.size()+" PV Systems are install in "+Country);
  		for (int i=0;i<plants.size();i++){
  				prof= new PVSystemCrawler(dbConn,plants.get(i));
  				prof.getProfileInfo();
  				String[] subpages = prof.getUrlOfSubpage();
  				if (subpages[0] != "nosubpage") { //if subpage doesn't exist, then readings dont exist
  					prof.getMonthlyReadings(urls.get(i), subpages);
  				}
  				prof.SaveInfo(); //saves or updates the system's information
  		}
  		//PVSystems Crawler ends here
        		
	}


	public static void runDirectoryCrawler(String maxPages) throws IOException, InterruptedException{
		int mxPgs;
		boolean done=true;
		int max = 0;
		DirectoryCrawler1 Dc= new DirectoryCrawler1(url);
		if (maxPages.toLowerCase().equals("all")){
			mxPgs=Dc.getMaximumPages(url);
			max=mxPgs;
		}else{
			mxPgs=Integer.parseInt(maxPages);
			max=Dc.getMaximumPages(url);
		}

			if(mxPgs<=max){
				int limit=5; //page limit
				int threadssize=(mxPgs / limit);

				ArrayList<Thread> threads=new ArrayList<Thread>();
				int c=0; //pagecounter
				if(mxPgs>=limit){
					for (int i=0;i<threadssize;i++){
						Thread object = new Thread(new DirectoryCrawler1("Thread "+i,dbConn,url,c+limit,c));
			            threads.add(object);
			            c=c+limit;
					}
				}
				if (mxPgs>c){
					System.out.println("Thread: "+threadssize+" FromPg: "+c+" To Page: "+mxPgs);
					Thread object = new Thread(new Thread(new DirectoryCrawler1("Thread "+threadssize,dbConn,url,mxPgs,c)));
		            threads.add(object);
		        }
				
				for(Thread t:threads){
					t.start();
				}
				
				//return to the Main Menu only if all threads have died.
					for(Thread t:threads){
						t.join();
					}
			
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
