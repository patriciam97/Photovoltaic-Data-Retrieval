public class myThread implements Runnable {
	  private int sleepFor;

	  public myThread(int sleepFor) {
	    this.sleepFor = sleepFor;
	  }

	  public void run() {
	    System.out.printf("[%s] thread starting\n",
	    Thread.currentThread().toString());
	    try { Thread.sleep(this.sleepFor); }
	    catch(InterruptedException ex) {}
	    System.out.printf("[%s] thread ending\n",
	    Thread.currentThread().toString());
	  }

}
