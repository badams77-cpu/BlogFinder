
package BlogFinder;

import java.util.*;
import java.io.*;

public class MimeTypes {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 2007 all Rights Reserved";


  Hashtable hash;
  String defaultType;

  public MimeTypes( String file,String def){
    hash = new Hashtable();
    defaultType = def;
    load(file);
  }

  private void load(String file){
    try {
      FileReader fr = new FileReader(file);
      LineNumberReader lnr = new LineNumberReader(fr);
      String line;
      while( (line = lnr.readLine()) != null){
        StringTokenizer tok = new StringTokenizer(line);
	      String type = "";
        Vector extensions = new Vector();
        try {
          type = tok.nextToken();
	        while(tok.hasMoreTokens()){
            String token = tok.nextToken();
	          if (token.length() != 0){
  	          extensions.addElement(token);
	          }
          }
        } catch( NoSuchElementException e){continue;}
        if (type.charAt(0) == '#'){ continue;}
        for(Enumeration e= extensions.elements();e.hasMoreElements();){
          hash.put(e.nextElement(),type);
        }
      }
      lnr.close();
      fr.close();
    } catch (Exception e){
      Logging.severe("Error loading Mimetypes "+e);
      System.err.println(e);
    }
  } 

  public String getType(String filename){
    StringTokenizer tok = new StringTokenizer(filename,".");
    String exten = "";
    while(tok.hasMoreTokens()){
      exten = tok.nextToken();
    }
    exten = exten.toLowerCase();
    String type = (String) hash.get(exten);
//    System.err.println("mime "+type+""+filename);
    if (type != null){return type;} else {return defaultType;}
  }

}