import java.io.IOException;
import java.util.ArrayList;


public class WebCrawler {
	public static String Country;  //desired country selected by the user
	public static String maxPages; //maximumPages to retrieve,specifies by the user
	public static int SleepLimit;
	public static String dbConn;
	public static String url="https://www.sunnyportal.com/Templates/publicPagesPlantList.aspx?";
	public static void main(String[] args) throws IOException {
		Country=args[0];
		maxPages=args[1];
		SleepLimit=20;
		String configtxt="configurations.txt";
		Database db= new Database(configtxt);
		dbConn=db.getConnection();
		DirectoryCrawler Dc= new DirectoryCrawler(url,maxPages,Country);
		ArrayList<String> urls=Dc.GetUrls();
		for(int i=0;i<urls.size();i++){
			System.out.println(urls.get(i).toString());
		}
		
	}

}
