package structures

/**
  * Created by pinguinson on 3/9/2017.
  */
case class Book(title: String, author: String) {
  override def toString = title + " by " + author
}
