package BlogFinder;

import java.io.*;

public class OpenByteArrayInputStream extends ByteArrayInputStream {

  byte[] buffer;
  int offset = 0;
  int length = 0;

  public OpenByteArrayInputStream(byte[] buf) {
    super(buf);
    buffer = buf;
    offset = 0;
    length=buf.length;
  }

  public OpenByteArrayInputStream(byte[] buf,int offset,int length){
    super(buf,offset,length);
    buffer = buf;
    this.offset = offset;
    this.length = length;
  }

  public byte[] getBuffer(){
    return buffer;
  }

  public int getOffset(){
    return offset;
  }

  public int getLength(){
    return length;
  }

}