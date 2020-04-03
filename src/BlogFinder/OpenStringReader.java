package BlogFinder;

import java.io.*;

public class OpenStringReader extends StringReader {

  String s;

  public OpenStringReader(String s){
    super(s);
    this.s = s;
  }

  public String getString(){
    return s;
  }

}