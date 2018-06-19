import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
public class WebCrawler {
	public static String Country;  //desired country selected by the user
	public static String maxPages; //maximumPages to retrieve,specifies by the user
	public static int SleepLimit;
	public static String dbConn;
	public static String url="https://www.sunnyportal.com/Templates/publicPagesPlantList.aspx?";
	public static void main(String[] args) throws IOException, ParseException {
		Country=args[0];
		maxPages=args[1];
		SleepLimit=20;
		String configtxt="configurations.txt";
		Database db= new Database(configtxt);
		dbConn=db.getConnection();
		DirectoryCrawler Dc= new DirectoryCrawler(url,maxPages,Country);
		ArrayList<String> urls=Dc.GetUrls();
		ArrayList<String> powerlist=Dc.getPowerList();
		if(urls.size()>0){
			if (urls.size()==1){
				System.out.println(urls.size()+" Url have been extracted.");
			}else{
				System.out.println(urls.size()+" Urls have been extracted.");
			}
		}else{
			System.out.println("No Urls have been extracted.");
		}
		for (int i=0;i<urls.size();i++){
			PVSystemCrawler prof= new PVSystemCrawler(dbConn,urls.get(i),powerlist.get(i));
		}
		//PVSystemCrawler prof= new PVSystemCrawler(dbConn,"3d89382c-deec-40dc-a105-d2b0cb4318b8","efdsf");
	}


}
