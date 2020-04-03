package BlogFinder;

public interface XMLCallBack {

  public void callBack( String whatisit, String itscontent);

  public void attCallBackPass1( String whatisit, String itscontent, XMLAttributeContext xac);

  public void attCallBackPass2( String whatisit, String itscontent, XMLAttributeContext xac);

  public void endElement( String element);
  
// We make to passes for Attributes so that on attribute can map another to a different field,
//  e.g. <PARAMETER NAME="target" VALUE="value">

} 