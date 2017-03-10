package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import messages.{BookSupply, RequestMoreBooks}
import structures.Book

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Created by pinguinson on 3/10/2017.
  */
class Supplier extends Actor with ActorLogging {
  import context.dispatcher
  var library = mutable.HashSet(
    Book("Harry Potter #8", "J.K. Rowling"),
    Book("Harry Potter #9", "J.K. Rowling"),
    Book("Harry Potter #10", "J.K. Rowling"),
    Book("Harry Potter #11", "J.K. Rowling"),
    Book("Harry Potter #12", "J.K. Rowling"),
    Book("Harry Potter #13", "J.K. Rowling"),
    Book("Harry Potter #14", "J.K. Rowling"),
    Book("Harry Potter #15", "J.K. Rowling"),
    Book("Harry Potter #16", "J.K. Rowling"),
    Book("Harry Potter #17", "J.K. Rowling")
  )

  def getFiveBooks: List[Book] = {
    log.info("Sending 5 books to the shop")
    val books = library.toList.take(5)
    library --= books
    books
  }

  def scheduleDelivery(requester: ActorRef): Cancellable =
    context.system.scheduler.scheduleOnce(30 seconds, requester, BookSupply(getFiveBooks))

  def receive = {
    case RequestMoreBooks =>
      val requester = sender
      scheduleDelivery(requester)
      log.info(s"Received delivery request from the shop")
  }

}
