package BlogFinder;

import java.util.*;

public interface WordsSource {

  public Enumeration listWords();

  public Enumeration listScores();

  public int noWordsInTitle();

  public int numberOfWords();

}