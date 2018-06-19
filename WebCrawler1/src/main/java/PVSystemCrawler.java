import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	private String url, plant;
	private String id, Owner, Location, Operator, StartDate, Power,
			AnProduction, CO2, Modules, Azimuth, Inclination, Communication,
			Inverter, Sensors, imgLink, readingsUnit;
	private String descinfo;
	private Document doc;
	private Elements elements, elementsval;
	private int StartingYear, currentYear;
	private List<BasicDBObject> monthlyReadings= new ArrayList<BasicDBObject>();
	private Calendar now;
	private DateFormat format,formatter;

	public PVSystemCrawler(String Conn, String plant, String power)
			throws IOException, ParseException {
		this.Conn = Conn;
		this.plant = plant;
		this.Power = power;
		this.url = "https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?plant="
				+ plant + "&splang=";
		now = Calendar.getInstance();
		currentYear = now.get(Calendar.YEAR);
		getProfileInfo();
		String[] subpages = getUrlOfSubpage();
		if (subpages[0] != "nosubpage") {
			getMonthlyReadings(plant, subpages);
		}
		SaveInfo(Conn);
	}

	public void getProfileInfo() throws IOException {
		doc = Jsoup.connect(url).get();
		// extracting the title
		elements = doc.select("head");
		Owner = elements.get(0).text().toString();
		// extracting the location
		elements = doc
				.select("td[class=PlantProfileCellLabel BoxRoundCornerLineVLeft]");
		elementsval = doc
				.select("td[class=PlantProfileCellValue BoxRoundCornerLineVRight]");
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).text().equals("Location:")) {
				Location = elementsval.get(i).text().toString();
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
		elements = doc.select("img[class=css3pie dropShadow]");
		if (elements.size() > 0) {
			imgLink = elements.get(0).attr("src").toString();
		}
	}

	public String[] getUrlOfSubpage() throws IOException {
		String[] returnval = null;
		doc = Jsoup.connect(url).get();
		if (doc.select(
				"#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_OpenButtonsDivImg")
				.size() > 0) {
			elements = doc.select("li[class=nosub selected]");
			if (elements.size() > 0) {
				returnval = new String[elements.size()];
				for (int i = 0; i < elements.size(); i++) {
					String[] u = elements.get(i).id().toString().split("_");
					returnval[i] = u[1];
				}
			}
		} else {
			elements = doc.select("li[class=nosub]");
			if (elements.size() > 0) {
				returnval = new String[elements.size()];
				for (int i = 0; i < elements.size(); i++) {
					String[] u = elements.get(i).id().toString().split("_");
					returnval[i] = u[1];
				}
			} else {
				returnval = new String[1];
				returnval[0] = "nosubpage";
			}
		}
		return (returnval);
	}

	public void getMonthlyReadings(String plant, String[] subpages)
			throws IOException, ParseException {
		for (String page : subpages) {
			url = "https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?page="
					+ page + "&plant=" + plant + "&splang=en-US";
			doc = Jsoup.connect(url).get();
			elements = doc
					.select("#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_HyperLinkLup");
			if (elements.size() > 0) {
				String[] idA = doc
						.select("#ctl00_ContentPlaceHolder1_PublicPagePlaceholder_PageUserControl_ctl00_UserControl1_HyperLinkLup")
						.get(0).attr("onClick").toString().split("'");
				id = idA[11];
				SaveReadings();
				break;
			} else {
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

	public void SaveReadings() throws IOException, ParseException {
		double powerR = 0;
		url = "https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="
				+ id
				+ "&endTime=12/31/"
				+ currentYear
				+ "%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=";
		doc = Jsoup.connect(url).get();
		elements = doc.select("div.tabelle table tr td.base-grid-header-cell");
		if(elements.size()==0){
			getDailyReadings();
			
		}else{
		readingsUnit = elements.get(1).text().split("\\[")[1].split("]")[0];
			for (int i = StartingYear; i < currentYear + 1; i++) {
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
						format = new SimpleDateFormat("MMM yy");
						Date date = (Date)format.parse(el.select("td").get(0).text());
						//String d=el.select("td").get(0).text();
						//Timestamp ts = new Timestamp(((java.util.Date)format.parse(d).getTime());
						Long timestamp = date.getTime();
						String power=el.select("td").get(1).text();
						if(power.isEmpty()==false){
							powerR = Double.parseDouble(power);
						}
						//Metric obj= new Metric(timestamp,powerR);
						//monthlyReadings.add(new BasicDBObject("timestamp",timestamp));
						BasicDBObject bdbo = new BasicDBObject();
						bdbo.append("timestamp", timestamp);
						bdbo.append("value", powerR);
						monthlyReadings.add(bdbo);
					}
					break;
				}
			}
		}
	}

	private void getDailyReadings() throws IOException, ParseException {
		double powerR;
		int month = now.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR);
        now.clear();
        do{
        	now.set(Calendar.YEAR, year);
        	powerR=0;
        	for (int currentMonth = month; currentMonth >0; currentMonth--) {
                now.set(Calendar.MONTH, currentMonth-1);
                //last day
                int day=now.getActualMaximum(Calendar.DAY_OF_MONTH);
                String date=currentMonth+"/"+day+"/"+year;
                url="https://www.sunnyportal.com/Templates/PublicChartValues.aspx?ID="+id+"&endTime="+date+"%2011:59:59%20PM&splang=en-US&plantTimezoneBias=180&name=";
                doc=Jsoup.connect(url).get();
                elements = doc
						.select("div.tabelle table tr[class^=base-grid-item]");
				if (elements.size() > 12) {
					for (Element el : elements) {
						String power=el.select("td").get(1).text();
						if(power.isEmpty()==false){
							powerR = powerR+Double.parseDouble(power);
						}
					}
					format = new SimpleDateFormat("MM/dd/yyyy");
					Date d = format.parse(date);
					Long timestamp = d.getTime();
					//monthlyReadings.add(new BasicDBObject("timestamp",timestamp));
					BasicDBObject bdbo = new BasicDBObject();
					bdbo.append("timestamp", timestamp);
					bdbo.append("value", powerR);
					monthlyReadings.add(bdbo);
				}else{
					break;
				}
            }
        	year=year-1;
        	month=12;
        }while(elements.size()>0);
		
	}

	public void SaveInfo(String Connetion) {
		MongoClientURI uri = new MongoClientURI(Connetion);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DBCollection collection = db.getCollection("PVSystemProfiles");
		DBObject prof = new BasicDBObject("_id", plant).append("Owner", Owner)
				.append("Location", Location).append("StartDate", StartDate)
				.append("SystemPower", Power)
				.append("SystemAnnualProduction", AnProduction)
				.append("CO2", CO2).append("Azimuth", Azimuth)
				.append("Inclination", Inclination)
				.append("Communication", Communication)
				.append("Modules", Modules).append("Inverter", Inverter)
				.append("Sensors", Sensors).append("Image", imgLink)
				.append("descinfo", descinfo)
				.append("readingsUnit", readingsUnit)
				.append("monthlyReadings", monthlyReadings);
		

		collection.insert(prof);
		System.out.println("Information Saved for " + Owner);
	}
}
