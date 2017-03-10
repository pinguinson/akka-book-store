import actors.{BookStore, Client, Supplier}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import messages.{RequestBook, ShowLibrary}
import spray.json._
import structures.{Book, Library}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by pinguinson on 3/10/2017.
  */


trait Protocols extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val bookRequestFormat = jsonFormat1(RequestBook)
  implicit val bookFormat = jsonFormat2(Book)
  implicit val libraryFormat = jsonFormat1(Library)
}

trait Service extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  implicit val timeout = Timeout(5.seconds)

  //TODO: process JSON requests
  val routes = {
    path("requestBook") {
      post {
        parameters('name.as[String], 'title.as[String]) { (name, title) =>
          complete {
            system.actorSelection(s"/user/$name") ! RequestBook(title)
            s"requested book $title for $name"
          }
        }
      }
    } ~ path("showLibrary") {
      get {
        parameters('name.as[String]) { name =>
          val request = (system.actorSelection(s"/user/$name") ? ShowLibrary).mapTo[Library]
          onComplete(request) {
            case Success(library) => complete(library)
            case Failure(ex) => complete(s"Something went wrong: $ex")
          }
        }
      }
    }
  }
}


object MainApp extends App with Service {
  
  override implicit val system = ActorSystem("sys")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  system.actorOf(Props[BookStore], "bookstore")
  system.actorOf(Props[Supplier], "supplier")
  system.actorOf(Props[Client], "bill")
  system.actorOf(Props[Client], "emma")

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
}
