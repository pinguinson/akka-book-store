package actors

import akka.actor.{Actor, ActorLogging, ActorSelection}
import messages._
import structures.{Book, Library}

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Created by pinguinson on 3/9/2017.
  */
class BookStore extends Actor with ActorLogging {

  val supplier: ActorSelection = context.actorSelection("/user/supplier")

  var awaitingSupply = false
  var library = mutable.HashSet(
    Book("Bible", "VA"),
    Book("The Idiot", "Fyodor Dostoyevsky"),
    Book("For Whom the Bell Tolls", "Ernest Hemingway"),
    Book("To Kill a Mockingbird", "Harper Lee"),
    Book("The Martian", "Andy Weir"),
    Book("Harry Potter #1", "J.K. Rowling"),
    Book("Harry Potter #2", "J.K. Rowling"),
    Book("Harry Potter #3", "J.K. Rowling"),
    Book("Harry Potter #4", "J.K. Rowling"),
    Book("Harry Potter #5", "J.K. Rowling"),
    Book("Harry Potter #6", "J.K. Rowling"),
    Book("Harry Potter #7", "J.K. Rowling")
  )

  def lookupBook(title: String): Option[Book] = {
    val query = library.find(_.title == title)
    query match {
      case Some(book) =>
        library -= book
        ensureLibrarySize
    }
    query
  }

  def ensureLibrarySize = {
    if (library.size <= 6 && !awaitingSupply) {
      supplier ! RequestMoreBooks
      log.info(s"Library is too small, requested more books")
    }
  }

  def receive = {
    case ShowLibrary =>
      log.info("Showing library")
      sender ! Library(library.toList)
    case RequestBook(title) =>
      lookupBook(title) match {
        case Some(book) =>
          sender ! BorrowedBook(book, 20 seconds)
          log.info(s"Found $book in the library, sending it to client")
        case None =>
          sender ! BookNotFound
          log.info("Didn't find the book")
      }
    case ReturnBook(book) =>
      library += book
      log.info(s"$book was returned by the client")
    case BookSupply(books) =>
      log.info("New books, yay!")
      library ++= books
      awaitingSupply = false
  }
}
