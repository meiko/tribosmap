package org.tribosmap.model.app

import org.apache.http.HttpVersion
import org.apache.http.conn.params.ConnManagerParams
import org.apache.http.conn.scheme.{SchemeRegistry, Scheme, PlainSocketFactory}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.{BasicHttpParams, HttpProtocolParams}
import org.apache.http.client.HttpClient

/**
 * The central object to load data concurrent from the internet.
 * <p>
 * It uses the apache http client implementation,
 * for best the performance when concurrent loading.
 * It generates DefaultHttpClient objects, which can be used
 * to access the internet.
 * @author Meiko Rachimow
 */
object HttpAccess {
  
  /**
   * standard parameters for a http connection
   */
  private val httpParams = new BasicHttpParams()
  
  /**
   * a thread safe http connection manager
   * will administrate the client connections...
   * @see ThreadSafeClientConnManager
   */
  private var conManger = {
    ConnManagerParams.setMaxTotalConnections(httpParams, 100)
    HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1)
    val schemeRegistry = new SchemeRegistry()
    schemeRegistry.register(
      new Scheme("http", PlainSocketFactory.getSocketFactory(), 80))
    new ThreadSafeClientConnManager(httpParams, schemeRegistry)
  }
  
  /**
   * the Client Http object to access the internet
   */
  def httpClient = new DefaultHttpClient(conManger, httpParams)

  /*
  
  //not neccessary
  private def start() {
    stop()
    conManger = startConnectionmanager
    //httpClient = new DefaultHttpClient(conManger, httpParams)
  }
  
  //not neccessary
  def stop() {
    conManger.shutdown
  }
  */
}
