package BlogFinder;

import java.util.*;

public class HowIndex {

  int mainScore;
  Hashtable fieldScores;
  String defString;

  public HowIndex() {
    mainScore = 0;
    fieldScores = new Hashtable();
    defString = "";
  }

  public HowIndex(String s) throws BadIndexingSyntaxException {
    mainScore = 0;
    fieldScores = new Hashtable();
    defString = s;
    setHowIndex(s);
  }

  public void setHowIndex(String s) throws BadIndexingSyntaxException {
    StringTokenizer tok = new StringTokenizer(s,";\n");
    while(tok.hasMoreTokens()){
      String t = tok.nextToken();
      StringTokenizer tok1 = new StringTokenizer(t,"=");
      if (tok1.countTokens() < 2){continue;}
      String item = removeWhiteSpace(tok1.nextToken());
      setEachItem(item, tok1);
    }
  }

  void setEachItem(String item, StringTokenizer tokens) throws BadIndexingSyntaxException {
    if (item.equalsIgnoreCase("fieldScores")){
      fieldScores(tokens.nextToken());
    } else {
      throw new BadIndexingSyntaxException("Unknown HowIndex setting "+item);
    }
  }

  public void fieldScores(String s) throws BadIndexingSyntaxException {
    StringTokenizer tok = new StringTokenizer(s,",");
    while(tok.hasMoreTokens()){
      String t = tok.nextToken();
      StringTokenizer tok1 = new StringTokenizer(t,":");
      if (tok1.countTokens()==0){ continue;}
      if (tok1.countTokens()==1){
        try {
          mainScore = Integer.parseInt(t);
        } catch (NumberFormatException e){
	        throw new BadIndexingSyntaxException(t+" is not a valid number");
        }
	      if (mainScore <0 || mainScore >255){
          throw new BadIndexingSyntaxException(t+" indexing score is outside range 0 to 255");
        }
      } else {
        String field = tok1.nextToken();
        String num = tok1.nextToken();
        int score = 0;
        try {
	        score = Integer.parseInt(num);
	      } catch (NumberFormatException e){
	        throw new BadIndexingSyntaxException(num+" is not a valid number");
	      }
	      if (score <0 || score >255){
	        throw new BadIndexingSyntaxException(num+" indexing score is outside range 0 to 255");
	      }
        if (field.equals("main")){
          mainScore = score;
        } else {
	        fieldScores.put(field,new Integer(score));
        }
      }
    }
  }

  public boolean getBoolean(String s,StringTokenizer tok) throws BadIndexingSyntaxException {
    if (!tok.hasMoreTokens()){
      throw new BadIndexingSyntaxException(" Missing Boolean Value for "+s);
    }
    String x = tok.nextToken();
    if (x.equalsIgnoreCase("true")){ return true;}
    if (x.equalsIgnoreCase("false")){ return false;}
    throw new BadIndexingSyntaxException("HowIndex Item "+s+" takes true or false values, not "+x);
  }

  public String removeWhiteSpace(String s){
    StringBuffer buf = new StringBuffer();
    int len = s.length();
    char cs[] = new char[1];
    for(int i=0;i<len;i++){
      char c = s.charAt(i);
      if (c != ' ' && c != '\t'){
        cs[0]=c;
        buf.append(cs);
      }
    }
    return buf.toString();
  }

  public String toString(){
    return defString;
  }

}