package BlogFinder;

import java.net.*;
import java.util.*;

public class CookieManager {

  private Hashtable cookies;

  public CookieManager() {
    cookies = new Hashtable();
  }

  public void setCookie(String cookie){
    System.err.println("Cookie "+cookie);
    Logging.finer("Saving cookie "+cookie);
    StringTokenizer tok = new StringTokenizer(cookie," ;");
    String name=null;
    String value=null;
    String path = "";
    while(tok.hasMoreTokens()){
      String el = tok.nextToken();
      int i = el.indexOf('=');
      if (i<1){ continue;}
      String name1 = el.substring(0,i);
      String value1 = el.substring(i+1);
      if (name1.equalsIgnoreCase("path")){
        path = value1;
        continue;
      }
      if (name1.equalsIgnoreCase("expires")){ continue; }
      if (name1.equalsIgnoreCase("domain")){ continue; }
      name = name1;
      value = value1;
    }
    if (name!=null && value!=null){
      setCookie(path,name,value);
    }
  }

  private void setCookie(String path,String name,String value){
    System.err.println("Adding coookie "+path+" "+name+"="+value);
    Logging.fine("Adding coookie "+path+" "+name+"="+value);
    if (path.equals("")){ path = "/"; }
    Hashtable pathHash = (Hashtable) cookies.get(path);
    if (pathHash==null){
      pathHash = new Hashtable();
      cookies.put(path,pathHash);
    }
    pathHash.put(name,value);
  }

  public String getCookiesForURL(URL url){
    String path = url.getFile();
    if (!path.startsWith("/")){
      path = "/"+path;
    }
    StringBuffer retBuf = new StringBuffer();
    for(Enumeration p= cookies.keys();p.hasMoreElements();){
      String cookiePath = (String) p.nextElement();
      if (path.startsWith(cookiePath)){
        Hashtable pathHash = (Hashtable) cookies.get(cookiePath);
        for(Enumeration n=pathHash.keys();n.hasMoreElements();){
          String name = (String) n.nextElement();
          String value = (String) pathHash.get(name);
          if (retBuf.length()!=0){
            retBuf.append("; ");
          }
          retBuf.append(name);
          retBuf.append("=");
          retBuf.append(value);
        }
      }
    }
    if (retBuf.length()!=0){ System.err.println("Sending cookie "+retBuf); Logging.fine("Sending cookie "+retBuf); }
    return retBuf.toString();
  }

}

