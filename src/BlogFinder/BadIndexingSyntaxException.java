

package BlogFinder;

public class BadIndexingSyntaxException extends Exception {

  public static final String CopyRight =
    "Exclusive Copyright of Barry David Ottley Adams 1998-1999 all Rights Reserved";


  String error;

  public BadIndexingSyntaxException(String s){
    error = s;
  }

  public String getError(){return error;}

} 