package org.tribosmap.model.app

import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import android.util.Log

/**
 * This class is used to connect to the TraceRecordService.
 * for further information
 * @see ServiceConnection
 * @author Meiko Rachimow
 */
class TraceRecordServiceConnection extends ServiceConnection {

  private[this] var recordService: ITraceRecordService = null
  
  /**
   * @return the TraceRecordService
   */
  def getRecordService: ITraceRecordService = recordService
  
  /**
   * Called when the service is connected
   * @param className name of the service-class
   * @param service
   * @see ServiceConnection
   */
  override def onServiceConnected(
    className: ComponentName, service: IBinder) {
    
    Log.i(this.getClass.toString, "Connect to ITraceRecordService")
    recordService = ITraceRecordService.Stub.asInterface(service)
  }
  
  /**
   * Called when the service is disconnected
   * @param className name of the service-class
   * @see ServiceConnection
   */   
  override def onServiceDisconnected(className: ComponentName) {
    
    Log.i(this.getClass.toString, "Disconnect from ITraceRecordService")
    recordService = null;
  }
}
