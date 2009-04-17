package org.tribosmap.model.app

import scala.xml.parsing.{FactoryAdapter}
import org.xml.sax.{Attributes, InputSource}
import scala.xml._
import javax.xml.parsers._
import scala.xml.factory.NodeFactory;
import org.xml.sax.InputSource;

/**
 * This class is used to load XML files inside the dalvik vm.
 * Because the Android Java APi is using an other implementation
 * for loading xml, than the standard java api this class is necessarry for loading files.
 * @author Meiko Rachimow
 */
class AndroidXmlFactoryAdapter extends FactoryAdapter with NodeFactory[Elem] {
  
 /** 
  * returns always true. Every XML node may contain text that the application needs
  */
  override def nodeContainsText( label:java.lang.String ): Boolean = true
  
  /** 
   * creates a scala.xml.Elem 
   */
  protected def create(pre: String, 
                       label: String, 
                       attrs: MetaData, 
                       scpe: NamespaceBinding, 
                       children:Seq[Node]): Elem = Elem( pre, label, attrs, scpe, children:_* )

  /** 
   * creates a node
   */
  def createNode(pre:String, 
                 label: String, 
                 attrs: MetaData, 
                 scpe: NamespaceBinding, 
                 children: List[Node] ): Elem = Elem( pre, label, attrs, scpe, children:_* )
  
  /** 
   * creates a text node
   */
  def createText( text:String ) = Text( text )

  /** 
   * create a processing instruction
   */
  def createProcInstr(target: String, data: String) = makeProcInstr(target, data)
  
  /** 
   * load XML document
   * 
   * here is our hack (we have to set the setNamespaceAware to false in the feature )
   * 
   * @param source
   * @return a new XML document object
   */
  override def loadXML(source: InputSource): Node = {
    // create parser
    val parser: SAXParser = try {
      val f = SAXParserFactory.newInstance()
      f.setNamespaceAware(false)
      f.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
      f.setFeature("http://xml.org/sax/features/namespaces", false)
      f.newSAXParser()
    } catch {
      case e: Exception ⇒
        Console.err.println("error: Unable to instantiate parser")
        throw e
    }
      
    // parse file
    scopeStack.push(TopScope)
    parser.parse(source, this)
    scopeStack.pop
    return rootElem
  } // loadXML
}
