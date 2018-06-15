import java.io.IOException;
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
	private String url,path;
	private String Owner,Location,Operator,StartDate,Power,AnProduction,CO2,Modules,Azimuth,Inclination,Communication,Inverter,Sensors,PVType,InverterType,SupportType,Telephone;
	private String descinfo;
	private Document doc;
	private Elements elements,elementsval;
	public PVSystemCrawler(String Conn,String path,String power) throws IOException {
		this.Conn=Conn;
		this.path=path;
		this.Power=power;
		this.url="https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?plant="+path+"&splang=";
		getProfileInfo();
		getUrlOfSubpage();
		//SaveInfo(Conn);
	}
	public void getProfileInfo() throws IOException{
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
	}
	public void getUrlOfSubpage() throws IOException{
		doc=Jsoup.connect(url).get();
		elements=doc.select("li[class=nosub]");
		String[]u= elements.get(0).id().toString().split("_");
		System.out.println(u[1]);
	}
	public void ShowProfInfo(){
		System.out.println(Owner);
		System.out.println(Location);
		System.out.println(Operator);
		System.out.println(StartDate);
		System.out.println(Power);
		System.out.println(AnProduction);
		System.out.println(CO2);
		System.out.println(Azimuth);
		System.out.println(Inclination);
		System.out.println(Communication);
		System.out.println(Modules);
		System.out.println(Inverter);
		System.out.println(Sensors);
		System.out.println(descinfo);	
	}
	public void SaveInfo(String Connetion){
		MongoClientURI uri=new MongoClientURI(Connetion);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE);
		DBCollection collection= db.getCollection("PVSystemProfiles");
		DBObject prof = new BasicDBObject("_id",path)
		                            .append("Owner",Owner)
		                            .append("Location",Location)
		                            .append("StartDate",StartDate)
		                            .append("AnProduction",AnProduction)
		                            .append("CO2",CO2)
		                            .append("Azimuth",Azimuth)
		                            .append("Inclination",Inclination)
		                            .append("Communication",Communication)
		                            .append("Modules",Modules)
		                            .append("Inverter",Inverter)
		                            .append("Sensors",Sensors)
		                            .append("descinfo",descinfo);
		collection.insert(prof);

	}

}
