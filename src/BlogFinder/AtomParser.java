package BlogFinder;

import java.io.File;
import java.io.FileInputStream;

public class AtomParser extends XMLFeedParser {

	private String eTitle="";
	private String eLink="";
	private String eDate="";
	private String eSummary="";
	private String eId = "";

	public AtomParser(){
		
	}
	
	public void reset(){
	  super.reset();
	  eTitle = eLink = eDate = eSummary = eId= "";
	}
		
	  public void callBack( String whatisit, String itscontent){
//		  System.err.println(whatisit);
		  if (whatisit.equalsIgnoreCase("!feed!title")){
			  title = itscontent; return;
		  }
		  if (whatisit.equalsIgnoreCase("!feed!subtitle")){
			  description.append(itscontent); return; 
		  }
		  if (whatisit.equalsIgnoreCase("!feed!author!name")){
			  author = itscontent; return;
		  }
		  if (whatisit.equalsIgnoreCase("!feed!author!email")){
			  authorEmail = itscontent; return; 
		  }
		  if (whatisit.length()>12 && whatisit.substring(0,11).equalsIgnoreCase("!feed!entry")){
			  entry(whatisit.substring(12), itscontent); 
		  } else if (whatisit.length()>7 && whatisit.substring(0,7).equalsIgnoreCase("!entry!")){
			  entry(whatisit.substring(7), itscontent);
		  }
	  }
	  
	  public void entry(String tag, String content){
//		  System.err.println("Parser tag "+tag);
		  if (tag.equalsIgnoreCase("title")){ 
			  eTitle=content;
			  addString(content,2);
			  return; 
		  }
//		  if (tag.equalsIgnoreCase("link")){ eLink = content; return; }
		  if (tag.equalsIgnoreCase("published")){ eDate = content; return; }
		  if (tag.equalsIgnoreCase("updated")&& eDate.equals("")){ eDate = content; return; }
		  if (tag.equalsIgnoreCase("content")){ eSummary = content;
		     addString(content,1);
		     return; 
		  }
		  if (tag.equalsIgnoreCase("summary")){ eSummary = content;
		     addString(content,1);
		     return;
		  }
		  if (tag.equalsIgnoreCase("id")){ eId = content; return; }
	  }
	  
	  public void endElement(String tag){
		  if (tag.equalsIgnoreCase("entry")){ 
			  addItem(eLink, eTitle, eDate, eSummary, eId);
			  eLink=eTitle=eDate=eSummary=eId="";
		  }
	  }

	  public void attCallBackPass1( String whatisit, String itscontent, XMLAttributeContext xac){
//		  System.err.println(whatisit);
		  if (whatisit.equalsIgnoreCase("link!href")){
			  eLink = itscontent;
		  }
	  }

	  public void attCallBackPass2( String whatisit, String itscontent, XMLAttributeContext xac){}
	
	  public static void main(String[] argv){
		  File f = new File(argv[0]);
		  try {
		    FileInputStream fis = new FileInputStream(f);
		    int len = (int) f.length();
		    byte b[] = new byte[len];
		    int off=0;
		    while(off<len){
		    	int got = fis.read(b,off,len);
		    	if (got==-1){ break; }
		    	off+=got;
		    }
		    OpenByteArrayInputStream obis = new OpenByteArrayInputStream(b,0,len);
		    XMLFeedParser xfp = new AtomParser();
		    xfp.parseAll(obis);
		    System.out.println("title: "+xfp.getTitle());
		  } catch (Exception e){
			  e.printStackTrace();
		  }
	  }
	  
}
