package main;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DCTaskManager implements Runnable {

	private final ExecutorCompletionService<DirectoryCrawler1> completionService;
	private final ExecutorService executorService;
	int count =0;
	public DCTaskManager(int workers) {
		executorService = Executors.newFixedThreadPool(workers);
		completionService = new ExecutorCompletionService<DirectoryCrawler1>(executorService);
		new Thread(this).start();
	}
	
	public void submitJob(DirectoryCrawler1 job){
		completionService.submit(job);
		count++;
	}

	public int howManyAreRunning(){
		return count;
	}
	public void run() {
		while(true){
			
			try {
				DirectoryCrawler1 result  = completionService.take().get();
				count--;
				System.err .println("Remaining jobs: "+count);
				if(result.getUrls().size()>0){
		  			System.out.println("Total: "+ (result.getUrls().size()));
		  		}else{
		  			System.out.println("No Urls have been extracted.");
		  		}
			} catch (InterruptedException e) {
				count--;
				e.printStackTrace();
			} catch (ExecutionException e) {
				count--;
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
