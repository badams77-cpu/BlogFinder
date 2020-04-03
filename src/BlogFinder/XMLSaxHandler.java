package BlogFinder;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

public class XMLSaxHandler extends org.xml.sax.HandlerBase {

  public static String SAXParserName = "javax.xml.parsers.SAXParser";

  XMLCallBack whoToCall;
  public StringBuffer content;
  private Vector activeElements;

  public XMLSaxHandler(XMLCallBack xcb) {
    super();
    whoToCall = xcb;
    content = new StringBuffer();
    activeElements = new Vector();
  }

  private void reset(){
    content = new StringBuffer();
    activeElements = new Vector();
  }

  public void readXML(Reader reader) throws IOException {
    reset();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      SAXParser xp = spf.newSAXParser();
      xp.parse(new InputSource(reader),this);
    } catch (org.xml.sax.SAXException e){
      e.printStackTrace();
      throw new IOException( "SAX Exception "+e.getMessage());
    } catch (javax.xml.parsers.ParserConfigurationException e){
      e.printStackTrace();
      throw new IOException( "SAX Exception "+e.getMessage());
    }
  }

  public void readXML(InputStream byteStream) throws IOException {
    reset();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      SAXParser xp = spf.newSAXParser();
      InputSource is = new InputSource(byteStream);
      if (whoToCall instanceof Parser){
        Parser p = (Parser) whoToCall;
        is.setEncoding(p.characterEncoding);
      }
      xp.parse(is,this);
    } catch (org.xml.sax.SAXException e){
      e.printStackTrace();
      throw new IOException("SAX Exception "+e);
    } catch (javax.xml.parsers.ParserConfigurationException e){
      e.printStackTrace();
      throw new IOException( "SAX Exception "+e.getMessage());
    }
  }


  public void characters(char c[], int start,int len){
    content.append(c,start,len);
  }

//  public void ignorableWhitespace(){
//    content.append(" ");
//  }

  public void startDocument(){}

  public void endDocument(){
    send();
  }


  public void startElement(String localname, AttributeList att){
    if (content.length()!=0){send();}
    String name = localname;
    if (name.equals("")){ name = "!Empty";}
    activeElements.add(name);
    try {
    XMLAttributeContext xac = new XMLAttributeContext();
    for(int i=0;i<att.getLength();i++){
      String aname = att.getName(i);
      String value = att.getValue(i);
      if (!value.equals("")){
        whoToCall.attCallBackPass1(name+"!"+aname,value,xac);
      }
    }
    for(int i=0;i<att.getLength();i++){
      String aname = att.getName(i);
      String value = att.getValue(i);
      if (!value.equals("")){
        whoToCall.attCallBackPass2(name+"!"+aname,value,xac);
      }
    }
    } catch (Exception e){
      e.printStackTrace();
    }
    
  }

  public void endElement(String localname){
    if (content.length()!=0){send();}
    String name = localname;
    if (name.equals("")){
      name = "!Empty";
    }
    activeElements.remove(name);
    whoToCall.endElement(localname);
  }

  
  private void send(){
    StringBuffer whatisit = new StringBuffer();
    for(Enumeration e=activeElements.elements();e.hasMoreElements();){
      String name = (String) e.nextElement();
      whatisit.append("!");
      whatisit.append(name);
    }
//    System.err.println("Sending "+whatisit.toString()+" "+content.toString());
    whoToCall.callBack(whatisit.toString(),content.toString());
    content.setLength(0);
  }

}

