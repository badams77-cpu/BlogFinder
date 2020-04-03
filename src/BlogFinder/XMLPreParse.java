package BlogFinder;

import java.io.*;

public class XMLPreParse {

// 	
	
	public XMLPreParse(){
		
	}
	
	public static OpenByteArrayInputStream preparse(InputStream is) throws IOException {
		String enc = getEncoding(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    InputStreamReader isr = new InputStreamReader(is,enc);
	    OutputStreamWriter osw = new OutputStreamWriter(baos,enc);
	    char buf[] = new char[500];
	    char out[] = new char[2500]; // (5* just in case all are & )
	    int off = 0;
	    int read = 0;
	    while( (read=isr.read(buf,off,500))>0){
	      int j=0;
	      for(int i=0;i<read;i++){
	    	char c = out[j++] = buf[i];
	    	if (c=='&'){
	    		boolean entTerm = false;
	    		boolean alphaMode = true;
	    		for(int k=i+1;k<i+10 && k<read;k++){
	    			char cx = buf[k];
	    			if (cx==';'){ entTerm = true; break; }
	    			if (k==i+1 && cx=='#'){ alphaMode = false; continue; }
	    			if (alphaMode){
	    				if ( (cx<'A') || cx>'z' || (cx>'Z' && cx<'a') ){ break; }
	    			} else {
	    				if ( cx<'0' || cx>'9'){ break; }
	    			}
	    		}
	    		if (!entTerm){
	    			out[j++]='a'; out[j++]='m'; out[j++]='p'; out[j++]=';';
	    		}
	    	}
	      }
	      osw.write(out,0,j);
	    }
	    isr.close();
	    osw.close();
	    baos.close();
	    OpenByteArrayInputStream obais = new OpenByteArrayInputStream(baos.toByteArray());
	    return obais;
	}

    public static String getEncoding(InputStream is){
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
      						return "ISO-8859-1";
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
    	return "ISO-8859-1"; // Default return orginal enc
      }
	
}
