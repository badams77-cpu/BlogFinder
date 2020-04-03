

package BlogFinder;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public abstract class Parser implements WordsSource {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";


  Reader reader;
  InputStream inputStream;
  String title;
  String description;
  String filename;
  Vector wordScores;
  Vector words;
  String delimeter;
  String title_field;
  String description_field;
  public boolean hypertextparser = false;
  StringBuffer linedata; // Used for Catalogs only
  long lastModified; // Last Modification data of the file/data parsed
  String languageCode;
  NullParser languageFieldParser;
  NullParser lastModFieldParser;
  String characterEncoding ="ISO-8859-1";
  public boolean useReader= true;
  public String langAllow[];
  public boolean isFrameSet = false;
  public FlyBall fly = null;
  Vector fieldNames = null;

  public Parser() {
    title = "";
    description = "";
    wordScores = new Vector();
    words = new Vector();
    lastModified = 0L;
    languageCode = "";
    languageFieldParser = null;
    lastModFieldParser = null;
    isFrameSet = false;
    filename = "";
    linedata = new StringBuffer(100);
    fieldNames = new Vector();
    fieldNames.addElement("lang");
    fieldNames.addElement("lastmod");
    fieldNames.addElement("title");
  }

  public void setEncoding(String enc){
    characterEncoding = enc;
  }

  public void setFrameInfo(FlyBall fly){
    this.fly = fly;
  }

  public boolean isFrameSet(){
    return isFrameSet;
  }

  public void reset (){
    title = "";
    description = "";
    wordScores = new Vector();
    words = new Vector();
    lastModified = 0L;
    languageCode = "";
    languageFieldParser = null;
    lastModFieldParser = null;
    isFrameSet = false;
    filename = "";
    linedata = new StringBuffer(100);
  }

  public void setLastModified(long lastmod){
    lastModFieldParser = new NullParser();
    lastModFieldParser.addWord( Long.toString(lastmod),1);
    lastModified = lastmod;
  }

  public String getLineData(){
    return linedata.toString();
  }

  public long getLastModified(){
    return lastModified;
  }

  public String getLanguageCode(){
    return languageCode;
  }

  public void setLanguageCode(String language_Code){
    if (language_Code.equals("")){ return;}
    languageCode = language_Code;
    languageFieldParser = new NullParser();
    languageFieldParser.addWord(language_Code,1);
  }

  public boolean shouldIndex(){
    return true;
  }

  public void setDelimeter(String deli){
    delimeter = deli;
  }

  public void setDescriptionField(String des_field){
    description_field = des_field;
  }

  public void setTitleField(String titleField){
    title_field = titleField;
  }

  public void setFilename(String filename){
    this.filename = filename;
  }

  public String getTitle(){
    return title;
  }

  public String getDescription(){
    return description;
  }

  public Enumeration listWords(){
    return words.elements();
  }

  public int numberOfWords(){
    return words.size();
  }

  public Enumeration listFields(){
    return fieldNames.elements();
  }

  public Parser getFieldParser(String field){
    if (field.equals("lang") && languageFieldParser != null){ return languageFieldParser;}
    if (field.equals("lastmod") && lastModFieldParser != null){ return lastModFieldParser;}
    if (field.equals("title")){ return getTitleFieldParser();}
    return new NullParser();
  }

  public Parser getTitleFieldParser(){
    NullParser titleParser = new NullParser();
    BreakIterator bi = BreakIterator.getWordInstance();
    String s = getTitle();
    bi.setText(s);
    int start = bi.first();
    for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
      String word = s.substring(start,end);
      if (!word.equals("")){
	titleParser.addWord(word,1);
      }
    }
    return titleParser;
  }

  public int noWordsInTitle(){
    String s= getTitle();
    int i=0;
    BreakIterator bi = BreakIterator.getWordInstance();
    bi.setText(s);
    int start= bi.first();
    for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
      for(int j=start;j<end; j++){
        if (Character.isLetterOrDigit(s.charAt(j))) { i++; break;}
      }
    }
    return i;
  }

  public Enumeration listScores(){
    return wordScores.elements();
  }

  public int nWords(){
    return words.size();
  }

  public void open(File f) throws IOException {
    title = "";
    description = "";
    wordScores = new Vector();
    words = new Vector();
    FileInputStream fis = new FileInputStream(f);
    if (useReader){
      reader = new BufferedReader(new InputStreamReader(fis,characterEncoding));
      parseAll(reader);
      reader.close();
    } else {
      InputStream is = new BufferedInputStream(fis);
      int length;
      byte buf[] = new byte[length= (int) f.length()];
      int offset=0;
      while(offset<length){
        int read = is.read(buf,offset,length-offset);
        if (read==-1){ break;}
        offset+=read;
      }
      OpenByteArrayInputStream obais = new OpenByteArrayInputStream(buf,0,offset);
      parseAll(obais);
      is.close();
    }
  }

  public void open(Reader reader) throws IOException {
    title = "";
    description = "";
    wordScores = new Vector();
    words = new Vector();
    parseAll(reader);
  }

  public void open(InputStream is) throws IOException {
    title = "";
    description = "";
    wordScores = new Vector();
    words = new Vector();
    parseAll(is);
  }

  public void configure(String args[],ParserLoader pl){
  }

  abstract void parseAll(Reader reader) throws IOException;

  void parseAll(InputStream is) throws IOException {
  }

}
