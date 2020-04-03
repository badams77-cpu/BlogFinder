package BlogFinder;

import java.util.*;
import java.io.*;

public class DivHTMLParser extends HTMLParser {

	Hashtable<String,Vector<String>> classLinks = new Hashtable<String,Vector<String>>();
	
	String classType = "";
	String linkTemp = "";
	
	
	protected void endTag() throws IOException {
	    String tag1 = tag.toString();
		if (tag1.equalsIgnoreCase("a") && offTag==false){
			classType = (String) attributes.get("class");
			linkTemp = (String) attributes.get("linkTemp");
		}
		if (tag1.equalsIgnoreCase("a") && offTag == true){
			if (classType==null){ classType="NULL"; }
			if (linkTemp!=null && !linkTemp.equals("")){
			  Vector<String> linkVec= classLinks.get(classType);
			  if (linkVec==null){
				  linkVec = new Vector<String>();
				  classLinks.put(classType,linkVec);
			  }
			  linkVec.add(linkTemp);
			}
		}
		super.endTag();
	}
	
	public Vector<String> linksForClass(String classType){
		return classLinks.get(classType);
	}
	
}
