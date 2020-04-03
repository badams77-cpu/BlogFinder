package BlogFinder;

import java.util.Hashtable;
import java.util.Vector;
import Distiller.Result;
import Distiller.FeedFinder;
import Distiller.Status;

public class FinderEngine implements Distiller.FeedFinder {

	public Hashtable<String,FeedJob> feedJobs; 
	
	public FinderEngine(){
		feedJobs = new Hashtable<String, FeedJob>();
	}
	
	@Override
	public Vector getResults(String id) {
	       FeedJob fj = (feedJobs.get(id));
	       if (fj==null) return null;
	       return fj.getResults();
	}

	@Override
	public Status searchStatus(String id) {
       FeedJob fj = feedJobs.get(id);
       if (fj==null){
    	   System.err.println("Job '"+id+"' no job found");
    	   return null;
       }
       return fj.getStatus();
	}

	@Override
	public String startSearch(String[] urls) {
		if (urls.length==0) return null;
		if (feedJobs==null){ feedJobs = new Hashtable<String,FeedJob>(); }
		synchronized(feedJobs){
			int code = urls.hashCode();
			String jobString = hexString(8,code);
			int time = (int) (System.currentTimeMillis()%65535L );
			jobString = jobString.concat(hexString(4,time));
			FeedJob job = new FeedJob(urls,code);
			feedJobs.put(jobString,job);
			job.start();
			return jobString;			
		}
	}

	private String hexString(int len,int code){
		String hex = Integer.toHexString(code);
		int l = hex.length();
		return "00000000".substring(0,len-l).concat(hex);
	}
	
}
