
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 
 * @author Patricia M.
 *
 */
public class DirectoryCrawler {
	private String url;
	private static String Country;
	private static int maxPages;
	private ArrayList<String> urls= new ArrayList<String>();
	private ArrayList<String> fullurls = new ArrayList<String>();
	private ArrayList<String> powerList = new ArrayList<String>();
	private ArrayList<String> cityList = new ArrayList<String>();
	private ArrayList<String> countryList = new ArrayList<String>();
	private ArrayList<String> zipcodeList = new ArrayList<String>();
	private ArrayList<String> systemList = new ArrayList<String>();
	private static Connection.Response response;
    static String viewState = "";
	static String viewStateGen="";
	static String eventArgument="";
	static Map<String,String> cookies = null;
	static Document doc;
	private static int counter=0;
	
	/**
	 * 
	 * @param url base url of website
	 * @param maxPgs maximum pages to crawl
	 * @param Country country we are investigating
	 * @throws IOException 
	 */
	public DirectoryCrawler(String url, String maxPgs, String Country) throws IOException {
		this.url = url;
		maxPgs=maxPgs.toLowerCase();
		if ( maxPgs.equals("all") ) {
			this.maxPages = getMaximumPages(url,Country.toUpperCase());
		} else {
			this.maxPages = Integer.parseInt(maxPgs);
		}
		this.Country = Country.toUpperCase();
	}


	private static Document Connect(String url,String country, int pg, boolean conn) throws IOException{
		String eT="";
	    String search= "";	
	    
	    if (conn==true) {
	    	search="Search";
			response = Jsoup.connect(url)
		            .method(Connection.Method.GET)
		            .timeout(1000000)
		            .execute();
			cookies= response.cookies();
		    doc = response.parse();
		    viewState = doc.select("input[name=__VIEWSTATE]").first().attr("value").toString();
		    viewStateGen = doc.select("input[name=__VIEWSTATEGENERATOR]").first().attr("value").toString();	
		    eventArgument = "";		    
	    }
	    
	    if (pg>=0){
	    	//eT="ctl00$ContentPlaceHolder1$_dataGridPagerUp$ClickPageNo"+pg;
	    	eT= doc.select("span a.base-grid-pager-title").get(pg).id();
	    }

	    if (country.equals("ALL")){
	    	country="";
	    	search="";
	    }

	    response= Jsoup.connect(url)
	                .data("ctl00$ContentPlaceHolder1$CountryDropDownList",country)
	                .data("ctl00$ContentPlaceHolder1$FilterButton",search)              
	                .data("__VIEWSTATE",viewState)
	                .data("__VIEWSTATEGENERATOR",viewStateGen)
	                .data("__EVENTTARGET",eT)
	                .data("__EVENTARGUMENT",eventArgument)
	                .data("ctl00$ContentPlaceHolder1$PlantNameFilterTextBox","")
	                .data("ctl00$ContentPlaceHolder1$ZipFilterTextBox","")
	                .data("ctl00$ContentPlaceHolder1$CityFilterTextBox","")
	                .data("ctl00$ContentPlaceHolder1$FromPeakPowerNumTB$numTB","")
	                .data("ctl00$ContentPlaceHolder1$FromPeakPowerNumTB$numTB_hidden","")
	                .data("ctl00$ContentPlaceHolder1$FromPeakPowerNumTB$numTB_max","")
	                .data("ctl00$ContentPlaceHolder1$ToPeakPowerNumTB$numTB","")
	                .data("ctl00$ContentPlaceHolder1$ToPeakPowerNumTB$numTB_hidden","")
	                .data("ctl00$ContentPlaceHolder1$ToPeakPowerNumTB$numTB_max","")
	                .data("ctl00$ContentPlaceHolder1$FromPeakPowerNumTB$numTB_min","0")
	                .data("ctl00$ContentPlaceHolder1$ToPeakPowerNumTB$numTB_min","0")
	                .data("ctl00$ContentPlaceHolder1$_dataGridPagerUp$ClickImgFirst.x","")
	                .data("ctl00$ContentPlaceHolder1$_dataGridPagerUp$ClickImgFirst.y","")
	                .cookies(cookies)
	                .timeout(100000)
	                .method(Connection.Method.POST)
	                .execute();
	    
	   doc=response.parse();
	   viewState=doc.select("input[name=__VIEWSTATE]").first().attr("value").toString();
	   cookies.putAll(response.cookies());
	   viewStateGen=doc.select("input[name=__VIEWSTATEGENERATOR]").first().attr("value").toString();
	   eventArgument=doc.select("input[name=__EVENTARGUMENT]").first().attr("value").toString();
	   return doc;
	}
	/**
	 * 
	 * @return pg maximum number of pages
	 * @throws IOException 
	 */
	private static int getMaximumPages(String url,String country) throws IOException {
		
		int pg = 0;
		Document doc=Connect(url,country,-1,true);
		Element table = doc
				.getElementById("ctl00_ContentPlaceHolder1__dataGridPagerDown_PagerTable");
		Elements pages = table.select("tr td a");
		// the last element is the total number of the pages
		pg = Integer.parseInt(pages.last().text());
		System.out.println("Total num of pages: "+pg);
		return pg+1;
	}

	/**
	 * 
	 * @return urls of all systems from the Directory page
	 * @throws IOException 
	 */
	public ArrayList<String> GetUrls(String url) throws IOException {
		Document doc;
		for (int i = 0; i < maxPages; i++) {
			counter=0;
			doc=ConnectToEachPage(url,i);
			ExtractUrls(doc);
			System.out.println("Extracted "+ counter + " urls from page "+ (i+1)+".");
		}
		this.urls = ManipulateUrls(); // path to each PV System Profile
		return urls;
	}
	
	/**
	 * this function connects to a page
	 * @param pg page number to crawl
	 * @throws IOException 
	 */
	private static Document ConnectToEachPage(String url,int pg) throws IOException {
		System.out.println("Connecting to page: " + (pg+1));
		Document doc= Connect(url,Country,pg,false);
		return doc;
	}
	/**
	 *  this function extracts the full url for each PV System Profile
	 */
	private void ExtractUrls(Document doc) {
		Elements reportContent;
		
		// Select all elements with an href tag
		if (Country.toLowerCase().equals("all")==false){
			reportContent = doc
					.select("table[class=base-grid] tr[class^=base-grid-item]");
			for (Element el : reportContent) {
						if(fullurls.contains(el.select("td a[href]").get(0).attr("href").toString())==false){
							getDetails(el);
							fullurls.add(el.select("td a[href]").get(0).attr("href").toString());
						}
			
			}
		}else{ //if we are looking for all countries
			reportContent = doc.select("table[class=base-grid] tr td a[href]"); 
			for (Element el : reportContent) {
				if(fullurls.contains(el.select("td a[href]").get(0).attr("href").toString())==false){
					getDetails(el);
					countryList.add(el.select("td").get(2).text().toString());
					fullurls.add(el.select("td a[href]").get(0).attr("href").toString());
				}
			}
			
		}
		this.urls = fullurls;
	}
	private void getDetails(Element el){
		//PV is located in the country we're interested in
		counter++;
		//get the its url,power and location		
		powerList.add(el.select("[align=right]").get(0).text().toString());
		cityList.add(el.select("td").get(4).text().toString());
		zipcodeList.add(el.select("td").get(3).text().toString());
		systemList.add(el.select("td").get(1).text().toString());

	}
	/**
	 * 
	 * @return all paths for each PV System(not fully url)
	 */
	private ArrayList<String> ManipulateUrls() {
		ArrayList<String> updatedUrls = new ArrayList<String>();
		String substrings[] = null;
		
		for (int i = 0; i < urls.size(); i++) {
			//for all urls available
			if (urls.get(i).toString().contains("OpenPlant")) {
				// splits the original url we have extracted at these characters
				substrings = urls.get(i).split("[=&]");
				// substring[2] corresponds to the one we want
				updatedUrls.add(substrings[1]);
			}
		}
		return updatedUrls;
	}
	
	/**
	 * 
	 * @return the power list from the Directory page
	 */
	public ArrayList<String> getPowerList(){
		return powerList;
		
	}
	/**
	 * 
	 * @return the location list from the Directory page
	 */
	public ArrayList<String> getCityList(){
		return cityList;
		
	}
	/**
	 * 
	 * @return the zip code list from the Directory page
	 */
	public ArrayList<String> getZipCodeList(){
		return zipcodeList;
		
	}
	/**
	 * 
	 * @return the country list from the Directory page
	 */
	public ArrayList<String> getCountryList(){
		return countryList;
		
	}
	/**
	 * 
	 * @return the system list from the Directory page
	 */
	public ArrayList<String> getSystemList(){
		return systemList;
		
	}

}
