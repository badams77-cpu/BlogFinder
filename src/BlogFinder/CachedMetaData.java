
package BlogFinder;

import java.io.*;
import java.net.URL;

public class CachedMetaData implements Serializable {

 static final long serialVersionUID = 0xDEADBABE02D20501L;
   
  public String contentType;
  public  int fileLength;
  public long lastModified;
  public URL url;
  public String lang;
  public String encoding;
  public byte[] bodyDigest;

  public CachedMetaData( FlyBall fly){
    fileLength = fly.bufferLength;
    contentType = fly.type;
    lastModified = fly.lastModified;
    lang = fly.lang;
    encoding = fly.encoding;
    url = fly.url;
    bodyDigest = fly.bodyDigest;
  }  

}