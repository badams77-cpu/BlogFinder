package BlogFinder;

import java.util.*;


/**
 * Title:        SiteSearch
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Barry Adams
 * @version 1.0
 */

public class NullWordsSource implements WordsSource {

  public NullWordsSource() {
  }

  public Enumeration listWords() {
    return (new Vector(0)).elements();
  }
  public Enumeration listScores() {
    return (new Vector(0)).elements();
  }
  public int numberOfWords() {
    return 0;
  }
  public int noWordsInTitle() {
    return 0;
  }
}