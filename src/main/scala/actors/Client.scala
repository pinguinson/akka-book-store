package actors

import akka.actor.{Actor, ActorLogging, ActorSelection}
import messages._
import structures.{Book, Library}

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

/**
  * Created by pinguinson on 3/9/2017.
  */
class Client extends Actor with ActorLogging {
  var library = mutable.HashSet[Book]()

  import context.dispatcher

  val shop: ActorSelection = context.actorSelection("/user/bookstore")

  def receive = {
    case ShowLibrary =>
      sender ! Library(library.toList)
      log.info("Showing library")
    case BorrowedBook(book, time) =>
      log.info(s"Borrowed $book, will return it in $time")
      addBookToLibrary(book)
      notifySelf(book, time)
    case ReturnBookNotification(book) =>
      returnBook(book)
      log.info(s"Returning $book")
    case req@RequestBook(title) =>
      log.info(s"Requesting $title")
      shop ! req
  }

  def addBookToLibrary(book: Book): Unit =
    library += book

  def notifySelf(book: Book, time: FiniteDuration): Unit =
    context.system.scheduler.scheduleOnce(time, self, ReturnBookNotification(book))

  def returnBook(book: Book): Unit = {
    library -= book
    shop ! ReturnBook(book)
  }
}