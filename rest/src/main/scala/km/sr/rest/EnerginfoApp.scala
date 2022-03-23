package km.sr.rest

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.{Failure, Success}

object EnerginfoApp {

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      implicit val system = context.system
      val downloader = new Downloader()
      val unmarshaller = new ReportUnmarshaller()

      val routes = new Routes(downloader, unmarshaller)(context.system)
      startHttpServer(routes.topLevelRoute)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "EnerginfoHttpServer")
  }
}
