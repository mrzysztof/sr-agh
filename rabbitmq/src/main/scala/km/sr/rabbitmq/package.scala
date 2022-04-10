package km.sr

import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.AMQP

package object rabbitmq {
  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
  def toBytes(x: String) = x.getBytes("UTF-8")

  def printerConsumer(channel: Channel) = new DefaultConsumer(channel){
    override def handleDelivery(consumerTag: String,
                                envelope: Envelope,
                                properties: AMQP.BasicProperties,
                                body: Array[Byte]): Unit =
      println("[Received]: " + fromBytes(body))
  }

  private def initQueue(exclusive: Boolean, autoDelete: Boolean)
                       (channel: Channel, queueName: String, routingKeys: List[String]): Unit = {
    channel.queueDeclare(queueName, true, exclusive, autoDelete, null)
    routingKeys.foreach(k => channel.queueBind(queueName, defaultExchange, k))
  }

  val initPrivQueue: (Channel, String, List[String]) => Unit =
    initQueue(true, true)

  val initSharedQueue: (Channel, String, List[String]) => Unit =
    initQueue(false, false)


  val defaultExchange: String = "direct-exchange"
}
