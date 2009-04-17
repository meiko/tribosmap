package org.tribosmap.frontend.trace

import java.io.File
import java.lang.Runnable

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.app.AlertDialog
import android.view.{Menu, MenuItem, MenuInflater}
import android.util.Log
import android.os.Handler


import scala.actors.Actor.actor

import org.tribosmap.model.business.domain.Trace
import org.tribosmap.frontend.common.list.TribosListActivity
import org.tribosmap.frontend.common.{ActivityHelper, Messager}
import org.tribosmap.frontend.common.file.{FileChooser, FileChooserComCodes}
import org.tribosmap.model.ServiceAccess

/**
 * This Activity holds a list of all Traces.
 * These Trace objects were fetched from the repository.
 * The user can delete, export, or edit them here.
 * @author Meiko Rachimow
 */
class TraceListActivity extends TribosListActivity[TraceRow] 
  with ActivityHelper 
  with ServiceAccess {
  
  ////////////////////////////////////////////////////////
  //Attributes
  ////////////////////////////////////////////////////////
    
  /**
   * a key used when communicate with the TraceEditActivity
   */
  private[this] val KEY_EDITTRACE = 11
  
  /**
   * a key used when communicate with the FileChooser (when exporting)
   */
  private[this] val KEY_CHOOSEFILE = 22
  
  ////////////////////////////////////////////////////////
  //Actions
  ////////////////////////////////////////////////////////
  
  /**
   * Called when the user has pressed the delete button in the menu.
   * It will show a dialog, in which the user can delete the selected Trace.
   */
  private[this] def deleteAction() {
    
    val traceRow = getSelectedObject
    
    /**
     * delete a Trace from the repository
     */
    def deleteTrace() = tryCatch {
      traceRow.trace.delete
      getListAdapter.remove(traceRow)
      Messager.makeMessageShort(this, getString(R.string.trace_deleted))
    }
    
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.dialog_question))
      .setMessage(getString(R.string.dialog_question_delete) + 
                    traceRow.title + "?")
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), deleteTrace _ )
      .setNegativeButton(getString(R.string.dialog_no), ()⇒())
      .show()
  }
  
  
  /**
   * Called when the user has pressed the export button in the menu.
   * It will open a dialog to start the export, by opening a FileChooser first.
   * After that the export will be started, if the user has selected a correct file.
   * To export a Trace the TraceService is used.
   */
  private[this] def exportAction(fileName: String) =  tryCatch {
    
    val targetFile = new File(fileName)
    assume(targetFile.getParentFile.canWrite, 
           getString(R.string.cannot_open_file_write) + targetFile)
    
    /**
     * start exporting a trace to a file, by using the TraceService
     */
    def writeTraceToFile() = tryCatch {
      
      val lastSelectedTraceRow = getListAdapter.getItem(lastChoosedRowIndex)
      val trace = lastSelectedTraceRow.trace
      
      val handler = new Handler()
      Messager.makeMessage(this, getString(R.string.trace_exporting))
      actor {
        trace.export(targetFile)
        handler.post(new Runnable { def run() {
          Messager.makeMessageShort(TraceListActivity.this, 
                                    getString(R.string.trace_exported)+ 
                                    " : " + trace.getName)                        
        }})
      }
    }

    if(! targetFile.createNewFile()) {
      new AlertDialog.Builder(this)            	
        .setTitle(getString(R.string.dialog_question))
        .setMessage(getString(R.string.dialog_question_overwrite) + fileName + "?")
        .setIcon(R.drawable.warning)
        .setPositiveButton(getString(R.string.dialog_yes), () ⇒ {
          targetFile.delete()
          targetFile.createNewFile()
          writeTraceToFile()
        })
        .setNegativeButton(getString(R.string.dialog_no), ()⇒())
        .show()
    } else {
      writeTraceToFile()
    }
  } 
  
  /**
   * Called when the user wants to export a Trace.
   * It will open the FileChooser to select a targetfile for the export operation.
   */
  private[this] def selectExportDestinationAction() {
    //set the last selected object
    getSelectedObject
    Messager.makeMessageShort(this, getString(R.string.window_open))
    val bundle = new Bundle()
    bundle.putStringArray(FileChooserComCodes.KEY_FILTER, Array("gpx"))
    bundle.putString(FileChooserComCodes.KEY_ROOTDIR, preferences.exportFolder)
    
    val intent = new Intent(this, classOf[FileChooser])
    intent.putExtras(bundle)
    startActivityForResult(intent, KEY_CHOOSEFILE)
    
  } 
  
  /**
   * Called when the user has pressed the edit button in the menu.
   * It will open the TraceEditActivity to edit the selected Trace.
   */
  private[this] def editTraceAction() {
    Messager.makeMessageShort(this, "open window: TraceEditActivity")
    val trace = getSelectedObject.trace
    val bundle = new Bundle()
    bundle.putSerializable(TraceComCodes.TRACE_PARAMETER_ID, trace)
    val intent = new Intent(this, classOf[TraceEditActivity])
    intent.putExtras(bundle)
    startActivityForResult(intent, KEY_EDITTRACE)
    
  }
  
  /**
   * This method reloads the Trace at the last selected position
   * from the database, and refreshes the row in the listview.
   */
  private[this] def reloadAtLastSelectedPosition() {
    val lastSelectedTraceRow = getListAdapter.getItem(lastChoosedRowIndex)
    getListAdapter.remove(lastSelectedTraceRow)
    getListAdapter.insert(
      new TraceRow(lastSelectedTraceRow.trace.reload(), getResources), 
      lastChoosedRowIndex)
    getListAdapter().notifyDataSetChanged()
  }
  
  ////////////////////////////////////////////////////////
  // Overriden Methods
  ////////////////////////////////////////////////////////
  
  /**
   * This method will load items (all Traces) into the listview of this activity.
   * The listadapter will be notified about this items.
   * The Traces are fetched from the traceService, wich loads them from the repository.
   */
  protected override def loadItems() {
    val handler = new Handler()
    val msg = Messager.makeMessage(this, getString(R.string.trace_loading))
    getListAdapter.clear()
    actor {
      traceService.foreachTrace(trace ⇒ {
        val traceRow = new TraceRow(trace, getResources)
        handler.post(new Runnable { def run() {
          getListAdapter.add(traceRow)
          msg.cancel()
        }})
        Thread.sleep(50)
      })
      handler.post(new Runnable { def run() { msg.cancel() }})
    }
  }
  
  /**
   * @return the menuId for the map list
   * @see TribosListActivity
   */
  protected override def menuId = R.menu.trace_list
  
  /**
   * @return the id of the menu group
   * @see TribosListActivity
   */
  protected override def menuItemGroupId = R.id.menu_group
  
  /**
   * @return true if the layout for the option menu was successful inflated
   * @see TribosListActivity
   */
  override def onCreateOptionsMenu(menu : Menu) : Boolean = {
    getMenuInflater.inflate(R.menu.trace_list, menu)
    true
  }
  
  /**
   * This method is called if the user selected a menuItem from the option-menu.
   * @param item the MenuItem of the selection
   * @return true if the selection was successful
   * @see TribosListActivity
   */ 
  override def onOptionsItemSelected(item : MenuItem) : Boolean = {
    item.getItemId() match {
      case R.id.edit_btn ⇒ editTraceAction()
      case R.id.export_btn ⇒ selectExportDestinationAction()
      case R.id.delete_btn ⇒ deleteAction()
      case _ ⇒ return false
    }
    true
  }
  
  /**
   * This method is called when a sub-activity has been finished.
   * In this case - if the TraceEditActivity was closed, or the 
   * FileChooser for the export of a Trace was closed.
   * @param requestCode the original requestcode when opening the subactivity
   * @param resultCode the resultcode of the subactivity
   * @param data the data given from the subactivity
   * @see TribosListActivity
   */
  protected override def onActivityResult(
    requestCode: Int, resultCode: Int, data : Intent){
    
    if(resultCode == Activity.RESULT_OK) {
      requestCode match {
        
        case KEY_EDITTRACE ⇒ 
          loadItems()
        case KEY_CHOOSEFILE ⇒ 
          exportAction(data.getExtras.get(FileChooserComCodes.RESULT).toString)
          
        case _ ⇒ None
      }
    }
  }
}


