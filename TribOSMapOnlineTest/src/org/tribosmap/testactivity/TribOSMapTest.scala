package org.tribosmap.testactivity

import android.app.ListActivity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.{Button, TextView, ProgressBar, ListView}
import android.app.Instrumentation
import android.util.Log
import android.graphics.LightingColorFilter

import scala.actors.Actor.{actor, react, self, loop}

import org.scalatest.{Reporter, Report, Stopper}
import org.tribosmap.model.test.online._

class TribOSMapTest extends ListActivity[ReportRow]  {

  private[this] val EXIT = "EXIT"
  private[this] val START_TEST = "START_TEST"
  
  private[this] lazy val statusText = findViewById( R.id.status ).asInstanceOf[TextView]
  private[this] lazy val testCounterText = findViewById( R.id.testCounter ).asInstanceOf[TextView]
  private[this] lazy val failureCounterText = findViewById( R.id.failureCounter ).asInstanceOf[TextView]
  private[this] lazy val totalCountText = findViewById( R.id.totalCount ).asInstanceOf[TextView]
  private[this] lazy val progressBar = findViewById( R.id.progress_bar ).asInstanceOf[ProgressBar]
  private[this] lazy val launcherButton = findViewById(R.id.launch_button).asInstanceOf[Button]
  private[this] lazy val stopButton = findViewById(R.id.stop_button).asInstanceOf[Button]
  private[this] lazy val normalProgressFilter = new LightingColorFilter(0xFF00FF00, 0)
  private[this] lazy val failureProgressFilter = new LightingColorFilter(0xFFFF0000, 0)
  private[this] lazy val parentSuite = new AllFunTests()
  
  private[this] lazy val expectedTestCount = parentSuite.expectedTestCount(
    Set(), Set("org.scalatest.Ignore")) 
  
  private[this] lazy val fullProgressWidth = progressBar.getMeasuredWidth

  /**
   * convert a scala list to a java list when needed
   */
  private[this] implicit def scalaListToJavaList(xs: scala.List[ReportRow]): java.util.List[ReportRow] = {
    val l = new java.util.LinkedList[ReportRow]
    xs.foreach(x ⇒ l.add(x))
    l
  }
  
  private[this] def getListAdapter() : ReportListAdapter = {
    super.getListAdapter.asInstanceOf[ReportListAdapter]
  }
  
  private[this] def runTestActor = actor {
    
    loop { react {
      case START_TEST ⇒ 
        val reporter = new MyAndroidTestRunner(this)        
        
        parentSuite.nestedSuites.foreach(suite ⇒ {
          
          suite match {
            case s: android.test.AndroidTestCase ⇒ s.setContext( this )
            case _ ⇒
          }
          
          suite.execute(None, 
                        reporter, 
                        new Stopper {}, 
                        Set(), 
                        Set("org.scalatest.Ignore"), 
                        Map(), 
                        None)
          
        })	
      case EXIT ⇒
        self.exit()
    }} 
  }
  
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    
    launcherButton.setOnClickListener( new View.OnClickListener {
      def onClick(view: View) {
        progressBar.getProgressDrawable.setColorFilter(normalProgressFilter)
        progressBar.setMax(expectedTestCount)
        progressBar.setProgress(0)
        setListAdapter(new ReportListAdapter(
          TribOSMapTest.this, R.layout.list_entry, R.id.row_title, List()))
        runTestActor ! START_TEST
      }
    })
    
    progressBar.getProgressDrawable.setColorFilter(normalProgressFilter)
    progressBar.setMax(expectedTestCount)
    totalCountText.setText(expectedTestCount.toString)
    
    setListAdapter(new ReportListAdapter(
      this, R.layout.list_entry, R.id.row_title, List()))
  }
   

  
  private[testactivity] def makeReport(event: TestEvent, 
                 report: Report, 
                 string: String, 
                 testsCompletedCount: Int, 
                 testsFailedCount: Int) {

    runOnUiThread( new Runnable() {
      def run {
        
        val status = new StringBuffer
        
        event match {
          
          case TestEvent.START_TEST ⇒
            status.append( "Starting: " )
            
          case TestEvent.END_TEST ⇒
            status.append( "Ending: " )
            getListAdapter.add(new ReportRow(report, getResources))
            getListAdapter.notifyDataSetChanged
            
          case TestEvent.FAILURE ⇒
            status.append( "Failure: " )
            progressBar.getProgressDrawable.setColorFilter(failureProgressFilter)
            getListAdapter.add(new ReportRow(report, getResources))
            getListAdapter.notifyDataSetChanged
        }
        
        val testName = report.name.substring(
          report.name.findIndexOf(_ == ':') + 2)
        
        status.append(testName)
        statusText.setText( new String( status ) )

        Log.i("Testrun: " + testsCompletedCount, status.toString)
        
        testCounterText.setText( "Tests: "+testsCompletedCount )
        failureCounterText.setText( "Failure: "+testsFailedCount )
        progressBar.setProgress(testsCompletedCount)
        
        getListView().setSelection(getListAdapter.getCount - 1)
      }
    })
  }
  

  
  protected override def onListItemClick(
    parent:ListView, view:View, position:Int, id:Long){
    
    super.onListItemClick(parent, view, position, id)
    getListView.setSelectionFromTop(position, 0)
    
    new ReportDialog(this, getListAdapter.getItem(position).report).show
  }
}
