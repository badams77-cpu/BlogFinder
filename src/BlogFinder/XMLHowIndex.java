// Simple data structure describe how to index a particular piece of data

package BlogFinder;

import java.util.*;

public class XMLHowIndex extends HowIndex {

  int tagMatch;
  int matchPri;
  boolean isRemap = false;
  Hashtable remappedFields;

  public static int TAGANYORDER=0;
  public static int TAGINORDER= 1;
  public static int TAGIGNORE = 2;
  public static int TAGANYWHERE = 3;


  public XMLHowIndex(){
    super();
    tagMatch = TAGINORDER;
    matchPri = 1;
    isRemap = false;
    remappedFields = new Hashtable();
  }

  public XMLHowIndex(String s) throws BadIndexingSyntaxException {
    super();
    tagMatch = TAGINORDER;
    matchPri = 1;
    isRemap = false;
    remappedFields = new Hashtable();
    defString = s;
    setHowIndex(s);
  }



  void setEachItem(String item,StringTokenizer tokens) throws BadIndexingSyntaxException {
      if (item.equalsIgnoreCase("tagMatch")){
        tagMatch(tokens.nextToken());
      } else if (item.equalsIgnoreCase("matchPri")){
        matchPri(tokens.nextToken());
      } else if (item.equalsIgnoreCase("Remap")){
        if (tokens.countTokens()<4){return;}
        remap(tokens.nextToken(), tokens.nextToken(), tokens.nextToken());
      } else {
        super.setEachItem(item,tokens);
      }
  }

  public void remap(String target,String value,String fieldScores) throws BadIndexingSyntaxException {
    XMLHowIndex xhi1 = new XMLHowIndex();
    xhi1.fieldScores(fieldScores);
    XMLRemap xr = new XMLRemap(value,xhi1);
    Vector remapVec = (Vector) remappedFields.get(target);
    if (remapVec == null){
      remapVec = new Vector();
      remappedFields.put(target,remapVec);
    }
    remapVec.addElement(xr);
    isRemap = true;
  }

  public Enumeration getRemappedFields(String s){
    Vector vec = (Vector) remappedFields.get(s);
    if (vec == null){ return ( new Vector()).elements();}
    return vec.elements();
  }


  public void tagMatch(String s) throws BadIndexingSyntaxException {
    if (s.equalsIgnoreCase("ANYORDER")){
      tagMatch = TAGANYORDER;
    } else if (s.equalsIgnoreCase("INORDER")){
      tagMatch = TAGINORDER;
    } else if (s.equalsIgnoreCase("IGNORE")){
      tagMatch = TAGIGNORE;
    } else if (s.equalsIgnoreCase("ANYWHERE")){
      tagMatch = TAGANYWHERE;
    } else {
      throw new BadIndexingSyntaxException("Unknown tagMatch value: "+s);
    }
  }

  public void matchPri(String s) throws BadIndexingSyntaxException {
    try {
      matchPri = Integer.parseInt(s);
    } catch (NumberFormatException e){
      throw new BadIndexingSyntaxException("MatchPri: "+s+" was not a valid number");
    }
  }

}