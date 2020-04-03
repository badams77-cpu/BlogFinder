package BlogFinder;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.text.*;

public class FeedParser extends HyperTextParser {

	StringBuffer nullDescription;
	static int DESCRIPTIONLENGTH=250;
	
	private static SimpleDateFormat formats[] = {
		new SimpleDateFormat("yyyy'-'MM'-'dd'T'hh':'mm':'ssZ"),		
	    new SimpleDateFormat("yyyy'-'MM'-'dd'T'hh':'mm':'ss'Z'"),
		new SimpleDateFormat("yyyy'-'MM'-'dd'T'hh':'mm':'ss.SSSz")		
	};
	
	private static SimpleDateFormat formats2[] = {
		new SimpleDateFormat("yyyy'-'MM'-'dd'T'hh':'mm':'ss"),		
		new SimpleDateFormat("yyyy'-'MM'-'dd'T'hh':'mm':'ss.SSS"),//Atom
		new SimpleDateFormat("EEE',' dd MMM yyyy hh':'mm':'ss z") // RSS 2.0
	};	
	
	LinkedList<String> elinks;
	LinkedList<String> etitles;
	LinkedList<Date> edates;
	LinkedList<String> esummary;
	LinkedList<String> eids;
	
    String author;
	String authorEmail;

	public FeedParser(){
		nullDescription = new StringBuffer();
	}
	
	@Override
    public void parseAll(Reader r) throws IOException {
		
	}
	
	public void reset(){
		super.reset();
		elinks = new LinkedList<String>();
		etitles = new LinkedList<String>();
		edates = new LinkedList<Date>();
		esummary = new LinkedList<String>();
		eids = new LinkedList<String>();
		nullDescription = new StringBuffer();
	}
	
	
	void addDate(String date){ // 2003-12-13T18:30:02Z
      for(int i=0;i<formats.length;i++){
		try {
			Date d = formats[i].parse(date);
			edates.add(d);
			return;
		} catch (Exception e){
			Logging.info("Couldn't add date with "+formats[i].toPattern()+"\n"+date);
		}
      }
      if (date.length()>5){
    	  String sz = date.substring(date.length()-6,date.length());
    	  String dd = date.substring(date.length()-6);
    	  long tz =0;
    	  if (sz.matches("[\\x2d\\x2b]\\d\\d:\\d\\d")){
    		  tz = (sz.charAt(1)-48)*10 +  (sz.charAt(2)-48);
    		  tz = 60*tz+(sz.charAt(4)-48)*10 + (sz.charAt(5)-48);
    		  tz = 60000*tz;
    		  if (sz.charAt(0)=='-'){ tz=-tz; }
    	  }	  
   	      Date d = null;
   	      for(int i=0;i<formats2.length;i++){
   	    	  try {
   	    		  d = formats2[i].parse(date);
   	    	  } catch (Exception e){}
   	         if (d!=null){ break; }
   	      }
   	      if (d!=null){
   	    	  long t = d.getTime();
   	    	  d = new Date(t-tz);
   	    	  edates.add(d);
   	    	  return;
   	      }
      }
	  Logging.info("Couldn't add date "+date);
	  edates.add(new Date(0));      
	}
	
	void addItem( String link, String title, String date, String summary, String id){
		elinks.add(link);
		etitles.add(title);
		addDate(date);
		esummary.add(summary);
		eids.add(id);
	}

	  public void addWord(String word,int score){
		    words.addElement(word);
		    wordScores.addElement(new Integer(score));
		    if (description.length() == 0 && nullDescription.length()<DESCRIPTIONLENGTH){
		      nullDescription.append(" ");
		      nullDescription.append(word);
		    }
		  }

	  public void addString(String s,int mainscore){
		//   System.err.println(xhi+" "+s);
//		    String s1 = stripTags(s);
		    BreakIterator bi = BreakIterator.getWordInstance();
		    bi.setText(s);
		    int start = bi.first();
		    for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
		      boolean isWord = false;
		      for(int x=start; x<end;x++){
		        char ch = s.charAt(x);
		        if (Character.isLetter(ch) || Character.isDigit(ch)){ isWord = true; }
		      }
		      if (!isWord){ continue; }
		      String word = s.substring(start,end);
		      if (mainscore != 0){
			      addWord(word,mainscore);
		      }	  
		    }
	   }
	  
	    public String stripTags(String s){
	    	boolean output = true;
	    	StringBuffer buffy = new StringBuffer(s.length());
	    	for(int i=0;i<s.length();i++){
	    		Character c = s.charAt(i);
	    		switch(c){
	    			case '<':
	    				output=false; break;
	    			case '>':
	    				buffy.append(' ');
	    				output=true; break;
	    			default:
	    				if (output) buffy.append(c);
	    		};
	    	}
	    	return buffy.toString();
	    }
	  
}
