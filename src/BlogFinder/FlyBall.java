package BlogFinder;

import java.net.URL;

public class FlyBall {

// The data caught by the Spider :-)

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";


  public byte[] inputbuffer;
  public  int bufferLength;
  public String type;
  public long lastModified;
  public URL url;
  public SpiderThread spider;
  public String lang;
  public String langAllow[];
  public String encoding;
  public byte[] bodyDigest;
  public URL currLink;
  public int taskId;
  public boolean hasData = false;
  public String error;
  
  public FlyBall(byte[] buffer, int length, String type, long lastmod,URL url, SpiderThread spider,String lang,String
encoding, String[] langAllow, int taskId){
    inputbuffer = buffer;
    bufferLength = length;
    this.type = type;
    lastModified = lastmod;
    this.url = url;
    this.spider = spider;
    this.lang = lang;
    this.encoding = encoding;
    this.langAllow = langAllow;
    bodyDigest = null;
    this.taskId = taskId;
    hasData = true;
    error="";
  }

  public FlyBall(byte[] buffer, int length, String type, long lastmod,URL url, SpiderThread spider,String lang,
	                    byte[] bodyDigest, URL currLink,String encoding, String[] langAllow, int taskId){
    inputbuffer = buffer;
    bufferLength = length;
    this.type = type;
    lastModified = lastmod;
    this.url = url;
    this.spider = spider;
    this.lang = lang;
    this.currLink = currLink;
    this.encoding = encoding;
    this.langAllow = langAllow;
    this.bodyDigest = bodyDigest;
    this.taskId = taskId;
    hasData = true;
    error="";
  }

  public FlyBall(CachedMetaData cmd,URL currLink, byte[] body, int taskId){
    inputbuffer = body;
    bufferLength = cmd.fileLength;
    type = cmd.contentType;
    lastModified = cmd.lastModified;
    url = cmd.url;
    spider = null;
    lang = cmd.lang;
    this.currLink = currLink;
    this.encoding = cmd.encoding;
    this.langAllow = null;
    this.bodyDigest = cmd.bodyDigest;
    this.taskId = taskId;
    hasData = true;
    error="";
  }

  public FlyBall(URL url, String error, int id){
	this.currLink = url;
	this.url = url;
    this.error=error;
    this.taskId = id;
    hasData=false;
  }		  
}