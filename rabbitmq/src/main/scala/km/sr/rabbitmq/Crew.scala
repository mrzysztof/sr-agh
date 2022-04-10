package km.sr.rabbitmq

import com.newmotion.akka.rabbitmq._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

object Crew extends App {
  val name = readLine("Enter name of the crew:\n")
  val connection = new ConnectionFactory().newConnection()
  val channel = connection.createChannel()

  def setupCrew(): Try[Unit]  = Try {
    val queueName = "crews." + name
    channel.exchangeDeclare(defaultExchange, "direct", true)
    initPrivQueue(channel, queueName, List(queueName, "crews", "everyone"))
    channel.basicConsume(queueName, true, printerConsumer(channel))
  }

  @tailrec
  def work(): Unit = {
    val order = readLine("Enter your order:\n")
    val message = name + "-" + order
    channel.basicPublish(defaultExchange, order, null, toBytes(message))
    channel.basicPublish(defaultExchange, "admin", null, toBytes(message))
    println("[Sent]: " + "Completed order for " + order)
    work()
  }

  setupCrew() match {
    case Success(_) => Future(work())
    case Failure(exception) => println(s"Failed to setup crew: ${exception.toString}")
  }
}
