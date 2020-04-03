package BlogFinder;

import java.util.*;

public class WildCardMatch {

  public WildCardMatch() {
  }

  public static boolean wildCardMatchWildEnd(String test,String match){
    if (match.endsWith("#")){
      return wildCardMatch(test,match.substring(0,match.length()-1));
    }
    StringTokenizer tok = new StringTokenizer(match,"*",true);
    int cc = tok.countTokens();
    if (cc == 0){return true;}
    int index = 0;
    boolean wild = false;
    while( tok.hasMoreTokens()){
      String t = tok.nextToken();
//      System.err.println(test+" - "+t);
      if (t.equals("*")){wild = true; continue;}
      if (t.equals("")){continue;}
      if (wild){
        int i = test.indexOf(t,index);
        if ( i <0){ return false;}
        index = i+t.length();
      } else {
        if (!test.startsWith(t,index)){return false;}
        index = index+t.length();
      }
      wild = false;
    }
//    if (!wild && index<test.length()){return false;}
    return true;
  }

  public static boolean wildCardMatch(String test,String match){
    StringTokenizer tok = new StringTokenizer(match,"*",true);
    int cc = tok.countTokens();
    if (cc == 0){return true;}
    int index = 0;
    boolean wild = false;
    while( tok.hasMoreTokens()){
      String t = tok.nextToken();
//      System.err.println(test+" - "+t);
      if (t.equals("*")){wild = true; continue;}
      if (t.equals("")){continue;}
      if (wild){
        int i = test.indexOf(t,index);
        if ( i <0){ return false;}
        index = i+t.length();
      } else {
        if (!test.startsWith(t,index)){return false;}
        index = index+t.length();
      }
      wild = false;
    }
    if (!wild && index<test.length()){return false;}
    return true;
  }

  public static boolean wildCardMatch(String test,String match,String wildcard){
    StringTokenizer tok = new StringTokenizer(match,wildcard,true);
    int cc = tok.countTokens();
    if (cc == 0){return true;}
    int index = 0;
    boolean wild = false;
    while( tok.hasMoreTokens()){
      String t = tok.nextToken();
//      System.err.println(test+" - "+t);
      if (t.equals(wildcard)){wild = true; continue;}
      if (t.equals("")){continue;}
      if (wild){
        int i = test.indexOf(t,index);
        if ( i <0){ return false;}
        index = i+t.length();
      } else {
        if (!test.startsWith(t,index)){return false;}
        index = index+t.length();
      }
      wild = false;
    }
    if (!wild && index<test.length()){return false;}
    return true;
  }

}