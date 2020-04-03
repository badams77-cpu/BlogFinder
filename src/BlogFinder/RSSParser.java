package BlogFinder;

//Parser for both RSS version 1.0
import java.io.*;

public class RSSParser extends XMLFeedParser {

	private String eTitle="";
	private String eLink="";
	private String eDate="";
	private String eSummary="";
	private String eId = "";
    private boolean comLine=false;
	
	public RSSParser(){
	}
	
	public void reset(){
	  super.reset();
	  eTitle = eLink = eDate = eSummary = "";
	}
		
	  public void callBack( String whatisit, String itscontent){
//		  System.err.println(whatisit);
		  // For version 1.0
		  if (whatisit.equalsIgnoreCase("!rdf:rdf!channel!title")){
			  title = itscontent; return;
		  }
		  if (whatisit.equalsIgnoreCase("!rdf:rdf!channel!description")){
			  description.append(itscontent); return; 
		  }
		  if (whatisit.equalsIgnoreCase("!rdf:rdf!channel!author!name")){
			  author = itscontent; return;
		  }
		  if (whatisit.equalsIgnoreCase("!rdf:rdf!channel!author!email")){
			  authorEmail = itscontent; return; 
		  }
		  if (whatisit.length()>14 && whatisit.substring(0,13).equalsIgnoreCase("!rdf:rdf!item!")){
			  entry(whatisit.substring(14), itscontent); return; 
		  } else if (whatisit.length()>10 && whatisit.substring(0,10).equalsIgnoreCase("!rdf!item!")){
			  entry(whatisit.substring(10), itscontent);  return;
		  }
//  For version 2.0
		  if (whatisit.equalsIgnoreCase("!rss!channel!title")){
			 title = itscontent; return; 
		  }
		  if (whatisit.equalsIgnoreCase("!rss!channel!description")){
             description.append(itscontent); return;
		  }
		  if (whatisit.equalsIgnoreCase("!rss!channel!managingEditor")){
			author = itscontent;  
		  }
		  if (whatisit.equalsIgnoreCase("!rss!channel!webmaster")){
			  if (author==null || author.equals("")){ author = itscontent; }
		  }
		  if (whatisit.length()>10 && whatisit.substring(0,10).equalsIgnoreCase("!rss!item!")){ 
	          v2entry(whatisit.substring(10),itscontent); 
	      }
		  if (whatisit.length()>18 && whatisit.substring(0,18).equalsIgnoreCase("!rss!channel!item!")){
	          v2entry(whatisit.substring(18),itscontent);
		  }	  
	  }
		  
	 // Version 1.0 entry 
	   public void entry(String tag, String content){
//		  System.err.println("Parser tag "+tag);
		  if (tag.equalsIgnoreCase("title")){ 
			  eTitle=content;
			  addString(content,2);
			  return; 
		  }
		  if (tag.equalsIgnoreCase("link")){ eLink = content; return; }
		  if (tag.equalsIgnoreCase("published")){ eDate = content; return; }
		  if (tag.equalsIgnoreCase("updated")&& eDate.equals("")){ eDate = content; return; }
		  if (tag.equalsIgnoreCase("description")){ eSummary = content;
		     addString(content,1);
		     return; 
		  }
		  if (tag.equalsIgnoreCase("summary")){ eSummary = content;
		     addString(content,1);
		     return;
		  }
		  if (tag.equalsIgnoreCase("id")){ eId = content; return; }
	  }
	  
	  public void v2entry(String tag, String content){
		  if (tag.equalsIgnoreCase("title")){
			  eTitle=content;
			  addString(content,2);
			  return;
		  }
		  if (tag.equalsIgnoreCase("link")){
			  eLink=content; return;
		  }
		  if (tag.equalsIgnoreCase("description")){
			  eSummary = content;
			  addString(content,1);
			  return;
		  }
		  if (tag.equalsIgnoreCase("guid")){
			  eId = content; return;
		  }
		  if (tag.equalsIgnoreCase("pubdate")){
			  eDate = content; return;
		  }
	  }
	   
	  public void endElement(String tag){
		  if (tag.equalsIgnoreCase("item")){ 
			  addItem(eLink, eTitle, eDate, eSummary, eId);
			  if (comLine){
				  System.err.println("Found Item:\n"+eTitle+"\n"+eLink+"\n"+eDate+"\n"+eId+"\n");
				  System.err.println(eSummary+"\n\n");
			  }
		  }
	  }

	  public void attCallBackPass1( String whatisit, String itscontent, XMLAttributeContext xac){
//		  System.err.println(whatisit);
//		  if (whatisit.equalsIgnoreCase("link!href")){
//			  eLink = itscontent;
//		  }
	  }

	  public void attCallBackPass2( String whatisit, String itscontent, XMLAttributeContext xac){}
	
	  public static void main(String argv[]){
		  if (argv.length<1){
			  System.err.println("Usage: RSSParser filename");
			  return;
		  }
		  try {
			  File infile = new File(argv[0]);
			  if (!infile.exists()){ System.err.println("no file "+argv[0]); return; }
			    InputStream is = new FileInputStream(infile);
				byte buf[]=new byte[1000];
				ByteArrayOutputStream bufmaker = new ByteArrayOutputStream();
				int read=0;
				while( (read=is.read(buf,0,1000))>0 ){
					bufmaker.write(buf,0,read);
				}
				is.close();
				bufmaker.close();
				buf = bufmaker.toByteArray();
				OpenByteArrayInputStream obis = new OpenByteArrayInputStream(buf,0,buf.length);
			    RSSParser rsparser = new RSSParser();
			    rsparser.comLine=true;
			    rsparser.parseAll(XMLPreParse.preparse(obis));
		  } catch (Exception e){
			  e.printStackTrace(System.err);
		  }
	  }
	  
}
