package messages

import structures.Book

import scala.concurrent.duration.FiniteDuration

/**
  * Created by pinguinson on 3/9/2017.
  */

case class RequestBook(title: String)

case class ReturnBook(book: Book)

case class BookNotFound(title: String)

case class BorrowedBook(book: Book, returnIn: FiniteDuration)

case object RequestMoreBooks

case class BookSupply(books: List[Book])

case object ShowLibrary

case class ReturnBookNotification(book: Book)
