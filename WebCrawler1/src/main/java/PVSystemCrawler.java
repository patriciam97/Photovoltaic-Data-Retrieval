import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class PVSystemCrawler {
	private String Conn;
	private String url;
	private String Owner,Location,Operator,StartDate,Power,AnProduction,CO2,Modules,Azimuth,Inclination,Communication,Inverter,Sensors,PVType,InverterType,SupportType,Telephone;
	private Document doc;
	private Elements elements;
	public PVSystemCrawler(String Conn,String path) throws IOException {
		this.Conn=Conn;
		this.url="https://www.sunnyportal.com/Templates/PublicPageOverview.aspx?"+path;
		getProfileInfo();
		ShowProfInfo();
	}
	public void getProfileInfo() throws IOException{
		String[] descinfo;
		doc=Jsoup.connect(url).get();
		//extracting the title
		elements=doc.select("head");
		Owner=elements.get(0).text().toString();
		//extracting the location
		elements=doc.select("td[class=PlantProfileCellValue BoxRoundCornerLineVRight]");
		Location=elements.get(0).text().toString();
		Operator=elements.get(1).text().toString();
		StartDate=elements.get(2).text().toString();
		Power=elements.get(3).text().toString();
		AnProduction=elements.get(4).text().toString();
		CO2=elements.get(5).text().toString();
		Modules=elements.get(6).text().toString();
		Azimuth=elements.get(7).text().toString();
		Inclination=elements.get(8).text().toString();
		Communication=elements.get(9).text().toString();
		Inverter=elements.get(10).text().toString();
		Sensors=elements.get(11).text().toString();
		elements=doc.select("span");
		descinfo=elements.get(3).text().split(":");
		PVType=descinfo[0];
		InverterType=descinfo[1];
		SupportType=descinfo[2];
		Telephone=descinfo[3].replaceAll("\\D+","");
				
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
		System.out.println(Inverter);
		System.out.println(Sensors);
		System.out.println(PVType);
		System.out.println(InverterType);
		System.out.println(SupportType);
		System.out.println(Telephone);
		
	}

}
