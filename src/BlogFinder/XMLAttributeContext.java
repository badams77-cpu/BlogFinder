package BlogFinder;

import java.util.Hashtable;

public class XMLAttributeContext {

// Used so we can map multiple Attributes at the same time

  public Hashtable remap;

  public XMLAttributeContext() {
    remap = new Hashtable();
  }

  public void setRemap(String attr, XMLHowIndex xhi){
    remap.put(attr,xhi);
  }

  public XMLHowIndex getRemap(String attr){
    return (XMLHowIndex) remap.get(attr);
  }

}