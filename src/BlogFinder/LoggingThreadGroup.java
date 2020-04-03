
package BlogFinder;

import java.util.logging.*;

/**
*   The logging thread group directs allows Log message to be routed to a logger decided by the ThreadGroup
* @author BDOA Adams
**/

public class LoggingThreadGroup extends ThreadGroup {

  private Logger mylogger;

  public LoggingThreadGroup(String groupname, String loggername){
    super(groupname);
    mylogger = Logger.getLogger(loggername);
    mylogger.setUseParentHandlers(false);
    mylogger.setLevel(Level.ALL);
  }

  public void addHandler(Handler handler){
    mylogger.addHandler(handler);
  }

  public void uncaughtException(Thread t,Throwable e){
    if (e instanceof ThreadDeath){ super.uncaughtException(t,e); return; }
    mylogger.log(Level.SEVERE, "Exception Caught by LoggingThreadGroup"+e, e);
  } 

  public void log(int level, String msg){
    mylogger.logp (getLevel(level),"","", msg);
  }

  public void log(int level, String msg, Throwable e){
    mylogger.log( getLevel(level),msg,e);
  }

  private Level getLevel(int levelcode){
//    if (levelcode==Logging.SEVERE){ return Level.SEVERE; }
    if (levelcode==Logging.WARNING){ return Level.WARNING; }
    if (levelcode==Logging.INFO){ return Level.INFO; }
    if (levelcode==Logging.CONFIG){ return Level.CONFIG; }
    if (levelcode==Logging.FINE){ return Level.FINE; }
    if (levelcode==Logging.FINER){ return Level.FINER; }
    if (levelcode==Logging.FINEST){ return Level.FINEST; }
    return Level.SEVERE; // Assume the worst for an unknown level
  }

}
