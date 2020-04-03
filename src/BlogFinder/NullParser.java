

package BlogFinder;

import java.io.*;

public class NullParser extends Parser {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";

  public NullParser() {
  }

  public void open(){}

  public void addWord(String word,int score){
    words.addElement(word);
    wordScores.addElement(new Integer(score));
    if (linedata.length()!=0){
      linedata.append(" ");
    }
    linedata.append(word);
  }

  public void parseAll(Reader r) throws IOException {

  }

} 