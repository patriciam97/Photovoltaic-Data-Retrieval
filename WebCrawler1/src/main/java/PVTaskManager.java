import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PVTaskManager implements Runnable {

	private final ExecutorCompletionService<CopyOfPVSystemCrawler> completionService;
	private final ExecutorService executorService;
	int count =0;
	public PVTaskManager(int workers) {
		executorService = Executors.newFixedThreadPool(workers);
		completionService = new ExecutorCompletionService<CopyOfPVSystemCrawler>(executorService);
		new Thread(this).start();
	}
	
	public void submitJob(CopyOfPVSystemCrawler copyOfPVSystemCrawler){
		completionService.submit(copyOfPVSystemCrawler);
		count++;
	}

	public int howManyAreRunning(){
		return count;
	}
	public void run() {
		while(true){
			
			try {
				CopyOfPVSystemCrawler result  = completionService.take().get();
				count--;
			} catch (InterruptedException e) {
				count--;
				e.printStackTrace();
			} catch (ExecutionException e) {
				count--;
				e.printStackTrace();
			}
		}
		
	}

}
