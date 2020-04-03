

package BlogFinder;

import java.util.*;
import java.net.*;

public abstract class HyperTextParser extends Parser {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";

  Vector links;
  String HREFbase="";
  String targetBase="";
  NullParser linksParser;
  boolean follow = true;

  public HyperTextParser() {
    links = new Vector();
    hypertextparser=true;
    linksParser = new NullParser();
    fieldNames.addElement("link");
  }

  public void reset(){
    links = new Vector();
    linksParser = new NullParser();
    HREFbase = "";
    targetBase = "";
    follow = true;
    super.reset();
  }

  public Enumeration getLinks(){
    if (!follow){ return (new Vector() ).elements(); }
    return links.elements();
  }

  public String getBase(){
    return HREFbase;
  }

  public String getTargetBase(){
    return targetBase;
  }

  public void addLinkField(URL linkurl){
    linksParser.addWord(linkurl.toExternalForm(),1);
  }

  public Parser getFieldParser(String field){
    if (field.equals("link")){ return linksParser;}
    return super.getFieldParser(field);
  }


}