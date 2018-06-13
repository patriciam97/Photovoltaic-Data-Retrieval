import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractInformation {
	private static Document doc = null;
	public static ArrayList<String> urls= new ArrayList<String>();
	public static String doclink = "https://www.sunnyportal.com/Templates/publicPagesPlantList.aspx"; //domain of webpage
	public static int numOfPages=0;
	public static ArrayList<String> main(String[] args) throws IOException, InterruptedException{
		Connect();
		Thread.sleep(4000);
		numOfPages=getNumOfPages();
		for (int i=0;i<numOfPages;i++){
			ExtractUrls();
		}
		urls=ManipulateUrls(); //path to each PV System Profile
//		for (int i=0;i<urls.size();i++){
//			System.out.println(urls.get(i));
//		}
		return urls;
	}
	public static void Connect() {
		//connects to the website 
		try {
			doc = Jsoup.connect(doclink).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static int getNumOfPages(){
		int pg=0;
		Element table= doc.getElementById("ctl00_ContentPlaceHolder1__dataGridPagerDown_PagerTable");
		Elements pages=table.select("tr td a"); 
		//the last element is the total number of the pages
		pg=Integer.parseInt(pages.last().text());
		return pg;
	}
	public static void ConnectToEachPage(int pg){
		doclink = "https://www.sunnyportal.com/Templates/publicPagesPlantList.aspx?PageIndex="+pg;
		try {
			doc = Jsoup.connect(doclink).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void ExtractUrls(){
		//extracts the url for each PV System Profile
		//Select all elements with an href tag
		Elements reportContent = doc.select("table[class=base-grid] tr td  	a[href]"); 
		int urlssize= reportContent.size();
		//Add all hrefs in an ArrayList called urls
		for (int i=0;i<urlssize;i++) {
			Element el=reportContent.get(i);
			String link=el.attr("href").toString();
			urls.add(link);
		}

	}
	public static ArrayList<String> ManipulateUrls(){
		//extracts the path for each PV System Profile
		ArrayList<String> updatedUrls= new ArrayList<String>();
		String substrings[]=null;
		for (int i=0;i<urls.size();i++) {
			if (urls.get(i).toString().contains("OpenPlant")){	
				//splits the original url we have extracted at these characters
				substrings=urls.get(i).split("[?']"); 
				//substring[2] corresponds to the one we want
				updatedUrls.add(substrings[2]);
			}
		}
		return updatedUrls;
	}
	
}