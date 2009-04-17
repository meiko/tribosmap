package org.tribosmap.frontend.common.file

import java.io.File
import android.content.Intent
import android.app.Activity
import android.os.Bundle
import android.view.{Menu, MenuItem, View}
import android.widget.{ListView, AdapterView, ArrayAdapter, ImageButton, EditText, TextView}
import android.widget.AdapterView.OnItemClickListener
import android.text.{TextWatcher, Editable}
import org.tribosmap.frontend.common.list._

/**
 * An activity for choosing a file,
 * returns the file in the bundle to the calling activity.
 * <p>
 * it is possible to select a file filter extension.
 * (key FileChooserComCodes.KEY_FILTER in the bundle)
 * And it is possible to specify a starting root directory
 * (key FileChooserComCodes.KEY_ROOTDIR in the bundle)
 * <p>
 * the result is saved with the key FileChooserComCodes.RESULT
 * in the bundle
 * 
 * @author Meiko Rachimow
 */
class FileChooser extends TribosListActivity[FileRow] {
  
  /**
   * the rootdirectory
   */
  private lazy val rootDirectory = {
    new File({
      val givenRootDir = 
        getIntent.getExtras.getString(FileChooserComCodes.KEY_ROOTDIR)
      
      if(givenRootDir != null) givenRootDir
      else "/"
    })
  }
  
  private lazy val filterExtensions = {
    
    if(getIntent.getExtras != null) 
      getIntent.getExtras.getStringArray(FileChooserComCodes.KEY_FILTER)
    
    else 
      Array[String]()
  }
  
  private lazy val okButton = 
    findViewById(R.id.ok_btn).asInstanceOf[ImageButton]
  
  private lazy val cancelButton = 
    findViewById(R.id.cancel_btn).asInstanceOf[ImageButton]
  
  private lazy val editFileNameField = 
    findViewById(R.id.edit_file_name).asInstanceOf[EditText]
  
  private lazy val showFileNameField = 
    findViewById(R.id.file_dir_text).asInstanceOf[TextView]
  
  protected def menuId = R.menu.import_list
  protected def menuItemGroupId = R.id.menu_group
  
  protected override def onCreate(savedInstanceState : Bundle) {
    
    setContentView(R.layout.file_list)
    super.onCreate(savedInstanceState)
    
    showFileNameField.setText(rootDirectory + "/" + editFileNameField.getText)
    
    okButton.setOnClickListener(new OnClickAction(() ⇒ {
      finish(showFileNameField.getText.toString)
    }))
 
    cancelButton.setOnClickListener(new OnClickAction(() ⇒ {
      setResult(Activity.RESULT_CANCELED)
      finish()
    }))  
    
    
    editFileNameField.setFilters(
      Array(new android.text.LoginFilter.UsernameFilterGeneric()))
    
    editFileNameField.addTextChangedListener(new TextWatcher() {
      override def beforeTextChanged(charSeq: CharSequence, i: Int, i2: Int, i3: Int){}
      override def onTextChanged(charSeq: CharSequence, i: Int, i2: Int, i3: Int){}
      override def afterTextChanged(text: Editable) = {
        val file = new File(showFileNameField.getText.toString)
        showFileNameField.setText(file.getParent + "/" + editFileNameField.getText)
        true
      }
    })    
    
  }
  
  private def finish(fileName: String) {
    
    val bundle = new Bundle()
    bundle.putString(FileChooserComCodes.RESULT, fileName);
    val intent = new Intent()
    intent.putExtras(bundle)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }
  
  private def compare(a : File, b : File) = {
    
    (a.isDirectory && !b.isDirectory) || 
    ((a.isDirectory == !b.isDirectory) && (a.compareTo(b) < 0))
  }
  
  private def selection(f : File): Boolean = {
    f.isDirectory || filterExtensions.find(ext ⇒ f.getName.endsWith(ext)).isDefined
  }
  
  private def listChildFiles(file : File) = {
    file.listFiles.filter(selection).toList.sort(compare)
      .map(file ⇒ new FileRow(file, getResources))
  }
  
  protected def loadItems() {
    load(listChildFiles(rootDirectory))
  }
  
  /**
   * load the items into the list, 
   * the listadapter has to be notified about the new items
   */
  protected def load(newItems: List[FileRow]) {
    getListAdapter.clear
    newItems.foreach(
      item ⇒ {
        getListAdapter.add(item)
      }
    )
    getListAdapter.notifyDataSetChanged 
  }
  
  override def onListItemClick(parent:ListView, view:View, position:Int, id:Long) {
    
    val selectedItem = getSelectedObject
    
    if(selectedItem.file.isDirectory) {
      
      showFileNameField.setText(
        selectedItem.file.getAbsolutePath + "/" + editFileNameField.getText)
      
      if(selectedItem.file.getAbsolutePath.equals(rootDirectory.getAbsolutePath)) 
        load(listChildFiles(rootDirectory))
      
      else
        load( List(new FileRow(
          new File(selectedItem.file.getParent), true, getResources))
                 ++ listChildFiles(selectedItem.file))
      
    } else {
      showFileNameField.setText(selectedItem.file.getAbsolutePath)
      editFileNameField.setText(selectedItem.file.getName)
      super.onListItemClick(parent, view, position, id)
    }
  }
 
  override def onCreateOptionsMenu(menu : Menu) : Boolean = {
    //getMenuInflater.inflate(R.menu.import_list, menu)
    true
  }
    
  override def onOptionsItemSelected(item : MenuItem) : Boolean = {
    
    item.getItemId() match {
      case R.id.import_btn ⇒ {
        
        true
      }
      case _ ⇒ {
        false
      }
    }
  }
}
