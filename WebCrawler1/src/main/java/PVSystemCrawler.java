import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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


public class PVSystemCrawler {
	private String Conn;
	private String url,plant;
	private String Owner,Location,Operator,StartDate,Power,AnProduction,CO2,Modules,Azimuth,Inclination,Communication,Inverter,Sensors,imgLink;
	private String descinfo;
	private Document doc;
	private Elements elements,elementsval;
	private int StartingYear;
	private Map<String, String> monthlyReadings = new HashMap<String, String>();
	public PVSystemCrawler(String Conn,String plant,String power) throws IOException, ParseException {
		this.Conn=Conn;
		this.plant=plant;
		this.Power=power;
		this.url="https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?plant="+plant+"&splang=";
		getProfileInfo();
		String page=getUrlOfSubpage();
		if (page!="nosubpage"){
			getMonthlyReadings(plant, page);
		}
		SaveInfo(Conn);
	}
	public void getProfileInfo() throws IOException, ParseException{
		doc=Jsoup.connect(url).get();
		//extracting the title
		elements=doc.select("head");
		Owner=elements.get(0).text().toString();
		//extracting the location
		elements=doc.select("td[class=PlantProfileCellLabel BoxRoundCornerLineVLeft]");
		elementsval=doc.select("td[class=PlantProfileCellValue BoxRoundCornerLineVRight]");
		for(int i=0;i<elements.size();i++){
			if (elements.get(i).text().equals("Location:")){
				Location=elementsval.get(i).text().toString();
			}else if (elements.get(i).text().equals("Operator:")){
				Operator=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Commissioning:")){
				StartDate=elementsval.get(i).text();
				StartingYear= Integer.parseInt(StartDate.split("/")[2]);
			}else if (elements.get(i).text().equals("PV system power:")){
				Power=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Annual Production:")){
				AnProduction=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("CO2 avoided:")){
				CO2=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Modules:")){
				Modules=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Azimuth angle:")){
				Azimuth=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Angle of inclination:")){
				Inclination=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Communication:")){
				Communication=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Inverter:")){
				Inverter=elementsval.get(i).text();
			}else if (elements.get(i).text().equals("Sensors:")){
				Sensors=elementsval.get(i).text();
			}	
		}
		elementsval=doc.select("span");
		descinfo=elementsval.get(3).text();	
		//imgLink=doc.selectFirst("img[class=css3pie dropShadow]").attr("src").toString();
	}
	public String getUrlOfSubpage() throws IOException{
		String returnval;
		doc=Jsoup.connect(url).get();
		elements=doc.select("li[class=nosub]");
		if(elements.size()>0){
			String[]u= elements.get(0).id().toString().split("_");
			returnval=u[1];
		}else{
			returnval="nosubpage";
		}
		System.out.println(returnval);
		return(returnval);
	}
	public void getMonthlyReadings(String plant, String page) throws IOException, ParseException{
		String id = null;
		url="https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?page="+page+"&plant="+plant+"&splang=en-US";
		doc=Jsoup.connect(url).get();
		elements=doc.select("#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_HyperLinkLup");
		Calendar now = Calendar.getInstance();
		int currentYear=now.get(Calendar.YEAR);
		if (elements.size()>0){
			String[] idA=doc.select("#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_HyperLinkLup").get(0).attr("onClick").toString().split("'");
			id=idA[11];
			for (int i=StartingYear;i<currentYear+1;i++){
				url="https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="+id+"&endTime=12/31/"+i+"%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=";
				doc=Jsoup.connect(url).get();
				elements=doc.select("div.tabelle tr[class^=base-grid-item]");
				SaveReadings();
			}
		}else{
			elements =doc.select("script");
			for(Element el: elements){
				if (el.html().contains("userid:")){
					id=(el.html().split("userid:")[1].split("'")[1]);
				}
			}
			for (int i=StartingYear;i<currentYear+1;i++){
				url="https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="+id+"&endTime=12/31/"+i+"%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=Year";
				doc=Jsoup.connect(url).get();
				elements=doc.select("div.tabelle table tr[class^=base-grid-item]");
				SaveReadings();
			}
		}
	}
	public void SaveReadings() throws ParseException{
		if(elements.size()>0){
			for(Element el:elements){
				String Date= el.select("td").get(0).text();
				String powerR=el.select("td").get(1).text();
				System.out.println(Date);
				System.out.println(powerR);
				monthlyReadings.put(Date,powerR);
			}
		}
	}
	public void SaveInfo(String Connetion){
		MongoClientURI uri=new MongoClientURI(Connetion);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE);
		DBCollection collection= db.getCollection("PVSystemProfiles");
		DBObject prof = new BasicDBObject("_id",plant)
		                            .append("Owner",Owner)
		                            .append("Location",Location)
		                            .append("StartDate",StartDate)
		                            .append("SystemPower",Power)
		                            .append("SystemAnnualProduction",AnProduction)
		                            .append("CO2",CO2)
		                            .append("Azimuth",Azimuth)
		                            .append("Inclination",Inclination)
		                            .append("Communication",Communication)
		                            .append("Modules",Modules)
		                            .append("Inverter",Inverter)
		                            .append("Sensors",Sensors)
		                            .append("Image",imgLink)
		                            .append("descinfo",descinfo)
		                            .append("monthlyReadings",monthlyReadings);
		collection.insert(prof);
		System.out.println("Information Saved for "+Owner);
	}

}
