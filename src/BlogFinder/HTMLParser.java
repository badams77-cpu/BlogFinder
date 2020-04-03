package BlogFinder;

import java.io.*;
import java.util.*;
import java.net.*;

public class HTMLParser extends HyperTextParser {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";


  boolean doMetaKeywords = true;
  boolean doMetaDescription = false;
  boolean doRobots = false;


  private boolean isComment;
  boolean isEnity;
  boolean isTag;
  boolean isTitle;
  boolean isBody;
  boolean isScript;
  boolean isStyle;
  protected boolean offTag;
  private boolean pleaseIndex;
  private boolean descriptionDone;
  private int bodyWords;
  private StringBuffer descriptionBuffer;
  private StringBuffer title;
  private StringBuffer word;
  protected StringBuffer tag;
  private StringBuffer attribute;
  private StringBuffer attributeData;
  private StringBuffer keywords;
  private StringBuffer comment;
  private int currentScore;
  private boolean wasDigit;
  private boolean wasStop;

  Hashtable activeTags;
  static Hashtable entityCodesHash;
  Hashtable attributes;

  protected int frameNumber = 1;
  private String localEncoding="";

  private char[] readBuffer = new char[1024];
  private int bufferPos = 0;
  private int bufferLen = 0;

  public HTMLParser(){
    useReader = false;
    if (Config.config !=null){
      doRobots = Config.config.metaRobots;
    }
    setEntities();
    reset();
  }

  public static void setEntities() {
    if (entityCodesHash != null) { return; }
    entityCodesHash = new Hashtable();
    for(int i=0; i<entities.length;i++){
      entityCodesHash.put(entities[i],new Integer(entityCodes[i]));
    }
  }

  private int  DESCRIPTIONLENGTH = 1200; // Length of Description
  private int TITLESCORE = 10; // Score for words in the Title
  private int KEYWORDSCORE = 5;
  private int DESCRIPTIONSCORE = 3;
  private int ALTTAGSCORE = 2;


  public void parseAll(Reader reader) throws IOException {
    this.reader = reader;
    this.inputStream = null;
    localEncoding="";
    parse();
  }

  public void parseAll(InputStream is) throws IOException {
    this.inputStream = is;
//    this.reader = new BufferedReader(new InputStreamReader(is,characterEncoding),1024);
    this.reader = new InputStreamReader(is,characterEncoding); // Use internal Buffer
    localEncoding="";
    parse();
  }

  public void reset(){
    super.reset();
    isComment = false;
    isEnity = false;
    isTag = false;
    isTitle = false;
    isBody = false;
    offTag = false;
    isScript = false;
    isStyle = false;
    pleaseIndex = true;
    HREFbase = "";
    bodyWords = 0;
    descriptionBuffer = new StringBuffer();
    descriptionDone = false;
    title = new StringBuffer();
    word = new StringBuffer();
    tag = new StringBuffer();
    keywords = new StringBuffer();
    activeTags = new Hashtable();
    links = new Vector();
    currentScore = 0;
    frameNumber = 1;
    isFrameSet = false;
    wasDigit = false;
    characterEncoding = "ISO-8859-1";
    localEncoding = "";
    bufferPos = 0;
    bufferLen = 0;
  }


  public String getDescription(){
    if (descriptionBuffer == null){return "";}
    if (descriptionBuffer.length()>DESCRIPTIONLENGTH){
      return descriptionBuffer.toString().substring(0,DESCRIPTIONLENGTH);
    }
    return descriptionBuffer.toString();
  }

  public String getKeywords(){
    if (keywords == null){return "";}
    return keywords.toString();
  }

  public String getTitle(){
    if (title == null){return "";}
    if (title.length() >100){ return title.toString().substring(0,100);}
    return title.toString();
  }

  public boolean shouldIndex(){
    return pleaseIndex;
  }

  private boolean isEndOfWord(char letter){
    if (Character.isWhitespace(letter)){return true;}
    if ( (letter=='.' || letter==',') && wasDigit){
      return false;
    }
    int type = Character.getType(letter);
    if ( ( (type == Character.END_PUNCTUATION || type == Character.OTHER_PUNCTUATION) &&
      letter != '`' && letter != 39) || letter == 153){
          return true;
      } else {
          return false;
      }
  }

  private void parse() throws IOException {
    int mode = 0;
    int l;
    char letter;
    char lastLetter = 0;
    wasStop = false;
    while(true){
      switch (mode) {
	      case 0:
          l = readChar();
          if ( l == -1){return;}
          letter = (char) l;
          if ( letter == '&'){
            letter = (char) parseEnity();
          }
          if ( letter == '<'){
            beginTag(); mode = 2; break;
          }
          if ( Character.isWhitespace(letter) ){
            break;
          }
          wasDigit = false;
          if ( !(Character.isLetter(letter) || (wasDigit = Character.isDigit(letter)))){
	          break;
          }
          if (! ( isBody || isTitle) || isScript || isStyle){ break;}
          word = new StringBuffer();
          word.append(letter);
          mode = 1; break;
        case 1:
          l = readChar();
          if ( l == -1){ closeWord(); return;}
          letter = (char) l;
          if ( letter == '&'){
            letter = (char) parseEnity(); // ParseEnity and append to word
          }
          if ( letter == '<'){
            closeWord(); beginTag(); mode =2 ; break;
          }
//          if ( Character.isWhitespace(letter) ){
//           closeWord(); mode = 0; break;
//          }
//          int type = Character.getType(letter);
//          if ( ( (type == Character.END_PUNCTUATION || type == Character.OTHER_PUNCTUATION) &&
//            letter != '`' && letter != 39) || letter == 153){
          if ( isEndOfWord(letter)){
            if (letter == '.' || letter == ',' || letter==':'){
              lastLetter = letter;
              mode = 13;
              break;
            }
            closeWord();
            if (letter==';'){ endSentance(); }
            mode = 0;
            break;
          }
          wasDigit = Character.isDigit(letter);
          word.append(letter);
	  break;
        case 13:   // Check for letter/digit letter :, or .
          l = readChar();
          if (l==-1){
            closeWord();
            return;
          }
          letter = (char) l;
          if (letter == '<'){
            if (lastLetter=='.' || lastLetter==':'){ endSentance(); }
            closeWord(); beginTag(); mode =2 ; break;
          }
          if (Character.isLetterOrDigit(letter)){
            word.append(lastLetter);
            word.append(letter);
            mode = 1;
            break;
          } else {
            closeWord();
            if (lastLetter=='.' || lastLetter==':'){ endSentance(); }
            mode = 0;
            break;
          }
        case 2:
          l = readChar();
          if ( l == -1){ return;}
          letter = Character.toLowerCase((char) l);
          if ( letter == '>'){
            endTag(); mode = 0; break;
          }
          if ( letter == '!'){
	    mode = 10; break;
          }
          if ( letter == '/'){
            offTag = true; mode = 3; break;
          }
	  if (isScript){ mode = 0; break;} // Ignore < in javascript unless </ or <!
          if ( Character.isWhitespace(letter) ){
            attributes = new Hashtable();
            mode = 4 ; break;
          }
          tag.append(letter);
          mode = 3; break;
        case 3:
          l = readChar();
          if ( l == -1){ return;}
          letter = Character.toLowerCase((char) l);
          if ( letter == '>'){
            endTag(); mode = 0;break;
          }
          if ( Character.isWhitespace(letter) ){
            attributes = new Hashtable();
            mode = 4; break;
          }
          tag.append(letter);
          break;
        case 4:
          l = readChar();
          if ( l == -1){ return;}
          letter = Character.toLowerCase((char) l);
          if ( Character.isLetter(letter)){
            attribute = new StringBuffer();
            attribute.append(letter);
	    mode = 5;break;
          }
          if ( letter == '>'){
            endTag(); mode = 0;break;
          }
          break;
	case 5:
          l = readChar();
          if ( l == -1){ return;}
          letter = Character.toLowerCase((char) l);
          if ( Character.isLetter(letter) || letter=='-'){
            attribute.append(letter);
            break;
          } else {
            if ( letter == '='){
              attributeData = new StringBuffer();
              mode = 7; break;
            }
            if ( letter == '>'){
              endTag(); mode = 0;break;
            }
          }
	  mode = 6; break;
        case 6:
          l = readChar();
          if ( l == -1){ return;}
          letter = Character.toLowerCase((char) l);
          if ( Character.isLetter(letter)){
            attribute = new StringBuffer();
            attribute.append(letter);
            mode = 5; break;
          }
          if ( letter == '='){
            attributeData= new StringBuffer();
            mode = 7; break;
          }
          if ( letter == '>'){
            endTag(); mode = 0;break;
          }
          break;
        case 7:
          l = readChar();
          if ( l == -1){ return;}
          letter = (char) l;
          if (Character.isWhitespace(letter)){
            break;
          }
          if (letter == '"'){
            mode = 9; break;
          }
          if (letter == '\''){
            mode = 12; break;
          }
          if ( letter == '>'){
            endTag(); mode = 0;break;
          }
          attributeData.append(letter);
	  mode = 8; break;
        case 8:
          l = readChar();
          if ( l == -1){ return;}
          letter = (char) l;
          if (Character.isWhitespace(letter)){
            endAttribute();
            mode = 4; break;
          }
          if ( letter == '>'){
	    endAttribute();
            endTag(); mode = 0;break;
          }
          attributeData.append(letter);
          break;
        case 9:
          boolean isQuote = false;
          while(!isQuote){
            l = readChar();
            if ( l == -1){ return;}
            letter = (char) l;
            if (letter == '"'){ isQuote=true;break;}
            attributeData.append(letter);
          }
          endAttribute();
          mode = 4;
	  break;
        case 10:
          comment = new StringBuffer();
          l = readChar();
          if ( l == -1){ return;}
          if ( l == '-'){
            l = readChar();
            if ( l == -1){ return;}
            if ( l == '-'){ mode = 11; break;}
          }
          isComment = true;
          while (isComment){
            l = readChar();
            if ( l == -1){ return;}
            if ( l == '>'){ isComment = false;} else {
              comment.append( (char) l);
            }
          }
          String comString = comment.toString();
          if (comString.equalsIgnoreCase("NOINDEX")){
            pleaseIndex = false;
          }
          if (comString.equalsIgnoreCase("DONT SEARCH")){
            pleaseIndex = false;
          }
          comment = null;
          mode = 0;
	        break;
        case 11:
          while(true){
            l = readChar();
            if ( l == -1){ return;}
            if ( l == '-'){
              l = readChar();
              if ( l == -1){ return;}
              if ( l == '-'){
                l = readChar();
		            while( l == '-'){
		              l = readChar();
		            }
                if ( l == -1){ return;}
                if (l == '>'){ mode = 0; comment=null; break;}
              }
            }
            if (comment !=null){
              comment.append( (char) l);
              int cl = comment.length();
              if (cl==6 && comment.toString().equalsIgnoreCase("SPIDER")){
                mode = 0;
                comment = null;
                break;
              }
              if (cl==7 && comment.toString().equalsIgnoreCase("NOINDEX")){
                pleaseIndex = false;
              }
            }
          }
          break;
        case 12:
          boolean isApost = false;
          while(!isApost){
            l = readChar();
            if ( l == -1){ return;}
            letter = (char) l;
            if (letter == '\''){ isApost=true;break;}
            attributeData.append(letter);
          }
          endAttribute();
          mode = 4;
	  break;
//      END CASE
      }
    }
  }

  private void beginTag(){
    tag = new StringBuffer();
    offTag = false;
    isComment = false;
  }

  private void endAttribute(){
    if ( attributes !=null && attribute != null && attributeData != null){
      attributes.put(attribute.toString().toLowerCase(),attributeData.toString());
    }
  }

  public void ANAME(String name){}

  public void endSentance(){
    addWord(".",0);
  }

  protected void endTag() throws IOException {
    String tag1 = tag.toString();
    if (tag1.equalsIgnoreCase("P")){
      endSentance();
    }
    if (!offTag && tag1.equals("embed")){
      String src = (String) attributes.get("src");
      if (src != null && !src.equals("")){
        links.addElement(substituteEnitiesURLs(src)+"!embed!");
      }
    }
    if (!offTag && tag1.equals("meta")){
      if (attributes != null){
	      String att = (String) attributes.get("name");
              if (att !=null && att.equalsIgnoreCase("robots") && doRobots){
                String robots = ((String) attributes.get("content")).toLowerCase();
		  if (robots.indexOf("noindex")>=0){ pleaseIndex = false;
//System.err.println("No index");
                  }
  	  	  if (robots.indexOf("nofollow")>=0){ follow = false;
//System.err.println("No follow");
                   }
	  	  if (robots.indexOf("none")>=0){ follow = false; pleaseIndex = false;
//System.err.println("Robots = none");
                  }

              }
	      if ( att != null && att.equalsIgnoreCase("description") && doMetaDescription){
	        String desc = (String) attributes.get("content");
          if (desc != null && !desc.equals("")){
            descriptionBuffer = new StringBuffer(desc);
            descriptionDone = true;
	          StringTokenizer tok = new StringTokenizer(desc,"\t\r\n ,.:;?!");
	          while(tok.hasMoreTokens()){
	            String word = tok.nextToken();
	            if (word.length() != 0){
	              addWord(word,DESCRIPTIONSCORE);
	              }
            }
	        }
        }
        if ( att != null && att.equalsIgnoreCase("keywords") && doMetaKeywords){
	        String key = (String) attributes.get("content");
	        if ( key != null){
	          StringTokenizer tok = new StringTokenizer(key,"\t\r\n ,;?!");
	          while(tok.hasMoreTokens()){
	            String word = tok.nextToken();
	            if (word.length() != 0){
	              addWord(word,KEYWORDSCORE);
	              if (keywords.length() != 0){keywords.append(",");}
                keywords.append(word);
	            }
            }
          }
        }
        String httpe = (String) attributes.get("http-equiv");
        if ( httpe != null && httpe.equalsIgnoreCase("refresh")){
          String cont = (String) attributes.get("content");
	        if (cont != null){
	          StringTokenizer tok = new StringTokenizer(cont,";");
            while(tok.hasMoreTokens()){
              String partial = tok.nextToken();
              if (partial.length()>4){
                String part1 = partial.substring(0,4);
                if (part1.equalsIgnoreCase("URL=")){
                  String urlx = partial.substring(4);
                  links.addElement(substituteEnitiesURLs(urlx));
                }
              }
            }
          }
        }
        if (httpe != null && httpe.equalsIgnoreCase("Content-Type")){
          setContentType( (String) attributes.get("content"));
        }
      }
    }
    if (!offTag && tag1.equals("a")){
      if (attributes != null){
        String name = (String) attributes.get("name");
        if (name!=null){
          ANAME(name);
        }
        String href = (String) attributes.get("href");
        String target = (String) attributes.get("target");
        if (href != null && !href.equals("")){
//	  System.err.println(href);
          if (target == null || target.equals("")){
            links.addElement(substituteEnitiesURLs(href));
          } else {
            links.addElement(substituteEnitiesURLs(href));
          }
        }
      }
    }
    if (!offTag && tag1.equals("frame")){
        String href = (String) attributes.get("src");
        String name = (String) attributes.get("name");
        if (name == null || name.equals("")){
          name = "_RS_"+Integer.toString(frameNumber++);
        }
        if (href != null && !href.equals("")){
          Logging.finer(" Frame Src= "+href);
          System.err.println(" Frame Src= "+href);
          links.addElement(substituteEnitiesURLs(href));
          isFrameSet = true;
        }
    }
     if (!offTag && tag1.equals("iframe")){
        String href = (String) attributes.get("src");
        String name = (String) attributes.get("name");
        if (name == null || name.equals("")){
          name = Integer.toString(frameNumber++);
        }
        if (href != null && !href.equals("")){
          Logging.finer(" Frame Src= "+href);
          System.err.println(" Frame Src= "+href);
          links.addElement(substituteEnitiesURLs(href));
          isFrameSet = true;
        }
    }

    if (!offTag && tag1.equals("base")){
        String href = (String) attributes.get("href");
        if (href != null && !href.equals("")){
          HREFbase = href;
        }
        String target = (String) attributes.get("target");
        if (target != null && !target.equals("")){
          targetBase = target;
        }
    }
    if (!offTag && tag1.equals("area")){
        String href = (String) attributes.get("href");
        String target = (String) attributes.get("target");
        if (href != null && !href.equals("")){
          if (target == null || target.equals("")){
            links.addElement(substituteEnitiesURLs(href));
          } else {
            links.addElement(substituteEnitiesURLs(href));
          }
        }
    }
    if (!offTag && tag1.equals("img")){
      String alt = (String) attributes.get("alt");
      if (alt != null && !alt.equals("")){
        indexalt(alt);
      }
    }
    if (offTag){
      activeTags.remove(tag1);
    } else {
      activeTags.put(tag1,tag1);
    }
    setMode();
  }

  void closeWord(){
    if (isScript || isStyle){ return;}
    String wordS = word.toString();
    if ( isBody){
      addWord(wordS,currentScore);
      if (!descriptionDone){
        if ( descriptionBuffer.length() + wordS.length() < DESCRIPTIONLENGTH){
          if (descriptionBuffer.length() != 0){ descriptionBuffer.append(" "); }
	        descriptionBuffer.append(wordS);
        } else {
          descriptionDone = true;
        }
      }
    }
    if ( isTitle){
      addWord(wordS,TITLESCORE);
      if (title.length() != 0){ title.append(" ");}
      title.append(wordS);
    }
  }

  void setMode(){
    boolean isTitleOld = isTitle;
    isTitle = ( activeTags.get("title") != null);
    if (isTitleOld && !isTitle){ endSentance(); }
    isBody = (activeTags.get("body") != null);
    isScript = (activeTags.get("script") != null);
    isStyle = (activeTags.get("style") != null);
    currentScore = 1;
  }

  protected void indexalt(String s){
    String s1 = substituteEnities(s);
    StringBuffer altword = null;
    boolean wordopen = false;
    wasDigit = false;
    boolean addedSome = false;
    for(int i=0;i<s.length();i++){
      char c = s.charAt(i);
      if (!wordopen){
        if ( !( (wasDigit=Character.isDigit(c)) || Character.isLetter(c) )){
          continue;
        }
        wordopen  = true;
        altword = new StringBuffer();
        altword.append(c);
      } else {
        if (isEndOfWord(c)){
          wasDigit=false;
          wordopen = false;
	  if (!addedSome){ endSentance(); }
          addedSome = true;
          addWord( altword.toString(), ALTTAGSCORE);
        } else {
          wasDigit = Character.isDigit(c);
          altword.append(c);
        }
      }
    }
    if (wordopen){
      if (!addedSome){ endSentance(); }
      addedSome = true;
      addWord( altword.toString(), ALTTAGSCORE);
    }
    if (addedSome){ endSentance(); }
  }

  protected void addWord(String word,int score){
    if (word.length()>1 && word.endsWith(".")){
      word = word.substring(0,word.length()-1);
      words.addElement(word);
      wordScores.addElement(new Integer(score));
      wasStop = false;
      addWord(".",0);
      return;
    } else if ( word.endsWith(",")){
      word = word.substring(0,word.length()-1);
      if (word.equals("")){ return; }
    }
    if (word.equals(".")){
      if (wasStop){ return; }
      wasStop = true;
    } else {
      wasStop = false;
    }
    words.addElement(word);
    wordScores.addElement(new Integer(score));
  }

  private int parseEnity() throws IOException {
    StringBuffer entityname = new StringBuffer();
    int l=0;
    boolean isLetter=true;
    while( isLetter ){
      l = readChar();
      if ( l == -1){ return ' ';}
      if ( l == '#'){ return parseNumEnity();}
      if ( l == '<'){ return l;}
      isLetter = Character.isLetter( (char) l);
      if (isLetter){ entityname.append( (char) l); }
    }
    if (l != ';'){ return ' ';}
    Integer entitycode = (Integer) entityCodesHash.get(entityname.toString());
    if (entitycode == null){ return ' ';}
    return ((Integer) entitycode).intValue();
  }

  private int parseNumEnity() throws IOException {
    StringBuffer entityname = new StringBuffer();
    int l=0;
    boolean isDigit=true;
    while( isDigit ){
      l = readChar();
      if ( l == -1){ return ' ';}
      if ( l == '<'){ return l;}
      isDigit = Character.isDigit( (char) l);
      if (isDigit){ entityname.append( (char) l); }
    }
    if (l != ';'){ return ' ';}
    try {
      return Integer.parseInt(entityname.toString());
    } catch (Exception e){
      return ' ';
    }
  }

  private String substituteEnities_old(String s){
    if (s.indexOf('&') <0){ return s;}
    int pos = 0;
    int length = s.length();
    StringBuffer out = new StringBuffer();
    StringBuffer entity = null;
    while( pos<length){
      char c = s.charAt(pos++);
      if ( c == '&'){
	entity = new StringBuffer();
        continue;
      } else if ( c ==';' && entity != null){
	String en = entity.toString();
	char c1 = 0;
	try {
	  c1 = (char) Integer.parseInt(en);
	} catch (Exception e){
	  try {
  	    c1 = (char) ((Integer) entityCodesHash.get(en)).intValue();
	  } catch (Exception e1){}
        }
	if (c1 != 0){
	  out.append(c1);
        }
        entity = null;
        continue;
      }
      if (entity == null){
	out.append(c);
      } else {
	entity.append(c);
      }
    }
    return out.toString();
  }

  public static String substituteEnitiesURLs(String s){
    if (s.indexOf(' ')>=0){
      StringBuffer out = new StringBuffer();
      int pos = 0;
      int length = s.length();
      while(pos<length){
        int pos1 = s.indexOf(' ',pos);
        if (pos1<0){
          out.append(s.substring(pos));
          break;
        } else {
          if (pos1 != pos){
            out.append(s.substring(pos,pos1));
	  }
          out.append("%20");
          pos = pos1+1;
        }
      }
      s = out.toString();
    }
    return substituteEnities(s);
  }

  public static String substituteEnities(String s){
//    setEntities();
    if (s.indexOf('&')<0){ return s;}
    int pos=0;
    int length = s.length();
    StringBuffer out = new StringBuffer();
    while(pos<length){
      int pos1 = s.indexOf('&',pos);
      if (pos1<0){
        out.append(s.substring(pos));
        break;
      } else {
        if (pos1 != pos){
          out.append(s.substring(pos,pos1));
	      }
      	int pos2 = s.indexOf(';',pos1);
	      if (pos2<0){
	        out.append(s.substring(pos1));
	        break;
       	}
      	String en = s.substring(pos1+1,pos2);
	      char c1 = 0;
      	try {
          if (en.length()>1 && en.charAt(0)=='#'){
            en = en.substring(1);
            if (en.length()>1 &&
              (en.charAt(0)=='x' || en.charAt(0)=='X')){
              c1 = (char) Integer.parseInt( en.substring(1),16);
            } else {
	            c1 = (char) Integer.parseInt(en);
            }
          } else {
            c1 = (char) Integer.parseInt(en);
          }
      	} catch (Exception e){
       	  try {
  	        c1 = (char) ((Integer) entityCodesHash.get(en)).intValue();
	        } catch (Exception e1){}
        }
      	if (c1 != 0){
	        out.append(c1);
          pos = pos2+1;
        } else {
          out.append('&');
          pos = pos1+1;
        }
      }
    }
    return out.toString();
  }

  private int readChar() throws IOException {
    if (bufferPos >= bufferLen){
      bufferPos = 0;
      bufferLen = reader.read(readBuffer);
      if (bufferLen <0){
        return -1;
      }
      return readBuffer[bufferPos++];
    } else {
      return readBuffer[bufferPos++];
    }
  }

  private void setContentType(String contentType) throws IOException {
//  Change character Encoding and Continue
    if (localEncoding !=""){ return; } // Don't change Encoding Twice
    StringTokenizer tok = new StringTokenizer(contentType,";");
    if (tok.countTokens()<2){ return;}
    tok.nextToken();
    String var = tok.nextToken("=");
    if (var.length()<1){ return; }
    if (var.charAt(0)==';'){ var = var.substring(1); }
    int j=0;
    for(int i=0; i<var.length();i++){
      if (var.charAt(i)==' '){ j++;} else { break;}
    }
    var = var.substring(j);
    if (!var.equalsIgnoreCase("charset")){ return;}
    String charSet = tok.nextToken();
    if (charSet.equals(characterEncoding)){ return; } // Already correct Encoding
    if (inputStream == null){
      throw new IOException("HTML Parser called with Reader cannot change Encoding");
    }
    if (! (inputStream instanceof OpenByteArrayInputStream) ){
      throw new IOException("HTML Parser not called with OpenByteArrayInputStream");
    }
    OpenByteArrayInputStream obais = (OpenByteArrayInputStream) inputStream;
    byte[] buf = obais.getBuffer();
    int length = obais.getLength();
    int offset = obais.getOffset();
    reader.close();
    obais.close();
    obais = new OpenByteArrayInputStream(buf,offset,length);
    inputStream = obais;
    try {
      reader = new BufferedReader(new InputStreamReader(obais,charSet),1024);
    } catch (UnsupportedEncodingException e){
      Logging.warning("Unknown charset "+charSet+" parsing with default ISO-8859-1 charSet");
      System.err.println("Unknown charset "+charSet+" parsing with default ISO-8859-1 charSet");
      reader = new BufferedReader(new InputStreamReader(obais),1024);
      charSet = "iso-8859-1";
    }
    Logging.info("Changed Encoding to "+charSet);
    System.err.println("Changed Encoding to "+charSet);
    reset();
    characterEncoding = charSet;
    localEncoding = charSet;
    //   We will restart reading the file from the beginning
    return;
  }

 private static final String entities[] = {
    "lt","gt","amp","quot","nbsp","shyp","AElig","Aacute","Acirc",
    "Agrave","Aring","Atilde","Auml","Ccedil","ETH","Eacute",
    "Ecirc","Egrave","Euml","Iacute","Icirc","Igrave","Iuml",
    "Ntilde","Oacute","Ocirc","Ograve","Oslash","Otilde","Ouml",
    "THORN","Uacute","Ucirc","Ugrave","Uuml","Yacute",
    "szlig","aelig","aacute","acirc","agrave","aring","atilde","auml",
    "ccedil","eth","eacute","ecirc","egrave","euml",
    "iacute","icirc","igrave","iuml","ntilde",
    "oacute","ocirc","ograve","oslash","otilde","ouml","thorn",
    "uacute","ucirc","ugrave","uuml","yacute","yuml",
    "iexcl","cent","pound","curren","yen","brvbar","sect",
    "uml","copy","ordf","laquo","not","shy","reg","macr",
    "deg","plusmn","sup2","sup3","actute","micro","para","middot",
    "cedil","sup1","ordm","raquo","frac14","frac12","frac34","iquest",
    "times","divide","hibar","die","rsquo"
  };

  private static final char[] entityCodes = {
    '<','>','&','"',' ','-','\u00c6','\u00c1','\u00c2',
    '\u00c0','\u00c5','\u00c3','\u00c4','\u00c7','\u00d0','\u00c9',
    '\u00ca','\u00c8','\u00cb','\u00cd','\u00ce','\u00cc','\u00cf',
    '\u00d1','\u00d3','\u00d4','\u00d2','\u00d8','\u00d5','\u00d6',
    '\u00de','\u00da','\u00db','\u00d9','\u00dc','\u00dd',
    '\u00df','\u00e6','\u00e1','\u00e2','\u00e0','\u00e5','\u00e3','\u00e4',
    '\u00e7','\u00f0','\u00e9','\u00ea','\u00e8','\u00eb',
    '\u00ed','\u00ee','\u00ec','\u00ef','\u00f1',
    '\u00f3','\u00f4','\u00f2','\u00f8','\u00f5','\u00f6','\u00fe',
    '\u00fa','\u00fb','\u00f9','\u00fc','\u00fd','\u00ff',
    '\u00a1','\u00a2','\u00a3','\u00a4','\u00a5','\u00a6','\u00a7',
    '\u00a8','\u00a9','\u00aa','\u00ab','\u00ac','\u00ad','\u00ae','\u00af',
    '\u00b0','\u00b1','\u00b2','\u00b3','\u00b4','\u00b5','\u00b6','\u00b7',
    '\u00b8','\u00b9','\u00ba','\u00bb','\u00bc','\u00bd','\u00be','\u00bf',
    '\u00d8','\u00f7','\u00af', '\u00a8','\''
  };


  public static void main(String argv[]){
    if (argv.length != 1){
      System.err.println("Usaged HTMLParser file");
      System.exit(255);
    }
    HTMLParser hp = new HTMLParser();
    try {
      Reader fp = new FileReader(argv[0]);
      hp.parseAll(fp);
      System.out.println("Title: "+hp.getTitle());
      System.out.println("Descrption: "+hp.getDescription());
      System.out.println("\n "+hp.links.size()+" Links\n");
      for(Enumeration en=hp.links.elements();en.hasMoreElements();){
        Object link = en.nextElement();
        if (link instanceof String){
          System.out.println(link);
        } else {
          URL url = (URL) link;
          System.out.println(url);
        }
      }
      for(Enumeration en=hp.words.elements();en.hasMoreElements();){
        System.err.print( (String) en.nextElement()+" ");
      }
    } catch (IOException e){
      e.printStackTrace(System.err);
    }
  }



}
