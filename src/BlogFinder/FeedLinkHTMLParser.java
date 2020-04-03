package BlogFinder;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import Distiller.Result;


public class FeedLinkHTMLParser extends HTMLParser {

	Hashtable<String,String> linkText;
	Hashtable<String,String> altText;
	Hashtable<String,String> typeText;
	boolean linkMid;
	StringBuffer linkTextBuf;
	String curLink;
	String imgAltText;
	
	FeedLinkHTMLParser(){
		linkText = new Hashtable<String,String>();
		altText = new Hashtable<String,String>();
		typeText = new Hashtable<String,String>();
		linkMid = false;
		linkTextBuf = new StringBuffer();
		curLink="";
		imgAltText="";
	}
	
	public void reset(){
		super.reset();
		linkText = new Hashtable<String,String>();
		altText = new Hashtable<String,String>();
		typeText = new Hashtable<String,String>();
		linkMid = false;
		linkTextBuf = new StringBuffer();
		curLink="";
		imgAltText="";
	}
	
	protected void endTag() throws IOException {
	    String tag1 = tag.toString();
	    if (tag1.equalsIgnoreCase("link")){
	    	String type = (String) attributes.get("type");
	    	String title= (String) attributes.get("title");
	    	String link = (String) attributes.get("href");
	    	if (title==null) title="";
	    	if (type==null) type="";
	    	if (link!=null){
	    		linkText.put(link, title);
	    		typeText.put(link, type);
	    	}
	    }
		if (tag1.equalsIgnoreCase("a") && offTag==false){
          linkMid = true;
          linkTextBuf.setLength(0);
          imgAltText="";
          curLink = (String) attributes.get("href");
          if (curLink==null || curLink.equals("")){ curLink=""; linkMid = false; }
		}
		if (tag1.equalsIgnoreCase("img") && linkMid){
			String imgTemp = (String) attributes.get("alt");
			if (imgTemp!=null && !imgTemp.equals("")){ imgAltText=imgTemp; }
		}
		if (tag1.equalsIgnoreCase("a") && offTag == true){
          linkMid = false;
          linkText.put(curLink, linkTextBuf.toString());
          altText.put(curLink,imgAltText);
		}
		super.endTag();
	}
	
	protected void addWord(String word,int score){
		if (linkMid){
			if (linkTextBuf.length()!=0){ linkTextBuf.append(" "); }
			linkTextBuf.append(word);
		}
		super.addWord(word,score);
	}
	
	public Result getFeed(){
		float bestScore = 0.0f;
		Result result=null;
		for(Enumeration<String> e=linkText.keys();e.hasMoreElements();){
			float score = 0.0f;
			String  link=e.nextElement();
			String text = linkText.get(link);
			if (text==null) text="";
			text=text.toLowerCase();
			String alt = altText.get(link);
			if (alt==null) alt="";
			alt = alt.toLowerCase();
			String type= typeText.get(link);
			if (type==null) type="";
			type=type.toLowerCase();
//  Transform link to allow types
			if (link.startsWith("feed:html:", 0)){ link=link.substring(5); }
			if (link.contains("feeds.feedburner.com") && !link.endsWith("?format=xml")){
				if (link.contains("?")){ link=link.substring(0,link.indexOf("?")); }
				link = link.concat("?format=xml");
			}
			
			if (text.contains("atom") || alt.contains("atom")){ type="atom"; score+=0.5; }
			if (text.contains("rss") || alt.contains("rss")){ type="rss"; score+=0.6; }
			if (text.contains("post (atom)")){ type="atom"; score+=1.0; } // BlogSpot type
			if (link.contains("feeds.")){ // URL With Feeds in it
				score+=1.0;
			}
			if (link.contains("/feed/")){
				score+=0.9;
			}
			if (type.equals("application/atom+xml")){ score+=1.6; type="atom"; }
			if (type.equals("application/rss+xml")){ score+=1.5; type="rss";}
			if (type.equals("text/xml")){ score+=0.5; }
			if (alt.contains("rss")){ score+=0.6; type="rss";}
			if (alt.contains("atom")){ score+=0.6; type="rss";}
			if (alt.contains("feed")){ score+=0.6; }
			if (alt.contains("subscribe")){ score+=0.4; }
			if (text.contains("subscribe")){ score+=0.4; }
			if (text.contains("comment")){ score-=0.7; }
			if (alt.contains("comment")){ score-=0.7; }
			if (link.contains("comments")){ score-=0.1; }
			if (link.endsWith("atom.xml")){ score+=1.1; type="atom";}
			if (link.endsWith("rss.php")){ score+=0.9; type="rss";}
// Avoid add it sites
			if (link.contains("newsgator.com")){ score-=0.8; }
			if (link.contains("addthis.com")){ score-=0.8; }
			if (link.contains("geckotribe.com")){score-=0.8; }
			if (link.contains("newsalloy.com")){score-=0.8; }
			if (link.contains("bloglines.com")){score-=0.8; }
			if (link.contains("my.yahoo.com")){ score-=0.8; }
			if (link.contains("fusion.google.com")){ score-=-0.8; }
//		
			if (link.contains("feeds.feedburner.com")){ score+=1.5; }
			if (link.contains("wordpress.com/feed")){ score+= 1.5; }
		    if (link.contains("rss")){ score+=0.3; type="rss";}
		    if (link.contains("atom")){ score+=0.3; type="atom";}
		    if (link.contains("xml")){score+=0.2; }
		    if (link.contains("rdf")){ score+=0.2; }
            if (score>bestScore){
            	result = new Result(link,type,text,"");
            	bestScore = score;
            }
            
            if (link.length()>5 && link.substring(0,5).equalsIgnoreCase("feed:")){
            	link = link.substring(5,link.length());
            	if (link.length()>5 && !link.substring(0,5).equalsIgnoreCase("http:")){
            		link="http:"+link;
            	}
            }
            
            Logging.fine("link: "+link+"\ntype: "+type+"\ntext: "+text+"\nalt: "+alt+"\nscore "+score);
		}
		return result;
	}
	
}
