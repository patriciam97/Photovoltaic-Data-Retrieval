import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
	/**#
	 * 
	 * @author Patricia M.
	 *
	 */
public class WebCrawler {
	public static String Country;  //desired country selected by the user
	public static String maxPages; //maximumPages to retrieve,specifies by the user
	public static int SleepLimit;
	public static String dbConn;
	public static String url="https://www.sunnyportal.com/Templates/publicPagesPlantList.aspx?";
	public static Document doc;
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {
		ArrayList<String> countrylist = null;
		long startTime = System.nanoTime();
		Country=args[0];
		maxPages=args[1];
		SleepLimit=20;
		String configtxt="configurations.txt";
		Database db= new Database(configtxt);
		dbConn=db.getConnection();
		PVSystemCrawler prof;
		DirectoryCrawler Dc= new DirectoryCrawler(url,maxPages,Country);
		ArrayList<String> urls=Dc.GetUrls(doc); //urls of desired systems
		ArrayList<String> systemlist=Dc.getSystemList(); //their location
		ArrayList<String> powerlist=Dc.getPowerList(); //their power
		ArrayList<String> citylist=Dc.getCityList(); //their location
		ArrayList<String> zipcodelist=Dc.getZipCodeList(); //their location
		if(Country.toLowerCase().equals("all")){
			countrylist=Dc.getCountryList(); //their location
		}
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
				if(Country.toLowerCase().equals("all")){
					prof= new PVSystemCrawler(dbConn,urls.get(i),systemlist.get(i),powerlist.get(i), citylist.get(i),countrylist.get(i),zipcodelist.get(i));
				}else{
					prof= new PVSystemCrawler(dbConn,urls.get(i),systemlist.get(i),powerlist.get(i), citylist.get(i),Country,zipcodelist.get(i));
				}
					prof.getProfileInfo();
				String[] subpages = prof.getUrlOfSubpage();
				if (subpages[0] != "nosubpage") { //if subpage doesn't exist, then readings dont exist
					prof.getMonthlyReadings(urls.get(i), subpages);
				}
				prof.SaveInfo(); //saves or updates the system's information

		}
				
		//calculates the elapsed time
		long estimatedTime = (System.nanoTime() - startTime);
		estimatedTime=TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);
		long mins= estimatedTime / 60;
		long secs= estimatedTime % 60;
		System.out.format("Elapsed Time: %d minutes and %d seconds.",mins, secs);
	}


}
