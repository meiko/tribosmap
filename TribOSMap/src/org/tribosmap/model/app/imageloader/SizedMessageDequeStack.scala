package org.tribosmap.model.app.imageloader

import scala.actors.{MessageQueue, MessageQueueElement, OutputChannel}

/**
 * This message deque can replace the MessageQueue of an actor.
 * The implementation is similar to the MessageQueue, but this class
 * is implemented as Double ended queue, and if used instead of the MessageQueue,
 * it will behave like a Stack.
 * (it is a really special thing ... and should only be used for special applications,
 * we use it for the actor in the ImageLoader )
 * It has a maximum number of elements, if the count of elements is bigger than that,
 * the oldest element is deleted.
 * 
 * @param maxSize the maximal size of the Deque
 * @author Meiko Rachimow
 * @see MessageQueue
 */
@serializable
protected[imageloader] class SizedMessageDequeStack(maxSize: Int) extends MessageQueue {
  
  
  @serializable
  class SizedMessageDequeStackElement extends MessageQueueElement{
    var before: SizedMessageDequeStackElement = null
  }
  
  implicit def messageQueueExtensions(msg : MessageQueueElement) = 
    msg.asInstanceOf[SizedMessageDequeStackElement]
  
  /** 
   * append a message
   */
  override def append(msg: Any, session: OutputChannel[Any]) = {
    
    changeSize(1) // size always increases by 1

    if (null eq last) { // list empty
      val el = new SizedMessageDequeStackElement
      el.msg = msg
      el.session = session
      first = el
      last = el
    }
    else {
      val el = new SizedMessageDequeStackElement
      el.msg = msg
      el.session = session
      el.next = first
      first.before = el
      first = el
    }
    
    if(size > maxSize) {
      changeSize(-1)
      //remove oldest element
      last = last.before
      
      if(null eq last){
        first = null
      } else {
        last.next = null
      }
    }
    
  }
  
  /** 
   * Removes the n-th msg that satisfies the predicate.
   */
  override def remove(n: Int)(p: Any ⇒ Boolean): Option[(Any, OutputChannel[Any])] = {
    
    var found: Option[(Any, OutputChannel[Any])] = None
    var pos = 0

    def test(msg: Any): Boolean =
      if (p(msg)) {
        if (pos == n)
          true
        else {
          pos += 1
          false
        }
      } else
        false

    if (last == null) None
    else if (test(first.msg)) {
      val tmp = first
      // remove first element
      first = first.next
      
      // might have to update last
      if (tmp eq last) {
        last = null
      } else {
        first.before = null
      }
      
      Some((tmp.msg, tmp.session))
    } else {
      var curr = first
      var prev = curr
      while(curr.next != null && found.isEmpty) {
        prev = curr
        curr = curr.next
        if (test(curr.msg)) {
          // remove curr
          prev.next = curr.next
          // might have to update last
          if (curr eq last) {
            last = prev
          } else {
            prev.next.before = prev
          }
          found = Some((curr.msg, curr.session))
        }
      }
      found
    }
  }

  override def extractFirst(p: Any ⇒ Boolean): MessageQueueElement = {
    
    changeSize(-1) // assume size decreases by 1

    val msg = if (null eq last) null
    else {
      // test first element
      if (p(first.msg)) {
        val tmp = first
        // remove first element
        first = first.next

        // might have to update last
        if (tmp eq last) {
          last = null
        } else {
          first.before = null
        }

        tmp
      }
      else {
        var curr = first
        var prev = curr
        while(curr.next != null) {
          prev = curr
          curr = curr.next
          if (p(curr.msg)) {
            // remove curr
            prev.next = curr.next

            // might have to update last
            if (curr eq last) {
              last = prev
            } else {
              prev.next.before = prev
            }

            return curr
          }
        }
        null
      }
    }

    if (null eq msg)
      changeSize(1) // correct wrong assumption

    msg
  }
}
