package org.tribosmap.model.test.online.business

import org.tribosmap.model.business.service._
import org.tribosmap.model.business.repository._
import org.tribosmap.model.database._
import android.content.Context

/*
 * The base class of activities which need access to services
 * here are some convinient methods to handle with the database objects, 
 */
trait OnlineTestServiceAccess  
  extends ServiceAccess { 
  
  override lazy val daoAccessor = new SQLiteDAOAccessor(
    context, "tribosmapTest", 1)
}
