package BlogFinder;

import java.io.File;
import java.util.logging.ConsoleHandler;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import Distiller.FeedFinder;


public class BlogFinder implements Runnable {

	public static BlogFinder blogFinder; // this task is a singleton so gives a global access method;
	
	Config config;
	LoggingThreadGroup spiderGroup;
	LoggingThreadGroup receiverGroup;
    Thread runner;
    boolean running=false;
    ParserLoader parserLoader;
	
	public BlogFinder(Config conf){
	  config = conf;
	  parserLoader = new ParserLoader(conf.parser_list,conf.parser_dir);
	}
	
	public void start(){
		spiderGroup = new LoggingThreadGroup("spider","spider thread group");
		receiverGroup = new LoggingThreadGroup("receiver","parser thread group");
		java.util.logging.Handler spidHand = new ConsoleHandler();
		java.util.logging.Handler reciHand = new ConsoleHandler();
		try {
			receiverGroup.addHandler( reciHand = new java.util.logging.FileHandler(config.parserLog,false));
		} catch (Exception e){ config.getLog().print("Problem openning process log"+e); }
		try {
			spiderGroup.addHandler(spidHand = new java.util.logging.FileHandler(config.spiderLog,false));
		} catch (Exception e){ config.getLog().print("Problem openning spider Log"+e); }
		runner = new Thread(receiverGroup,this);
		running = true;
		runner.start();
	}
	
    public void rmiStart() {
    	System.setProperty("java.security.policy", config.policyFile);
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "BlogFinder";
            FeedFinder engine = new FinderEngine();
            FeedFinder stub =
                (FeedFinder) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            Logging.warning("RMI: BlogFinder bound");
        } catch (Exception e) {
            Logging.severe("RMI: BlogFinder couldn't be bound: exception:",e);
        }
    }

	
	public void stop(){
		running=false;
	}
	
	public void run(){
	   rmiStart();
	   while(running){
		 try {
		   Thread.sleep(1000);
		 } catch (InterruptedException e){}
	   }
	}
	
	public static void main(String[] args) {
  	      Config conf=null;
	      if (args.length>=1){
	    	  File f = new File(args[0]);
	    	  if (f.exists()){
	    		  conf = new Config(args[0]);
	    	  } else {
	    		  conf = new Config();
	    	  }
	    	  
	      } else {
	    	  conf = new Config();
	      }
	      blogFinder = new BlogFinder(conf);
	      blogFinder.start();
	}
	
}
