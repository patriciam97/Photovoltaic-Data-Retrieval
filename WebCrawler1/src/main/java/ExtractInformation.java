import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;



public class ExtractInformation {
	
	public static void  main(String[] args) throws InterruptedException, IOException{				

	    
		String doclink="https://www.sunnyportal.com";  //domain name of the webpage
		Document doc=null;
		doc=Jsoup.connect(doclink).get();  //creates a new connection,fetches and parses the HTML file
		Thread.sleep(4000);  
		Elements newsHeadlines = doc.select("#ctl00_ContentPlaceHolder1_HyperLinkPublicPlants");
		for (Element headline : newsHeadlines) {
		  doclink=headline.absUrl("href"); 
		}
		try {
			doc = Jsoup.connect(doclink).get();  //connecting to the website with the infomation we're interested
			Thread.sleep(4000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(doc.title());
		Elements reportContent = doc.select("table[class=base-grid] tr td  	a[href^=javascript:OpenPlant(]");
		for (Element headline : reportContent) {
			System.out.printf("%s %s \n",headline.text(),headline.absUrl("href"));
		}
		
	}
	}