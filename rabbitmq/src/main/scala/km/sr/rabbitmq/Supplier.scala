package km.sr.rabbitmq

import scala.io.StdIn.readLine
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.AMQP

import scala.util.{Failure, Success, Try}

object Supplier extends App{
  val name = readLine("Enter name of the supplier:\n")
  val offer = readLine("Enter offered equipment:\n")
              .split(" ").toList
  val connection = new ConnectionFactory().newConnection()
  val channel = connection.createChannel()

  val orderConsumer = new DefaultConsumer(channel){
    override def handleDelivery(consumerTag: String,
                                envelope: Envelope,
                                properties: AMQP.BasicProperties,
                                body: Array[Byte]): Unit = {
      val receivedMsg: String = fromBytes(body)
      receivedMsg.split("-") match {
        case Array(crew, orderedItem) => {
          println(s"[Received]: Order for $orderedItem from $crew.")
          val confirmationMsg = s"Your order for $orderedItem is realized by $name."
          channel.basicPublish(defaultExchange, "crews." + crew, null, toBytes(confirmationMsg))
          println(s"[Sent]: Order confirmation for $orderedItem to $crew.")
        }
        case _ => println("[Error]: Could not parse received message.")
      }
    }

  }

  def setupSupplier(): Try[Unit] = Try {
    channel.exchangeDeclare(defaultExchange, "topic", true)
    offer.foreach(item => {
      val queueName = "items." + item
      initSharedQueue(channel, queueName, List(queueName))
      channel.basicConsume(queueName, true, orderConsumer)
    })

    val privQueueName = "suppliers." + name
    initPrivQueue(channel, privQueueName, List("suppliers", "everyone"))
    channel.basicConsume(privQueueName, true, printerConsumer(channel))
  }

  setupSupplier() match {
    case Success(_) => println(s"$name is ready for orders.")
    case Failure(exception) => println(s"Failed to setup supplier: ${exception.toString}")
  }
}
