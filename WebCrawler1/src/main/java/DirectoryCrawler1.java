import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * 
 * @author Patricia M.
 *
 */
 public class DirectoryCrawler1 {
 	private static String url;
 	private String Conn;
 	private int maxPgs;
 	private ArrayList<String> urls= new ArrayList<String>();
 	private ArrayList<String> fullurls = new ArrayList<String>();
 	private ArrayList<String> powerList = new ArrayList<String>();
	private ArrayList<String> cityList = new ArrayList<String>();
	private ArrayList<String> countryList = new ArrayList<String>();
	private ArrayList<String> zipcodeList = new ArrayList<String>();
	private ArrayList<String> systemList = new ArrayList<String>();
 	private static int counter=0;
 	private static Document doc;
	/**
	 * 
	 * @param url base url of website
	 * @param maxPgs maximum pages to crawl
	 * @param Country country we are investigating
	 * @throws IOException 
	 */
 	public DirectoryCrawler1(String Conn,String url,String pg) throws IOException {
 		this.url = url;
 		//maxPgs=getMaximumPages(url);
 		this.maxPgs=Integer.parseInt(pg);
 		this.Conn=Conn;
 	}

	/**
	 * 
	 * @return pg maximum number of pages
	 */
	private static int getMaximumPages(String url) throws IOException {
		
		int pg = 0;
		doc= Jsoup.connect(url).timeout(100000).get();
		Element table = doc.getElementById("ctl00_ContentPlaceHolder1__dataGridPagerDown_PagerTable");
		Elements pages = table.select("tr td a");
		// the last element is the total number of the pages
		pg = Integer.parseInt(pages.last().text());
		System.out.println("Total num of pages: "+pg);
		return pg+1;
	}

	/**
	 * 
	 * @return urls of all systems from the Directory page
	 */
 	public ArrayList<String> GetUrls() {
 		for (int i = 0; i < maxPgs; i++) {
 			counter=0;
 			ConnectToEachPage(i);
 			ExtractUrls();
 			System.out.println("Extracted "+ counter +" urls from page "+ (i+1));
 		}
 		this.urls = ManipulateUrls(); // path to each PV System Profile
 		return urls;
 	}


	/**
	 * this function connects to a page
	 * @param pg page number to crawl
	 */
 	private static void ConnectToEachPage(int pg) {
 		String link = url + "PageIndex=" + pg;
 		System.out.println("Crawling page: " + (pg+1));
 		try {
			doc = Jsoup.connect(link).timeout(100000).get();
			//connects to the link
			doc = Jsoup.connect(link).timeout(1000000).get();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}

	/**
	 *  this function extracts the full url for each PV System Profile
	 */
 	private void ExtractUrls() {
 		String system,country,city,zipcode,power;
 		Elements reportContent;		
 		// Select all elements with an href tag
 		reportContent = doc.select("table[class=base-grid] tr[class^= base-grid-item]"); 
 			for (Element el : reportContent) {
 				String link=el.select("td a[href]").get(0).attr("href").toString();
 				if(fullurls.contains(link)==false){
	 				counter++;
	 				country=el.select("td").get(2).text().toString();
	 				zipcode=el.select("td").get(3).text().toString();
	 				city=el.select("td").get(4).text().toString();
	 				power=el.select("td").get(5).text().toString();
	 				system=el.select("td").get(1).text().toString();
	 				powerList.add(power);
					cityList.add(city);
					countryList.add(country);
					zipcodeList.add(zipcode);
					systemList.add(system);
	 				fullurls.add(link);
	 				System.out.printf("%s \t %s \t %s \t %s \t %s \n",system,country,city,zipcode,power);
 				}
 			}
 		urls = fullurls;
 	}
	/**
	 * 
	 * @return all paths for each PV System(not fully url)
	 */
 	private ArrayList<String> ManipulateUrls() {
	// extracts the path for each PV System Profile
 		ArrayList<String> updatedUrls = new ArrayList<String>();
 		String substrings[] = null;
		
 		for (int i = 0; i < urls.size(); i++) {
			//for all urls available
 			if (urls.get(i).toString().contains("OpenPlant")) {
 				// splits the original url we have extracted at these characters
 				substrings = urls.get(i).split("[=&]");
 			}
 		}
		return updatedUrls;
}

/**
 * 
 * @return the power list from the Directory page
 */
	public ArrayList<String> getPowerList(){
		return powerList;
		
	}
/**
 * 
 * @return the city list from the Directory page
 */
public ArrayList<String> getCityList(){
	return cityList;
	
}
/**
 * 
 * @return the zip code list from the Directory page
 */
public ArrayList<String> getZipCodeList(){
	return zipcodeList;
	
}
/**
 * 
 * @return the country list from the Directory page
 */
public ArrayList<String> getCountryList(){
	return countryList;
		
}
	/**
	 * 
	 * @return the system list from the Directory page
	 */
	public ArrayList<String> getSystemList(){
		return systemList;
		
	}
	public void SaveDirectory() {
		MongoClientURI uri = new MongoClientURI(Conn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		DBCollection collection = db.getCollection("DirectoryCollection");
		// creating a document for the PV System
		for(int i=0;i<urls.size();i++) {
				DBObject prof = new BasicDBObject("_id", urls.get(i)).
						 append("System", systemList.get(i))
						.append("Country", countryList.get(i))
						.append("City", cityList.get(i))
						.append("ZipCode",zipcodeList.get(i))
						.append("SystemPower", powerList.get(i));
				//check if this document already exists
				DBObject exists=collection.findOne(urls.get(i));
				if (exists!=null){ 
					//if it exists
					if (exists.equals(prof)){
						//if it is the same
						System.out.println(dtf.format(now)+": "+systemList.get(i)+" is up to date.");
					}else{
						//needs to be updated
						collection.update(exists,prof);
						System.out.println(dtf.format(now)+": "+systemList.get(i)+ " updated.");
					}
				}else{
					//if it doesnt exist, insert it
					collection.insert(prof);
					System.out.println(dtf.format(now)+": "+systemList.get(i)+" saved.");
				}
		}
	}
}
