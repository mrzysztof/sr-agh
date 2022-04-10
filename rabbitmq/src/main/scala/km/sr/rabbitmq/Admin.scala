package km.sr.rabbitmq

import com.newmotion.akka.rabbitmq._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

object Admin extends App{
  val name = "admin"
  val connection = new ConnectionFactory().newConnection()
  val channel = connection.createChannel()

  def setupAdmin(): Try[Unit]  = Try {
    channel.exchangeDeclare(defaultExchange, "direct", true)
    initPrivQueue(channel, name, List("admin"))
    channel.basicConsume(name, true, printerConsumer(channel))
  }

  def work(): Unit = {
    val message = readLine("Enter your message:\n")
    def send(receivers: String): Unit = {
      channel.basicPublish(defaultExchange, receivers, null, toBytes("[Admin] " + message))
      println(s"[Sent]: Successfully sent to $receivers.")
    }

    val mode = readLine("Send to suppliers[s], crews[c] or both[b]. Cancel[x]\n")
    mode.toLowerCase match {
      case "s" => send("suppliers")
      case "c" => send("crews")
      case "b" => send("everyone")
      case _ => work()
    }
    work()
  }

  setupAdmin() match {
    case Success(_) => Future(work())
    case Failure(exception) => println(s"Failed to setup admin: ${exception.toString}")
  }
}
