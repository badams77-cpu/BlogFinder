package BlogFinder;

public class Logging {



  public static final int SEVERE = 7;
  public static final int WARNING = 6;
  public static final int INFO = 5;
  public static final int CONFIG = 4;
  public static final int FINE = 3;
  public static final int FINER = 2;
  public static final int FINEST = 1;

  public static void log(int level, String s){  
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    if (group instanceof LoggingThreadGroup){
      LoggingThreadGroup ltg = (LoggingThreadGroup) group;
      ltg.log(level, s);
    }
  }


  public static void log(int level, String s, Throwable t){  
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    if (group instanceof LoggingThreadGroup){
      LoggingThreadGroup ltg = (LoggingThreadGroup) group;
      ltg.log(level, s, t);
    }
  }



  public static void severe(String s){ log(SEVERE,s); }
  public static void warning(String s){ log(WARNING,s); }
  public static void info(String s){ log(INFO,s); }
  public static void config(String s){ log(CONFIG,s); }
  public static void  fine(String s){ log(FINE,s); }
  public static void  finer(String s){ log(FINER,s); }
  public static void  finest(String s){ log(FINEST,s); }
//   Also with exceptions

  public static void  severe(String s, Throwable t){ log(SEVERE,s,t); }
  public static void  warning(String s, Throwable t){ log(WARNING,s,t); }
  public static void  info(String s, Throwable t){ log(INFO,s,t); }
  public static void  config(String s, Throwable t){ log(CONFIG,s,t); }


}