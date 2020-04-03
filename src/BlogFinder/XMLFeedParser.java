package BlogFinder;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.io.File;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

public abstract class XMLFeedParser extends FeedParser implements XMLCallBack {

	  public static final String CopyRight =
		    "Exclusive Copyright of Barry David Ottley Adams 2008 all Rights Reserved";

      private int  DESCRIPTIONLENGTH = 250; // Length of Description

      public Hashtable fieldParsers;	
	
	  StringBuffer description;
	  StringBuffer nullDescription;



	  public XMLFeedParser() {	  
	    fieldParsers = new Hashtable();
	    description = new StringBuffer();
	    nullDescription = new StringBuffer();
	    title = "";
	    useReader=false; // Prefer InputStream, so we can change the character set
	  }

	  public void reset(){
	    fieldParsers = new Hashtable();
	    description = new StringBuffer();
	    nullDescription = new StringBuffer();
	    title = "";
	    super.reset();
	  }

	  public String getDescription(){  
	    if (description == null){return "";}
	    if (description.length()>DESCRIPTIONLENGTH){
	      return description.toString().substring(0,DESCRIPTIONLENGTH);
	    }
	    if (description.length()==0){
	      return nullDescription.toString();
	    }
	    return description.toString();
	  }

	  public String getTitle(){
	    if (title == null){return "";}
	    if (title.length() >100){ return title.substring(0,100);}
	    return title;
	  }

	  public Enumeration listFields(){
	    return fieldParsers.keys();
	  }
	  
	  public void parseAll(Reader reader) throws IOException {
		//   for(Enumeration e= fieldParsers.elements();e.hasMoreElements();){
//		      ((Parser) e.nextElement()).reset();
//		    }
		    reset();
	    XMLSaxHandler xhs = new XMLSaxHandler(this);
	    xhs.readXML(reader);
	 }

      public void parseAll(InputStream is) throws IOException {
		//   for(Enumeration e= fieldParsers.elements();e.hasMoreElements();){
//		      ((Parser) e.nextElement()).reset();
//		    }
    	  reset();
    	  characterEncoding = getEncoding(is);
		  XMLSaxHandler xhs = new XMLSaxHandler(this);
		  InputStream is1 = is;
		  try {
			  is1 = XMLPreParse.preparse(is);
		  } catch (Exception e){
			  Logging.warning("Exception preparsing the file ",e);
		  }
		  xhs.readXML(is1);
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
      
      public abstract void callBack( String whatisit, String itscontent);

      public abstract void attCallBackPass1( String whatisit, String itscontent, XMLAttributeContext xac);

      public abstract void attCallBackPass2( String whatisit, String itscontent, XMLAttributeContext xac);

	  public abstract void endElement(String element);
	  

}
