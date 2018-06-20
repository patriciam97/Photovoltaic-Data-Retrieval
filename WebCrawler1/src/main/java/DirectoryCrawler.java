import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DirectoryCrawler {
	private static String url;
	private String Country;
	private static int maxPages;
	private static Document doc;
	private ArrayList<String> urls= new ArrayList<String>();
	private ArrayList<String> fullurls = new ArrayList<String>();
	private ArrayList<String> powerList = new ArrayList<String>();
	private ArrayList<String> locList = new ArrayList<String>();
	private static int counter=0;
	public DirectoryCrawler(String url, String maxPgs, String Country) {
		this.url = url;
		maxPgs=maxPgs.toLowerCase();
		if ( maxPgs == "all" ) {
			this.maxPages = getMaximumPages();
		} else {
			this.maxPages = Integer.parseInt(maxPgs);
		}
		this.Country = Country.toLowerCase();
	}

	private static int getMaximumPages() {
		int pg = 0;
		Element table = doc
				.getElementById("ctl00_ContentPlaceHolder1__dataGridPagerDown_PagerTable");
		Elements pages = table.select("tr td a");
		// the last element is the total number of the pages
		pg = Integer.parseInt(pages.last().text());
		return pg;
	}

	public ArrayList<String> GetUrls() {
		for (int i = 0; i < maxPages; i++) {
			counter=0;
			ConnectToEachPage(i);
			ExtractUrls();
			System.out.println("Extracted "+ counter + " urls from page "+ (i+1)+".");
		}
		this.urls = ManipulateUrls(); // path to each PV System Profile
		return urls;
	}
	public ArrayList<String> getPowerList(){
		return powerList;
		
	}
	public ArrayList<String> getLocationList(){
		return locList;
		
	}
	private static void ConnectToEachPage(int pg) {
		String link = url + "PageIndex=" + pg;
		System.out.println("Connecting to page: " + (pg+1));
		try {
			doc = Jsoup.connect(link).timeout(100000).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void ExtractUrls() {
		Elements reportContent;
		// extracts the url for each PV System Profile
		// Select all elements with an href tag
		if (Country.equals("all")==false){
			reportContent = doc
					.select("table[class=base-grid] tr[class^=base-grid-item]");
			for (Element el : reportContent) {
					String temp = el.select("td span").text().toLowerCase();
					if (temp.compareTo(this.Country) == 0) {
						counter++;
						powerList.add(el.select("[align=right]").get(0).text().toString());
						locList.add(el.select("td").get(4).text().toString());
						fullurls.add(el.select("td a[href]").get(0).attr("href").toString());
					}
			}
		}else{
			reportContent = doc.select("table[class=base-grid] tr td a[href]"); 
			for (Element el : reportContent) {
				String link=el.attr("href").toString();
				counter++;
				powerList.add(el.select("[align=right]").get(0).text().toString());
				fullurls.add(link);
			}
		}
		this.urls = fullurls;

	}

	private ArrayList<String> ManipulateUrls() {
		// extracts the path for each PV System Profile
		ArrayList<String> updatedUrls = new ArrayList<String>();
		String substrings[] = null;
		for (int i = 0; i < urls.size(); i++) {
			if (urls.get(i).toString().contains("OpenPlant")) {
				// splits the original url we have extracted at these characters
				substrings = urls.get(i).split("[=&]");
				// substring[2] corresponds to the one we want
				updatedUrls.add(substrings[1]);
			}
		}
		return updatedUrls;
	}

}
