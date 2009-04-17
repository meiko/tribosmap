package org.tribosmap.model.app.imageloader

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler

import scala.actors.{Actor, MessageQueue, Exit}
import scala.actors.Actor.{loop, actor, react, self}
import android.util.Log

/**
 * The central object to load images from the internet or from a file.
 * It consists of a set of loader-actors, which will work concurrent,
 * and of a central Actor, which distribute the jobs to the loaders.
 * <p>
 * The ImageLoader will execute the ImageLoadingJobs.
 * After a work is finished, a Handler-object will use the callback
 * of the ImageLoadingJob, to notify the GUI.
 * The central actor (<code>jobCenter</code>) will deliver new jobs to
 * the loaders only if they are ready, which means they have no jobs in their
 * mailinglist. The jobCenter has a maximum number of jobs in memory.
 * If a new Job arrives, and the jobCenter has more than the maximum number
 * of jobs, the oldest job will be deleted.
 * </p>
 * @author Meiko Rachimow
 */
object ImageLoader {

  /**
   * the maximum number of jobs in the jobCenter actor queue
   */
  private val maxJobsOnStack = 9

  /**
   * the count of loaders (worker actors)
   */
  private val loadersCount = 3

  /**
   * an empty image - the result for failed loadings
   */
  private val noImageDrawable = new ShapeDrawable(new RectShape)

  /**
   * the Handler to notify the gui
   */
  private val handler = new Handler

  /**
   * the loaders are loading the images
   */
  private val loaders: Seq[Actor] = for( i ← 0 until loadersCount) yield actor {
    
    loop { react {
      
      case job: ImageLoadingJob ⇒  
        try {
              
          val bitmapFirstTry = job.work()
          val bitmap = if(bitmapFirstTry == null) job.work() 
                       else bitmapFirstTry
          
          assert(bitmap != null, "error loading bitmap")
              
          handler.post( new Runnable() { 
            override def run() = job.callBack(bitmap)
          })
              
        } catch {
          case error ⇒ 
            Log.e("JobLoader", "error loading image", error)
        }
        
      case Exit ⇒ 
        self.exit
        Actor.clearSelf
        
    }}
  }

  /**
   * the central actor which distributes the jobs to the loaders
   */
  private val jobCenter = { 
    
    val actorTmp = new Actor {
    
      //the mailbox (a special queue with a maximum size, 
      //the oldest will be deleted if the size is too big)
      override protected val mailbox: MessageQueue = 
        new SizedMessageDequeStack(maxJobsOnStack)
    
      //find idle loaders
      private[this] def idleLoader: Actor = 
        loaders.find(_.mailboxSize == 0).getOrElse({
          Thread.sleep(50)
          idleLoader
      })
    
      def act { loop { react { 
        
        case job: ImageLoadingJob ⇒ idleLoader.forward(job)
        
        case Exit ⇒ 
          self.exit
          Actor.clearSelf
          
      }}}
    
    }
    
    actorTmp.start
    
    actorTmp
  }

  /**
   * This method has to be called to load an image.
   * 
   * @param job the ImageLoadingJob
   */
  def loadImage(job: ImageLoadingJob) = jobCenter ! job

}
