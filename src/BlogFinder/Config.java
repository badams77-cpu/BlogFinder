package BlogFinder;


import java.lang.reflect.*;
import java.util.*;
import java.io.*;


public class Config {

  private String configFile = "Conf/engine.conf";

  public int port = 8001;
  public static final String serverName = "Distillation Engine 0.1";
  public String agentName = "Blog Finder BDF0.1";
  public String debugNavigation="";

  public int spiderThreads = 5;
  public int spiderBuffers = 5;
  public int spiderDepth = 20;
  public String spiderDepthSort = "false";
  public long spiderMemory = 10240000;
  public int debugLevel = 0;
  public String log = "";
  public String stoplist = "";
  public String mimetypes = "Conf/mime.types";
  public String def_type = "text/html";
  public String parser_list = "Conf/parser.list";
  public String parser_dir = "bin";
  public String howindex_dir=  "howindex";
  public String fields_delimeter = "";
  public String fields_list = "";
  public String stoplisted_fields = "";
  public String title_field = "";
  public String haltFile = "";
  public String descriptionfield = "";
  public String useRemoteTypes = "";
  public String useBodyHashing = "";
  public String synonyms = "";
  public String makeCatalog = "";
  public String charSet="ISO-8859-1";
  public String corporaFile="";
  public String spacinatorCorporaFile="";
  public String langIgnore="";
  public String frameIndex = "";
  public String virtualIndex = "";
  public String rmiAccessName = "";
  public String rsbridgeAccessName = "";
  public int indexMemory = 10240000;
  public String benchIndex = "";
  public String benchHTML = "";
  public String benchSearch = "";
  public String descMatchBegin = "<B>";
  public String descMatchEnd = "</B>";
  public String descSeperator = "...";
  public String allowHeaderLessHTTP = "false";
  public int descWords = 200;
  public String contextualDescription = "false";
  public String spelling = "false";
  public int nFunctions=0;
  public int nGenerations=0;
  public int nThreads=0;
  public int nearDistance=8;
  public String spacinatorFunction = "";
  public String initialFunction = "";
  public String catalogFields = "";
  public String categorizersForLanguages = "";
  public String categoryField = "";
  public boolean sendCookies = true;
  public int wordsForSimilarPages = 40;
  public int wordsDisplayedForSimilarPages = 40;
  public boolean metaRobots = false;
  public String downloadTo = "";

  public String spiderLog = "spider.log";
  public String parserLog = "parser.log";
  public String rmiLog = "rmi.log";
  public String policyFile = "conf/policyFile";

  public static boolean makeIndexSpace = false;
  public static boolean doneRegistration = false;

  public String patbinRegistrationKey = "Aster.Viola.Grass v1.4/-193865574";

  private static String aStaticLockObject = "LOCK";
  
  
  
  private PrintWriter logStream;
  private ParserLoader parserLoader;  
  
  public static Config config;

  public Config(){
    readConfig();
    config = this;
  }

  public Config(String configFile){
    this.configFile = configFile;
    readConfig();
    if (!useRemoteTypes.equals("false")){
      useRemoteTypes = "";
    }
    config = this;
  }

  public PrintWriter getLog(){
    if (logStream==null){ return new PrintWriter(System.err,true); }
    return logStream;
  }

  public void setlog(PrintWriter log){
    logStream = log;
  }

  public synchronized ParserLoader getParserLoader(){
    if (parserLoader == null){
      parserLoader = new ParserLoader(parser_list,parser_dir);
    }
    return parserLoader;
  }

  public void configureParser(Parser p,String filename){
    p.setDelimeter(fields_delimeter);
    p.setTitleField(title_field);
    p.setDescriptionField(descriptionfield);
    p.setFilename(filename);
  }


/*
  public Hashtable getCategorizersForLanguages() throws Exception {
    Hashtable ret = new Hashtable();
    for(StringTokenizer tok= new StringTokenizer(categorizersForLanguages,",");tok.hasMoreTokens();){
      StringTokenizer tok1 = new StringTokenizer(tok.nextToken(),":");
      if (tok1.countTokens()!=2){ continue;}
      String lang = tok1.nextToken();
      File catFile = new File(tok1.nextToken());
      if (!catFile.exists() || catFile.isDirectory()){ continue; }
      Categorizer cat = Categorizer.readCategorizer(catFile);
      if (cat==null){ continue; }
      if (lang.equals("")){ lang="*"; }
      ret.put(lang,cat);
    }
    return ret;
  }
*/
  




  public Hashtable readStopList(){
    return readHash(stoplist);
  }

  private Hashtable readHash(String listfile){
    Hashtable ret = new Hashtable();
    if (listfile.equals("")){ return ret;}
    try {
      FileReader fr = new FileReader(listfile);
      BufferedReader br = new BufferedReader(fr);
      String line = "";
      while( (line = br.readLine()) != null){
	      if (line.equals("")){ continue;}
        ret.put(line,line);
      }
    } catch (IOException e){
      System.err.println("Warning - caught a "+e+"\b  while trying to read the stoplist "+listfile);
      Logging.warning("Warning - caught a "+e+"\b  while trying to read the stoplist "+listfile);
    }
    return ret;
  }

  public Hashtable readLangIgnore(){
    Hashtable ret = new Hashtable();
    if (stoplist.equals("")){ return ret;}
    StringTokenizer tok = new StringTokenizer(langIgnore,",");
    while(tok.hasMoreTokens()){
      String s = tok.nextToken();
      s = s.toLowerCase();
      ret.put(s,s);
    }
    return ret;
  }



  public Hashtable readSynonyms(){
    Hashtable ret = new Hashtable();
    if (synonyms.equals("")){ return ret;}
    try {
      FileReader fr = new FileReader(synonyms);
      BufferedReader br = new BufferedReader(fr);
      String line = "";
      while( (line = br.readLine()) != null){
        StringTokenizer tok = new StringTokenizer(line," \t");
        if (!tok.hasMoreTokens()){continue;}
	      String master = tok.nextToken();
        master = master.toLowerCase();
	      while(tok.hasMoreTokens()){
	        String syn = tok.nextToken().toLowerCase();
          ret.put(syn,master);
	      }
      }
    } catch (IOException e){
      System.err.println("Warning - caught a "+e+"\b  while trying to read the synonyms "+synonyms);
      Logging.warning("Warning - caught a "+e+"\b  while trying to read the synonyms "+synonyms);
    }
    return ret;
  }


  public String getServerName(){
    return serverName;
  }

  public void readConfig(){
    FileInputStream fis;
    try {
      fis = new FileInputStream(configFile);
    } catch (FileNotFoundException e){
      Logging.severe("FileNotFound logging the config "+e);
      System.err.println(e);
      return;
    }
    initalizeConfig(this,fis);
  }

  public static Hashtable readParameters(InputStream is){
    Vector lines = new Vector();
    Hashtable hash;
    try {
      InputStreamReader reader = new InputStreamReader(is);
      BufferedReader ds = new BufferedReader(reader);
      String line="Start";
      while(line != null){
        line = ds.readLine();
        if (line == null){continue;}
        if (line.length()==0){ continue;}
        if (line.charAt(0)=='#'){ continue;}
        lines.addElement(line);
        System.err.println(line);
      }
      ds.close();
      reader.close();
      is.close();
    } catch (Exception e){System.err.println(e); Logging.severe("Error reading configuration "+e);}
    hash = new Hashtable(lines.size());
    int i = 0;
    for(Enumeration en=lines.elements();en.hasMoreElements();i++){
      try {
        StringTokenizer t = new StringTokenizer( (String) en.nextElement(),"=");
        if (t.countTokens() >= 2){
	  String name = t.nextToken();
	  String value = t.nextToken();
	  hash.put(name,value);
//	  System.err.println(i+" "+name+" "+value);
//      } else {
//          System.err.println(i);
        }
      } catch (Exception e){System.err.println(e+" parsing Input"); Logging.severe("Error reading configuration "+e);}
     }
     return hash;
   }

   public static void initalizeConfig(Config config,InputStream is) {

        Class metaclass = config.getClass();
        Field[] fields = metaclass.getFields();
        String param = null;
	Hashtable params = readParameters(is);
	if (params == null){
	  System.err.println("Parameters were not Read");
	  Logging.severe("Configuration parameters not found");
	  return;
        }
	Hashtable fieldsHash = new Hashtable();
        for (int i = 0; i < fields.length; i++) {
            fieldsHash.put( fields[i].getName(),new Integer(i));
            try {
		param = (String) params.get(fields[i].getName());
//		System.err.println(fields[i].getName() + " "+ param);
		if (param == null){
//                  System.err.println("Warming Config variable "+param+" not found");
                    continue;
                }
                if ( Modifier.isFinal(fields[i].getModifiers()) ) continue;

                Class fieldType = fields[i].getType();

                if (fieldType.equals(boolean.class)) {
                    fields[i].setBoolean(config, Boolean.valueOf(param).booleanValue());
                }

                else if (fieldType.equals(byte.class)) {
                    fields[i].setByte(config, Byte.valueOf(param).byteValue());
                }
                else if (fieldType.equals(char.class)) {
                    fields[i].setChar(config, param.charAt(0));
                }

                else if (fieldType.equals(double.class)) {
                    fields[i].setDouble(config, Double.valueOf(param).doubleValue());
                }

                else if (fieldType.equals(float.class)) {
                    fields[i].setFloat(config, Float.valueOf(param).floatValue());
                }

                else if (fieldType.equals(int.class)) {
                    fields[i].setInt(config, Integer.valueOf(param).intValue());
                }

                else if (fieldType.equals(long.class)) {
                    fields[i].setLong(config, Long.valueOf(param).longValue());
                }

                else if (fieldType.equals(short.class)) {
                    fields[i].setShort(config, Short.valueOf(param).shortValue());
                }

                else if (fieldType.equals(String.class)) {
                    fields[i].set(config, param);
                }
            }
            catch (Exception e) {
                System.err.println(e + " while initializing " + fields[i]);
		Logging.severe(e+ " while initialzing "+fields[i]);
            }
        }
	for(Enumeration e = params.keys(); e.hasMoreElements();){
          String s = (String) e.nextElement();
          if (fieldsHash.get(s) == null){
            System.err.println("warning Configuration parameter "+s+" is unknown");
	    Logging.warning("Warning Configuration parameter "+s+" is unknown");
          }
        }
    }



}




