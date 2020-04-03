package BlogFinder;

import java.io.*;
import java.util.*;

/**
* Central location for the Reading Caching of Any type of Parser Definitions
**/

public class ParserDefinitionStore {

/** Cache for the list of template files **/

  private static Hashtable definitionsTemplateList = new Hashtable(100);

/** cache for the template files themselves **/

  private static Hashtable definitionsCache = new Hashtable(100);

/** This cache will be used instead of reading the definition from the disk **/

  private static Hashtable definitionFileCache = new Hashtable(100);

  private static Hashtable readTemplateListFile(String file) throws IOException {
	File templateFile = new File(Config.config.howindex_dir,file);
	System.err.print(templateFile);
    BufferedReader fr = new BufferedReader(new FileReader(templateFile));
    String line;
    Hashtable templateHash = new Hashtable(93);
    while( (line = fr.readLine()) !=null){
      StringTokenizer tok = new StringTokenizer(line,"\t");
      if (tok.countTokens()<2){ continue;}
      templateHash.put(tok.nextToken(),tok.nextToken());
    }
    return templateHash;
  }



  public static synchronized Hashtable getTemplateList(String file) throws IOException {
    Hashtable ret = (Hashtable) definitionsTemplateList.get(file);
    if (ret == null){
      ret = readTemplateListFile(file);
      definitionsTemplateList.put(file,ret);
    }
    return ret;
  } 

  public static synchronized String[] listTemplates(String file) {
    Hashtable temps = null;
    try {
      temps = getTemplateList(file);
    } catch (IOException e){
      return new String[0];
    }
    if (temps == null){ return new String[0]; }
    String ret[] = new String[ temps.size() ];
    int i=0;
    for(Iterator it=temps.keySet().iterator(); it.hasNext(); i++){
      ret[i] = (String) it.next();
    }
    return ret;
  }



  public static synchronized String getDefinitionForURL(Hashtable deflist, String url) throws IOException {
    String defFile = null;
    int lastMatchLength = 0;
    for(Enumeration e=deflist.keys();e.hasMoreElements();){
      String match = (String) e.nextElement();
//      System.err.println("Matching "+url+" "+match);
      if (WildCardMatch.wildCardMatchWildEnd(url,match)){
        if (match.length() >lastMatchLength){
          lastMatchLength = match.length();
          defFile = (String) deflist.get(match);
        }
      }
    }
    if (defFile==null){ return null; }
    Logging.config("Found Regex For "+url+" "+defFile);
    System.err.println("Found Regex For "+url+" "+defFile);
    return defFile;
  }

  public static synchronized void putDefinition(String deffile, ParserDefinition pd){
    definitionsCache.put(deffile, pd);
  }  

  public static synchronized String getDefinitionString(String defFile){
    return (String) definitionFileCache.get(defFile);
  }
  
  public static synchronized ParserDefinition getDefinition(String defFile){
    return (ParserDefinition) definitionsCache.get(defFile);
  }

  public static synchronized BufferedReader getDefinitionReader(String defFile) throws IOException {
    String defString = (String)  definitionFileCache.get(defFile);
    if (defString != null){
      return new BufferedReader(new StringReader(defString),1024);
    }
    return new BufferedReader(new FileReader(new File(Config.config.howindex_dir,defFile)),1024);
  }

/**
  Used by the IndexController to place temporary parser Definitions
**/

  public static synchronized void putDefinitionString(String templateListFile, String pattern, String definitionFileName, String definition) throws IOException {
    Hashtable templatesList = getTemplateList(templateListFile);
    templatesList.put(pattern, definitionFileName);
    definitionFileCache.put(definitionFileName, definition);
    definitionsCache.remove(definitionFileName);
  }

  public static synchronized void removeDefinitionString(String templateListFile, String pattern, String definitionFileName) throws IOException {
    Hashtable templatesList = getTemplateList(templateListFile);
    templatesList.remove(pattern);
    definitionFileCache.remove(definitionFileName);
    definitionsCache.remove(definitionFileName);
  }


}