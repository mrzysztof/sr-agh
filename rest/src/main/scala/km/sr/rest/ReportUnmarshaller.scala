package km.sr.rest

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import io.circe._
import io.circe.parser._

final case class BasicCountryReport(totalProduction: Double,
                                    totalRenewable: Double,
                                    renewableContrib: Double,
                                    usagePerCapita: Double)
final case class ProductionReport(total: Double,
                                  renewableTotal: Double,
                                  renewableContrib: Double)
final case class UsageReport(total: Double,
                             usageRel: Double,
                             usagePerCapita: Double)
final case class AllTimeReport(vals: List[Double])


class ReportUnmarshaller(implicit val system: ActorSystem[_]) {
  def entityToString(entity: ResponseEntity): Future[String] = {
    entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
  }

  def getReport[T](parseFunc: Json => T)
               (response: HttpResponse): Future[T] = {
    entityToString(response.entity)
      .map(rawJson => parse(rawJson))
      .flatMap{
        case Left(err) => Future.failed(err)
        case Right(parsedJson) =>
          Future.successful(parseFunc(parsedJson))
      }
  }

  def getRecordValue(json: Json, varIdx: Int, yearIdx: Int): Double = {
    val cursor: HCursor = json.hcursor
    val decoding = cursor.downField("results")
                         .downN(varIdx)
                         .downField("values")
                         .downN(yearIdx)
                         .downField("val")
                         .as[Double]
    decoding match {
      case Left(_) => -1.0
      case Right(value) => value
    }
  }

  def parseProductionJson(json: Json): ProductionReport = {
    val total = getRecordValue(json, 0,0)
    val renewableTotal = getRecordValue(json, 1,0)
    val renewableContrib = getRecordValue(json, 2,0)
    ProductionReport(total, renewableTotal, renewableContrib)
  }

  def parseUsageJson(json: Json): UsageReport = {
    val total = getRecordValue(json, 0,0)
    val usageRel = getRecordValue(json, 2,0)
    val usagePerCapita = getRecordValue(json, 1,0)
    UsageReport(total, usageRel, usagePerCapita)
  }

  def parseAllTimeJson(json: Json): AllTimeReport = {
    val valuesPerYear = for {
      idx <- 0 until 15
    } yield getRecordValue(json, 0, idx)
    AllTimeReport(valuesPerYear.toList)
  }

  def parseBasicCountryJson(json: Json): BasicCountryReport = {
    val totalProduction = getRecordValue(json, 0,0)
    val totalRenewable = getRecordValue(json, 1,0)
    val renewableContrib = getRecordValue(json, 2,0)
    val usagePerCapita = getRecordValue(json, 2,0)
    BasicCountryReport(totalProduction, totalRenewable, renewableContrib, usagePerCapita)
  }

  val getProductionReport: HttpResponse => Future[ProductionReport] =
    getReport[ProductionReport](parseProductionJson)

  val getUsageReport: HttpResponse => Future[UsageReport] =
    getReport[UsageReport](parseUsageJson)

  val getAllTimeReport: HttpResponse => Future[AllTimeReport] =
    getReport[AllTimeReport](parseAllTimeJson)

  val getBasicCountryReport: HttpResponse => Future[BasicCountryReport] =
    getReport[BasicCountryReport](parseBasicCountryJson)
}
