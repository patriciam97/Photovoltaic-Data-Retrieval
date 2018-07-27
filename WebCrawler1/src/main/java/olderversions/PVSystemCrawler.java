package olderversions;
import main.WebCrawler;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.WebCrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
/**
 * 
 * @author Patricia M
 *
 */
public class PVSystemCrawler  {
	private String Conn,Country;
	private String url, plant;
	private String id, SystemTitle,Operator, City,Zipcode, StartDate, Power,
			AnProduction, CO2, Modules, Azimuth, Inclination, Communication,
			Inverter, Sensors, imgLink, readingsUnit;
	private String descinfo;
	private int StartingYear;
	private List<BasicDBObject> monthlyReadings= new ArrayList<BasicDBObject>();
	private ArrayList<String> coordinates;
	/**
	 * Constructor of each PV System
	 * @param Conn		connection to the database
	 * @param plants.		path of each PVSystem
	 * @param power 	power of the System
	 * @param loc		location of the System(City)
	 * @param Country	country in which the System is install
	*/
	public PVSystemCrawler(String Conn,DBObject plants){

		this.Conn = Conn;
		this.plant = plants.get("_id").toString();
		this.url = "https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?plant="
				+ plant + "&splang=";
	}
	/**
	 * this function gets the profile information
	 * and saves them as the values of the global variables above.
	 * @throws IOException
	 */
	public void getProfileInfo() throws IOException {
		Elements elements,elementsval; //used for the selected elements.
		
		//connects to the url
		Document doc = Jsoup.connect(url).timeout(20000000).get();
		SystemTitle= doc.select("#ctl00_ContentPlaceHolder1_title").get(0).text();
		// extracting the location
		elements = doc
				.select("td[class=PlantProfileCellLabel BoxRoundCornerLineVLeft]");
		elementsval = doc
				.select("td[class=PlantProfileCellValue BoxRoundCornerLineVRight]");
		//extraction of any information available
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).text().equals("Location:")) {
				City = elementsval.get(i).text().toString();
			} else if (elements.get(i).text().equals("Operator:")) {
				Operator = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Commissioning:")) {
				StartDate = elementsval.get(i).text();
				StartingYear = Integer.parseInt(StartDate.split("/")[2]);
			} else if (elements.get(i).text().equals("PV system power:")) {
				Power = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Annual Production:")) {
				AnProduction = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("CO2 avoided:")) {
				CO2 = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Modules:")) {
				Modules = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Azimuth angle:")) {
				Azimuth = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Angle of inclination:")) {
				Inclination = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Communication:")) {
				Communication = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Inverter:")) {
				Inverter = elementsval.get(i).text();
			} else if (elements.get(i).text().equals("Sensors:")) {
				Sensors = elementsval.get(i).text();
			}
		}
		elementsval = doc.select("span");
		descinfo = elementsval.get(3).text();
		//extraction of the image if it exists
		elements = doc.select("img[class=css3pie dropShadow]");
		if (elements.size() > 0) {
			imgLink = elements.get(0).attr("src").toString();
			if (imgLink.startsWith("..")){
				imgLink=imgLink.split("[.]")[2];
			}
		}
	}
	/**
	 * this function gets the url of each subpage
	 * @return      all urls that exist or "nosubpage" if no subpage exist 
	 * @throws IOException
	 */
	public String[] getUrlOfSubpage() throws IOException {

		String[] returnval = null;
		Elements elements; //used for the selected elements.
		
		//connects to the url
		Document doc = Jsoup.connect(url).timeout(20000000).get();
		//if subpages exist
		if (doc.select(
				"#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_OpenButtonsDivImg")
				.size() > 0) {
			elements = doc.select("li[class=nosub selected]");
			if (elements.size() > 0) {
				returnval = new String[elements.size()];
				for (int i = 0; i < elements.size(); i++) {
					String[] u = elements.get(i).id().toString().split("_");
					returnval[i] = u[1]; //save the path to each subpage
				}
			}
		} else { 
			//subpages exist under this class too
			elements = doc.select("li[class=nosub]");
			if (elements.size() > 0) {
				returnval = new String[elements.size()];
				for (int i = 0; i < elements.size(); i++) {
					String[] u = elements.get(i).id().toString().split("_");
					returnval[i] = u[1];
				}
			} else { 
				//if no subpages exist
				returnval = new String[1];
				returnval[0] = "nosubpage";
			}
		}
		return (returnval);
	}
	/**
	 * 
	 * @param plant path of each PV System.
	 * @param subpages paths to any subpage
	 * @throws IOException
	 * @throws ParseException
	 */
	public void getMonthlyReadings(String plant, String[] subpages)
			throws IOException, ParseException {
		Elements elements; //selected elements
		Document doc;
		
		for (String page : subpages) {
			url = "https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?page="
					+ page + "&plant=" + plant + "&splang=en-US";
			//connect to each subpage
			doc = Jsoup.connect(url).get();
			//id of each PVSystem is stored under this ID.
			elements = doc
					.select("#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_HyperLinkLup");
			if (elements.size() > 0) {
				String[] idA = doc
						.select("#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_HyperLinkLup")
						.get(0).attr("onClick").toString().split("'");
				id = idA[11]; //save the ID of each PV System
				SaveReadings(); //saves the monthly readings.
				break;
			} else {
				//if ID doesnt exist, get userid
				elements = doc.select("script");
				for (Element el : elements) {
					if (el.html().contains("userid:")) {
						id = (el.html().split("userid:")[1].split("'")[1]);
						SaveReadings();
					}
					break;
				}

			}
		}
	}
	/**
	 * this function saves the monthyReadings for a PV System(if available)
	 * in a List of BasicDBObject.
	 * @throws IOException
	 * @throws ParseException
	 */
	private void SaveReadings() throws IOException, ParseException {
		double powerR = 0;
		Elements elements;  //used for the selected elements
		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);
		DecimalFormat df = new DecimalFormat("#.#####");
		DateFormat format = new SimpleDateFormat("MMM yy");
		Document doc;
		
		url = "https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="
				+ id
				+ "&endTime=12/31/"
				+ currentYear
				+ "%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=";
		//connects to the url
		doc = Jsoup.connect(url).get();
		elements = doc.select("div.tabelle table tr td.base-grid-header-cell");
		if(elements.size()==0){
			getDailyReadings(); //check for daily readings
		}else{
		readingsUnit = elements.get(1).text().split("\\[")[1].split("]")[0]; //unit used for the readings
			for (int i = StartingYear; i < currentYear + 1; i++) { 
				//from the year, the System has been install, until now
				//get all readings available
				url = "https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="
						+ id
						+ "&endTime=12/31/"
						+ i
						+ "%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=";
				doc = Jsoup.connect(url).get();
				elements = doc
						.select("div.tabelle table tr[class^=base-grid-item]");
				if (elements.size() > 0) {
					for (Element el : elements) {
						Date date = (Date)format.parse(el.select("td").get(0).text());
						//convert the date to timestamp(long integer)
						Long timestamp = date.getTime();
						//reading for the specific timestamp
						String power=el.select("td").get(1).text();
						if(power.isEmpty()==false){ 
							// if column is empty, convert it to 0.
							powerR = Double.parseDouble(power);
						}
						BasicDBObject bdbo = new BasicDBObject();
						bdbo.append("timestamp", timestamp);
						bdbo.append("value", df.format(powerR));
						monthlyReadings.add(bdbo);
					}
				}
			}
		}
	}
	/**
	 * this function is used only if the daily readings are available
	 * @throws IOException
	 * @throws ParseException
	 */
	private void getDailyReadings() throws IOException, ParseException {
		double powerR;
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH); //current month
        int year = now.get(Calendar.YEAR); //current year
        DecimalFormat df = new DecimalFormat("#.#####");
        Elements elements = null;
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Document doc;
		
        now.clear(); 
        do{
        	now.set(Calendar.YEAR, year);
        	powerR=0;
        	for (int currentMonth = month; currentMonth >0; currentMonth--) {
                now.set(Calendar.MONTH, currentMonth-1);
                //last day of month
                int day=now.getActualMaximum(Calendar.DAY_OF_MONTH);
                String date=currentMonth+"/"+day+"/"+year;
                url="https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="+id+"&endTime="+date+"%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=";
                //connect to the page with the daily readings for each month
                doc=Jsoup.connect(url).get();
                elements = doc
						.select("div.tabelle table tr[class^=base-grid-item]");
				if (elements.size() > 12 && elements.size() <24) {
					//if over 12, it means we have the daily ones
					for (Element el : elements) {
						String power=el.select("td").get(1).text();
						if(power.isEmpty()==false){
							powerR = powerR+Double.parseDouble(power); //add them together
						}
					}
					Date d = format.parse(date);
					Long timestamp = d.getTime();
					BasicDBObject bdbo = new BasicDBObject();
					bdbo.append("timestamp", timestamp);
					bdbo.append("value", df.format(powerR));
					monthlyReadings.add(bdbo);
				}else{
					// no daily readings are available
					break;
				}
            }
        	year=year-1;
        	month=12;
        }while(elements.size()>0); //until we have information
		
	}
	/**
	 * Connects to the database and saves all information.
	 */
	public void SaveInfo() { 
		MongoClientURI uri = new MongoClientURI(Conn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		DBCollection collection = db.getCollection("PVSystemProfiles");
		// creating a document for the PV System
		DBObject prof = new BasicDBObject("_id", plant)
				.append("System", SystemTitle)
				.append("Operator", Operator)
				.append("systemid", id)
				.append("Country", Country)
				.append("City", City)
				.append("ZipCode", Zipcode)
				.append("StartDate", StartDate)
				.append("SystemPower", Power)
				.append("SystemAnnualProduction", AnProduction)
				.append("CO2", CO2).append("Azimuth", Azimuth)
				.append("Inclination", Inclination)
				.append("Communication", Communication)
				.append("Modules", Modules).append("Inverter", Inverter)
				.append("Sensors", Sensors).append("Image", imgLink)
				.append("descinfo", descinfo)
				.append("readingsUnit", readingsUnit)
				.append("monthlyReadings", monthlyReadings)
				.append("Coordinates", coordinates);
		//check if this document already exists
		DBObject exists=collection.findOne(plant);
		if (exists!=null){ 
			//if it exists
			if (exists.equals(prof)){
				//if it is the same
				System.out.println(dtf.format(now)+": "+SystemTitle+" is up to date.");
			}else{
				//needs to be updated
				collection.update(exists,prof);
				System.out.println(dtf.format(now)+": "+SystemTitle+ " updated.");
			}
		}else{
			//if it doesnt exist, insert it
			collection.insert(prof);
			System.out.println(dtf.format(now)+": "+SystemTitle+" saved.");
		}

	}
	public void getCoordinates() throws IOException{
		//get coordinates
  		DBCursor results;
  		BasicDBObject query ;
  		DBCollection collection;
  		String link;
  		
		MongoClientURI uri = new MongoClientURI(Conn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		collection = db.getCollection("DirectoryCollection");
		query = new BasicDBObject().append("_id",plant); // WHERE _id= plant selected
		BasicDBObject project = new BasicDBObject();    //SELECT id,city and country
		project.put("_id", "");
		project.put("City","");
		results= collection.find(query,project);
		List<DBObject> locations= results.toArray();
		for(DBObject l:locations){
			//get id,city for each one
			String id= l.get("_id").toString();
			String City= l.get("City").toString();
			City.replace(" ","+");  //change spance to '+'
			link="http://api.geonames.org/search?q="+City+"+"+Country+"&username=patriciam97";
			System.out.println("Searcing for "+City);
			if(Jsoup.connect(link).get().selectFirst("totalResultsCount").text().equals("0")){
				//if location not found on the geonames database
				//check in our correction collection
				collection=db.getCollection("CorrectionsCollection");
				DBObject prof = new BasicDBObject("_id",City);
				DBObject exists=collection.findOne(City);
				if (exists==null){  //if it doesnt exist
					collection.insert(prof); //add it in the corrections collection
					System.out.println("Needs correction");
				}else{
					//if it exists check if correction exists
					if(WebCrawler.corrections.containsKey(City)){
						System.out.println("Getting the correction");
						City= WebCrawler.corrections.get(City);
						updateCoordinates(db,id,City);
					}
				}
			}else{
				updateCoordinates(db,id,City);
			}
			
		}

	}
	
	public void updateCoordinates(DB db,String id,String City) throws IOException{
  		BasicDBObject query ;
  		DBCollection collection;
		String link="http://api.geonames.org/search?q="+City+"+"+Country+"&username=patriciam97";
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		System.out.println(Jsoup.connect(link).get().selectFirst("geonames geoname toponymName").text());
		String lat=Jsoup.connect(link).timeout(20000).get().selectFirst("geonames geoname lat").text();
		String lng=Jsoup.connect(link).timeout(20000).get().selectFirst("geonames geoname lng").text();
		System.out.println(lat+" "+lng);
		query = new BasicDBObject().append("_id", id);
		coordinates= new ArrayList<String>();
		coordinates.add(lat);
		coordinates.add(lng);
	}
}
