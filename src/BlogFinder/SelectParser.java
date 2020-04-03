package BlogFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PushbackReader;
import java.util.*;

// A parser that switches between a choice of other parsers when it
// recognises a particular character string

public class SelectParser extends Parser {

	Hashtable<String,Parser> parserForPats;
	Parser activeParser;
	
	public SelectParser(){
		parserForPats = new Hashtable<String,Parser>();
		useReader = false;
	}
	
	public void reset(){
		super.reset();
		activeParser = null;
	}
	
	public Parser getParser(){
		if (activeParser==null){ return this; }
		return activeParser;
	}
	
	
	
	public void configure(String args[], ParserLoader pl){
		if (args.length<2 || (args.length/2)*2!=args.length){
			throw new Error("Bad arguments for Select Parser");
		}
		for(int i=0;i<args.length;){
			String pat = args[i++];
			String mime = args[i++];
			Parser p = pl.getParser(mime);
			parserForPats.put(pat, p);
		}
	}
	
	
	void parseAll(InputStream is) throws IOException {
		OpenByteArrayInputStream obis;
		if (is instanceof OpenByteArrayInputStream){ 
			obis=(OpenByteArrayInputStream) is;
		} else {
			byte buf[]=new byte[1000];
			ByteArrayOutputStream bufmaker = new ByteArrayOutputStream();
			int read=0;
			while( (read=is.read(buf,0,1000))>0 ){
				bufmaker.write(buf,0,read);
			}
			is.close();
			bufmaker.close();
			buf = bufmaker.toByteArray();
			obis = new OpenByteArrayInputStream(buf,0,buf.length);
		}
		characterEncoding = getEncoding(obis);
		InputStreamReader reader = new InputStreamReader(obis,characterEncoding);
		activeParser = null;
		boolean hasMore = true;
		int lineCount=20;
		StringBuffer buf = new StringBuffer();
        while(activeParser==null && hasMore){
            buf.setLength(0);
            hasMore = readline(reader,buf);
            String s = buf.toString();
            for(Enumeration<String> e=parserForPats.keys();e.hasMoreElements();){
          	  String pat=e.nextElement();
          	  if (s.contains(pat)){
          		  activeParser=parserForPats.get(pat);
          	  }
            }
            if (lineCount--==0){ reader.close(); obis.close(); return; }
          }
        if (activeParser==null){ reader.close(); obis.close(); return; }
        activeParser.reset();
        obis.close();
        obis = new OpenByteArrayInputStream(obis.getBuffer());
        activeParser.parseAll(obis);
        obis.close();
	}
	
	@Override
	void parseAll(Reader reader) throws IOException {
		activeParser=null;
		PushbackReader pbr = new PushbackReader(reader,10240);
		Stack<String> lines = new Stack<String>();
        StringBuffer buf = new StringBuffer();
        boolean hasMore = true;
        int lineCount=20;
        while(activeParser==null && hasMore){
          buf.setLength(0);
          hasMore = readline(pbr,buf);
          String s = buf.toString();
          for(Enumeration<String> e=parserForPats.keys();e.hasMoreElements();){
        	  String pat=e.nextElement();
        	  if (s.contains(pat)){
        		  activeParser=parserForPats.get(pat);
        	  }
          }
          lines.push(s);
          if (lineCount--==0){ pbr.close(); return; }
        }
        if (activeParser==null){ pbr.close(); return; }
        while(!lines.isEmpty()){
        	String s=lines.pop();
//        	System.err.println(s);
        	pbr.unread(s.toCharArray());
        }
        activeParser.reset();
        activeParser.parseAll(pbr);
        pbr.close();
	}

    public String getEncoding(InputStream is){
    	try {
    	  if (is instanceof OpenByteArrayInputStream){
      		OpenByteArrayInputStream obis = (OpenByteArrayInputStream) is;
      		byte buf[] = obis.getBuffer();
      		LineNumberReader lr = new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(buf),"iso-8859-1"));
      		String topLine = lr.readLine();
      		if (topLine.contains("<?xml")){
      			int i= topLine.indexOf("<?xml");
      			int j= topLine.indexOf("?>",i);
      			String s = topLine.substring(i,j);
      			i = s.indexOf("encoding=");
      			if (i>0){
      				int i1 = s.indexOf("\"",i+9);
      				j = s.indexOf("\"",i1+1);
      				if (i1<0 || j<0){
      					i1 = s.indexOf("'",i+9);
      					j = s.indexOf("'",i1+1);
      					if (i1<0 || j<0){ 
      						Logging.info("Couldn't find charset in "+topLine);
      						return characterEncoding;
      					}
      				}      				
      				String newCharset = s.substring(i1+1,j);
      		    	Logging.finer("XMLFeedParser using charset "+newCharset);
      				return newCharset;
      			}
      		}
      	  }
    	} catch (Exception e){
    		Logging.warning("Couldn't find charset ",e);
    	}
    	return characterEncoding; // Default return orginal enc
      }
	
	private boolean readline(Reader r, StringBuffer b) throws IOException {
	  char c[]=new char[1];
	  int ch;
	  while((ch=r.read())!=-1){
		  c[0]=(char) ch;
		  b.append(c);
		  if (ch=='\n' || ch=='>') return true; // End at each element
	  }
	  return false;
	}

	
}
