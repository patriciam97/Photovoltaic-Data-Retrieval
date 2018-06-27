import java.io.IOException;
import java.util.ArrayList;


public class myThread  implements Runnable {
	private Thread t;
	private String threadName;
	private DirectoryCrawler1 Dc;
	private int maxPages;
	private String dbConn;
	private String url;
	private int frompg;
	private ArrayList<String> urls =null;
	public boolean done=false;
	
	public myThread(String threadName,String dbConn,String url,int mxPgs,int pg) {
		 this.threadName = threadName;
		 this.dbConn=dbConn;
		 this.url=url;
		 this.maxPages=mxPgs;
		 this.frompg=pg;
	     System.out.println("Creating " +  threadName );
	}

	public void run() {
		try
        {
            // Displaying the thread that is running
            System.out.println (threadName+" is running");
            geturls();
            System.out.println(threadName+" is completed.");
            done=true;
        }
        catch (Exception e)
        {
            // Throwing an exception
            System.out.println ("Exception is caught for "+threadName);
        }
  }
		
  		
		
	public void start () {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
	}
	public void geturls() throws IOException{
		Dc=new DirectoryCrawler1(dbConn,url,"all");
  		urls=Dc.GetUrls(frompg,maxPages);
	  		ArrayList<String> systemList=Dc.getSystemList();
	  		Dc.SaveDirectory(urls);
	
	  		if(urls.size()>0){
	  			System.out.println("Total: "+ (urls.size()));
	  		}else{
	  			System.out.println("No Urls have been extracted.");
	  		}
	}

}
