package BlogFinder;

import Distiller.FeedFinder;
import Distiller.Result;
import Distiller.Status;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Vector;
import java.net.URL;

public class FeedJob implements Runnable {

	static int nThreads = 10;
	
	
	
	String urls[];
	int id;
	boolean finished=false;
	boolean notStopped = false;
	int nScanned=0;
	int nResults=0;
	int nFeeds=0;
	int nChecked=0;
	int nToScan=0;
	
	Config conf;
	SpiderThread sThread[];
	Hashtable<String,Result> resultsByLinker;
	Hashtable<String,Result> resultsByPage;
	Hashtable<String,String> linkerToPage;
	Vector<String> toCheck;
	Hashtable<String,String> toCheckHash;
	Thread runner;
	ParserLoader loader;
	SpiderTask tasks[];
	int curTask = 0;
	int curCheck = 0;
	
	public FeedJob(String urls[], int id){
		finished =false;
		nScanned = 0;
		nResults = 0;
		nFeeds = 0;
		this.id = id;
		this.urls = urls;
		SpiderTask task = new SpiderTask(urls,id);
		resultsByLinker = new Hashtable<String,Result>();
		resultsByPage = new Hashtable<String,Result>();
		linkerToPage = new Hashtable<String,String>();
		toCheckHash = new Hashtable<String,String>();
		toCheck = new Vector<String>();
		tasks = task.splitTask();
	}
	
	public void start(){
		loader = BlogFinder.blogFinder.parserLoader;
		conf = BlogFinder.blogFinder.config;
		LoggingThreadGroup spdGrp = BlogFinder.blogFinder.spiderGroup;
		LoggingThreadGroup recGrp = BlogFinder.blogFinder.receiverGroup;
		notStopped = true;
		sThread = new SpiderThread[nThreads];
		int nThr = nThreads;
		curTask=0;
		curCheck=0;
		if (tasks.length<nThr) nThr=tasks.length;
	    for(int i=0; i<nThr; i++){
	    	sThread[i] = new SpiderThread(conf,conf.getLog(),spdGrp);
	    	sThread[i].addSite(tasks[i]);
	    	curTask++;
	    }
	    nToScan=tasks.length;
	    Logging.finer("Starting "+nToScan+" tasks");
	    runner = new Thread(recGrp,this);
	    runner.start();
	}
	
   public void run(){
	  int runTime=0;
	  while(notStopped && runTime<40000){  // No longer than a minute
		for(int i=0;i<sThread.length;i++){
		   SpiderThread st = sThread[i];
		   if (st==null) continue;
		   FlyBall fb = null;
	       if ((fb=st.getFlyBall())==null){ continue; }

	       String page = fb.currLink.toExternalForm();
	       st.addLinks();
	       if (fb.taskId==id+1){ // This marks a link check
	    	   if (!fb.hasData){
	    		   // Checked feed didn't load
	    		   nChecked++;
	    		   resultsByPage.remove(page);
	    		   resultsByLinker.remove(linkerToPage.get(page));
	    	   } else {
	    		   Result res=parse(fb,true);
	    		   if (res!=null){
	    			// Was a feed
	    			   nFeeds++; nChecked++;
	    		   } else {
	    			// wasn't a feed
		    		   nChecked++;
		    		   resultsByPage.remove(page);
		    		   resultsByLinker.remove(linkerToPage.get(page));	    			   
	    		   }
	    	   }
	         } else {
	       // Not an existing result page
	         if (!fb.hasData){
	      	   nScanned++; // Failed download
	         } else {
	    	     Result res = parse(fb,false);
	    	     nScanned++;
	    	     if (res!=null && res.getUrl()!=null){
	    		   try {
	    			   URL u=new URL(fb.url,res.getUrl());
	    			   res.setURL(u.toExternalForm());
	    		   } catch (Exception e){}
	    		   resultsByLinker.put(page,res);
	    		   String resU = res.getUrl();
	    		   if (!resultsByPage.containsKey(resU)){
	    			   resultsByPage.put(resU,res);
	    			   linkerToPage.put(resU, page);
	    			   if (!toCheckHash.containsKey(resU)){
	    				   toCheck.add(resU);
	    				   toCheckHash.put(resU,resU);
	    				   nResults++;
	    			   }
	    		   }
	    	     }
	       	 }
	       }
	       //
		   if (curTask<tasks.length){
			   if (st.addSite(tasks[curTask])){ curTask++; }
		   } else if (curCheck<toCheck.size()){
			   String temp[] = new String[1];
			   temp[0]=toCheck.elementAt(curCheck);
			   SpiderTask stx = new SpiderTask(temp,id+1);
			   if (stx==null || !stx.hasURLs()){ curCheck++; }
			   else if (st.addSite(stx)){ curCheck++; }
		   }
	     }
		 if (nScanned==nToScan && nChecked==curCheck ){ notStopped=false; } else {
		   try {
	         runner.sleep(100);
	         runTime+=100;
		   } catch (InterruptedException e){}
		 }
	   }
	   notStopped = false;
	   for(int i=0;i<nThreads;i++){
		   sThread[i]=null;
	   }
	}
   
   public Status getStatus(){
	   return new Status(nResults,nScanned,nFeeds,nChecked, !notStopped);
   }
   
   public Vector getResults(){
	   Vector res = new Vector<Result>();
	   for(int i=0;i<urls.length;i++){
		   Result r = resultsByLinker.get(urls[i]);
		   if (r!=null){ res.add(r); }
	   }
	   return res;
   }

   public Result parse(FlyBall fly, boolean checkFeed){
	      String encoding = fly.encoding;
	      String type = fly.type;
	      StringTokenizer tok = new StringTokenizer(type,";");
	      if (tok.countTokens()>1){
	        type = tok.nextToken();
	        String var = tok.nextToken("=");
	        if (var.length()<1){ return null; }
	        if (var.charAt(0)==';'){ var = var.substring(1); }
	        int j=0;
	        for(int i=0; i<var.length();i++){
	          if (var.charAt(i)==' '){ j++;} else { break;}
	        }
	        var = var.substring(j);
	        if (var.equalsIgnoreCase("charset")){
	          encoding = tok.nextToken();
	        }
	      }
	      Parser parser = loader.getParser(type);
	      if (parser == null){
	    	Logging.info("No parser for type "+type+" at href="+fly.currLink);
	        fly.spider.addLinks();
	        return null;
	      } else {
	        String filename = fly.url.toExternalForm();
//	        System.err.println("Parsing "+filename);
	        conf.configureParser(parser,filename);
	        parser.reset();
	        parser.setEncoding(encoding);
	        runner.yield();
	        Logging.fine("Starting Parsing "+filename+" with "+parser.getClass().getName());
	        if (parser.useReader){
	          Reader reader;
	          try {
	            reader = new BufferedReader(new InputStreamReader(
	              new ByteArrayInputStream(fly.inputbuffer,0,fly.bufferLength),
	                encoding),1024);
	          } catch (UnsupportedEncodingException e){
	            reader = new BufferedReader(new InputStreamReader(
	              new ByteArrayInputStream(fly.inputbuffer,0,fly.bufferLength)),1024);
	          }
	          try {
	            parser.open(reader);
	          } catch (Exception e){
	            System.err.println( e.toString()+" parsing "+filename);
		        Logging.warning(e.toString()+" parsing "+filename, e);
	          }
	        } else {
	          InputStream is = new OpenByteArrayInputStream(fly.inputbuffer,0,fly.bufferLength);
	          try {
	            parser.open(is);
	          } catch (Exception e){
	        	  Logging.warning(e.toString()+" parsing "+filename, e);
	            }
	          }
	        }
	        if (parser instanceof SelectParser){ parser=((SelectParser) parser).getParser(); }
	        runner.yield();
	        String filename = fly.url.toExternalForm();
	      	parser.setLanguageCode(fly.lang);
	        parser.langAllow = fly.langAllow;
	        parser.setFrameInfo(fly);
	        fly.spider.addLinks();
		      long lastModified = fly.lastModified;
		      lastModified = 3600*(lastModified /3600); // To nearest hour
	        parser.setLastModified(fly.lastModified);
	        if (checkFeed){
	        	if (parser instanceof AtomParser || parser instanceof RSSParser){
	        		Result res = resultsByPage.get(filename);
	        		if (res!=null){
	        			res.setTitle(parser.getTitle());
	        			return res;
	        		}
	        	}
	        } else {
	        // Get the link for the feed
	        	if (parser instanceof FeedLinkHTMLParser){
	        		return ((FeedLinkHTMLParser) parser).getFeed();
	        	}
	        }
            return null;
	    }   
   
}
