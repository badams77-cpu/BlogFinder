

package BlogFinder;

import java.util.*;
import java.io.*;

public class ParserLoader extends ClassLoader {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";


  String parser_dir;
  static Hashtable cache;
  Hashtable parserCache;
  Hashtable classesForType;
  Hashtable initDataForType;

  public ParserLoader(){
  }

  public ParserLoader(String parser_list,String parser_dir) {
    this.parser_dir = parser_dir;
    cache = new Hashtable();
    parserCache = new Hashtable();
    classesForType = new Hashtable();
    initDataForType = new Hashtable();
    try {
      FileReader fr = new FileReader(parser_list);
      LineNumberReader lnr = new LineNumberReader(fr);
      String line;
      while( (line = lnr.readLine()) != null){
        StringTokenizer tok = new StringTokenizer(line);
	String parser = "";
	String mimetypes = "";
        String argv[];
        Hashtable extensions = new Hashtable();
        try {
          parser = tok.nextToken();
          mimetypes = tok.nextToken();
	  int count = tok.countTokens();
	  argv = new String[count];
	  for(int i=0;i<count;i++){
	    argv[i] = tok.nextToken();
	  }

	  tok = new StringTokenizer(mimetypes,",");
	  while(tok.hasMoreTokens()){
            String token = tok.nextToken();
	          if (token.length() != 0){
  	            extensions.put(token,argv);
	          }
          }
        } catch( NoSuchElementException e){continue;}
        if (parser.charAt(0) == '#'){ continue;}
        for(Enumeration e= extensions.keys();e.hasMoreElements();){
	  Object key = e.nextElement();
          classesForType.put(key,parser);
	  String args[] = (String[]) extensions.get(key);
	  initDataForType.put(key,args);
        }
      }
      lnr.close();
      fr.close();
    } catch (Exception e){
      System.err.println("Fatal Error "+e+" trying to read parser list "+parser_list);
      Logging.severe("Fatal Error "+e+" trying to read parser list "+parser_list);
      e.printStackTrace(System.err);
      throw new Error("Fatal Error "+e+" trying to read parser list "+parser_list);
    }
  }

  public Parser getParser(String mimetype){
    String classname = (String) classesForType.get(mimetype);
    String args[] = (String[]) initDataForType.get(mimetype);
    if (classname==null){return null;}
    Parser parser = (Parser) parserCache.get(classname);
    if (parser!= null){parser.configure(args,this); return parser;}
    Class c = loadClass(classname,true);
    try {
      parser = (Parser) c.newInstance();
    } catch (IllegalAccessException e){
      System.err.println("Fatal Error "+e+" Accessing "+classname);
      throw new Error("Fatal Error "+e+" Accessing "+classname);
    } catch (InstantiationException e){
      System.err.println("Fatal Error "+e+" Instantizing "+classname);
      throw new Error("Fatal Error "+e+" Accessing "+classname);
    } catch (ClassCastException e){
      System.err.println("Fatal Error "+e+"\n: "+classname+" was not a Parser");
      throw new Error("Fatal Error "+e+"\n: "+classname+" was not a Parser");
    }
    parserCache.put(classname,parser);
    parser.configure(args,this);
    return parser;
  }

  public String[] getArgs(String mimetype){
    return (String[]) initDataForType.get(mimetype);
  }

  public Parser getParserUnique(String mimetype){
    String classname = (String) classesForType.get(mimetype);
    String args[] = (String[]) initDataForType.get(mimetype);
    if (classname==null){return null;}
    Parser parser =null;
    Class c = loadClass(classname,true);
    try {
      parser = (Parser) c.newInstance();
    } catch (IllegalAccessException e){
      System.err.println("Fatal Error "+e+" Accessing "+classname);
      throw new Error("Fatal Error "+e+" Accessing "+classname);
//      System.exit(255);
    } catch (InstantiationException e){
      System.err.println("Fatal Error "+e+" Instantizing "+classname);
      throw new Error("Fatal Error "+e+" Accessing "+classname);
//      System.exit(255);
    } catch (ClassCastException e){
      System.err.println("Fatal Error "+e+"\n: "+classname+" was not a Parser");
      throw new Error("Fatal Error "+e+"\n: "+classname+" was not a Parser");
//      System.exit(255);
    }
    parser.configure(args,this);
    return parser;
  }


  private byte[] loadClassData(String name){
    File classfile;
    String name1 = translatePackage(name)+".class";
    if (parser_dir != null && !parser_dir.equals("")){
      classfile = new File(parser_dir,name1);
    } else {
      classfile = new File(name1);
    }
    if (!classfile.exists()){
      System.err.println("Fatal Error - Cannot find Parser, "+name+" at\n"+classfile);
      throw new Error("Fatal Error - Cannot find Parser, "+name+" at\n"+classfile);
//      System.exit(255);
    }
    int length = (int) classfile.length();
    byte buffer[] = new byte[length];
    try {
      FileInputStream fis = new FileInputStream(classfile);
      fis.read(buffer,0,length);
      fis.close();
    } catch (IOException e){
      System.err.println("Fatal Error - Cannot Load Parser, "+name);
      throw new Error("Fatal Error - Cannot Load Parser, "+name);
//      System.exit(255);
    }
    return buffer;
  }

  private  String translatePackage(String name){
    StringBuffer retBuffer= new StringBuffer();
    for(int i=0;i<name.length();i++){
      char c = name.charAt(i);
      if (c == '.'){
        retBuffer.append(File.separatorChar);
      } else {
        retBuffer.append(c);
      }
    }
    return retBuffer.toString();
  }

  public synchronized Class loadClass(String name, boolean resolve) {
    Class retclass;
    try {
      retclass = findLoadedClass(name);
      if (retclass != null){return retclass;}
    } catch (Exception e){}
    try {
      retclass = findSystemClass(name);
      if (retclass != null){return retclass;}
    } catch (Exception e){}
    Class c = (Class) cache.get(name);
    if (c == null){
      byte data[] = loadClassData(name);
      c = defineClass("", data,0,data.length);
      cache.put(name,c);
    }
    if (resolve) resolveClass(c);
    return c;
  }

  public Enumeration allOKTypes(){
    return classesForType.keys();
  }

}