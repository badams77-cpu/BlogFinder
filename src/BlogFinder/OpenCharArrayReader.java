package BlogFinder;

import java.io.*;

public class OpenCharArrayReader extends CharArrayReader {

  char[] buffer;
  int offset = 0;
  int length = 0;

  public OpenCharArrayReader(char[] buf) {
    super(buf);
    buffer = buf;
    offset = 0;
    length=buf.length;
  }

  public OpenCharArrayReader(char[] buf,int offset,int length){
    super(buf,offset,length);
    buffer = buf;
    this.offset = offset;
    this.length = length;
  }

  public char[] getBuffer(){
    return buffer;
  }

  public int getOffset(){
    return offset;
  }

  public int getLength(){
    return length;
  }

}