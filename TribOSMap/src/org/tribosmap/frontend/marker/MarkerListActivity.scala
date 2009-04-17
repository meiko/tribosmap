package org.tribosmap.frontend.marker

import java.io.File
import java.lang.Runnable

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.app.Activity
import android.util.Log
import android.view.MenuItem
import android.os.Handler
import scala.actors.Actor.actor

import org.tribosmap.model.business.domain.Marker
import org.tribosmap.frontend.common.list.TribosListActivity;
import org.tribosmap.frontend.common.{ActivityHelper, Messager};
import org.tribosmap.model.ServiceAccess

/**
 * This Activity holds a list of all Markers.
 * These Marker objects were fetched from the repository.
 * The user can delete, export, or edit them here.
 * @author Meiko Rachimow
 */
class MarkerListActivity extends TribosListActivity[MarkerRow] 
  with ActivityHelper  with ServiceAccess  {
  
  /**
   * a key used when communicate with the MarkerEditActivity
   */
  private[this] val KEY_EDITMARKER = 33
    
  ////////////////////////////////////////////////////////
  //Actions
  ////////////////////////////////////////////////////////
  
  /**
   * Called when the user has pressed the delete button in the menu.
   * It will show a dialog, in which the user can delete the selected Marker.
   */
  private[this] def deleteAction() {
    
    val markerRow = getSelectedObject
    
    /**
     * delete a Marker from the repository
     */
    def deleteMarker() = tryCatch {
      markerRow.marker.delete
      getListAdapter.remove(markerRow)
      Messager.makeMessageShort(this, getString(R.string.marker_deleted))
    }
    
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.dialog_question))
      .setMessage(getString(R.string.dialog_question_delete) + 
                    markerRow.title + "?")
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), () ⇒ { deleteMarker() })
      .setNegativeButton(getString(R.string.dialog_no), () ⇒ {})
      .show()
  }
  
  /**
   * Called when the user has pressed the export button in the menu.
   * TODO: implementation not finished (see also MarkerService)
   */
  private[this] def exportAction() = tryCatch {
    assume(getFilesDir.canWrite, 
           getString(R.string.cannot_open_file_write) + getFilesDir)
    Messager.makeMessageShort(this, getString(R.string.marker_exporting))
    val marker = getSelectedObject.marker
    marker.export(getFilesDir)
  }
  
  /**
   * Called when the user has pressed the edit button in the menu.
   * It will open the MarkerEditActivity to edit the selected Marker.
   */
  private[this] def editAction() {
    Messager.makeMessageShort(this, getString(R.string.window_open))
    val marker = getSelectedObject.marker
    val bundle = new Bundle()
    bundle.putSerializable(MarkerComCodes.MARKER_PARAMETER_ID, marker)
    val intent = new Intent(this, classOf[MarkerEditActivity])
    intent.putExtras(bundle)
    startActivityForResult(intent, KEY_EDITMARKER)
    
  }
  
  /**
   * This method reloads the Marker at the last selected position
   * from the database, and refreshes the row in the listview.
   */
  private[this] def reloadAtLastSelectedPosition() {
    val lastSelectedMarkerRow = getListAdapter.getItem(lastChoosedRowIndex)
    getListAdapter.remove(lastSelectedMarkerRow)
    getListAdapter.insert(
      new MarkerRow(lastSelectedMarkerRow.marker.reload(), getResources), 
      lastChoosedRowIndex)
    getListAdapter().notifyDataSetChanged()
  }
  
  ////////////////////////////////////////////////////////
  // Overriden Methods
  ////////////////////////////////////////////////////////
  
  /**
   * @return the menuId for the map list
   * @see TribosListActivity
   */
  protected override def menuId = R.menu.marker_list
  
  /**
   * @return the id of the menu group
   * @see TribosListActivity
   */
  protected override def menuItemGroupId = R.id.menu_group
  
  /**
   * This method will load items (all Markers) into the listview of this activity.
   * The listadapter will be notified about this items.
   * The Markers are fetched from the markerService, wich loads them from the repository.
   */
  protected override def loadItems() {
    val handler = new Handler()
    val msg = Messager.makeMessage(this, getString(R.string.marker_loading))
    msg.show()
    actor {
      markerService.foreachMarker(marker ⇒ {
        val markerRow = new MarkerRow(marker, getResources)
        handler.post(new Runnable { def run() {
          getListAdapter.add(markerRow)
          msg.cancel()
        }})
        Thread.sleep(50)
      })
      handler.post(new Runnable { def run() { msg.cancel() }})
    }
  }

  
  /**
   * This method is called when a sub-activity has been finished.
   * In this case - if the MarkerEditActivity was closed.
   * @param requestCode the original requestcode when opening the subactivity
   * @param resultCode the resultcode of the subactivity
   * @param data the data given from the subactivity
   * @see TribosListActivity
   */
  protected override def onActivityResult(
    requestCode: Int, resultCode: Int, data : Intent){
    
    requestCode match {
      case KEY_EDITMARKER ⇒ 
        if(resultCode == Activity.RESULT_OK) {
          reloadAtLastSelectedPosition() 
        }
      case _ ⇒ None
    }
  }
  
  /**
   * This method is called if the user selected a menuItem from the option-menu.
   * @param item the MenuItem of the selection
   * @return true if the selection was successful
   * @see TribosListActivity
   */ 
  override def onOptionsItemSelected(item : MenuItem) : Boolean = {
    
    item.getItemId() match {
      case R.id.edit_btn ⇒ editAction()
      case R.id.export_btn ⇒ exportAction()
      case R.id.delete_btn ⇒ deleteAction()
      case _ ⇒ false
    }
    true
  }
}

