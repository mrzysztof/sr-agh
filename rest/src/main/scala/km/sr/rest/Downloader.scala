package km.sr.rest

import akka.actor.typed.{ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Downloader(implicit val system: ActorSystem[_]){
  val queryBase = "https://bdl.stat.gov.pl/api/v1/data/by-unit/"
  val format = "format=json"
  val prodParams = "var-id=7883&var-id=194886&var-id=288086"
  val usageParams = "var-id=8863&var-id=519852&var-id=474248"
  val basicCountryParams = "var-id=7883&var-id=194886&var-id=288086&var-id=474248"

  def makeQuery(varArray: String)
               (year: String, regionSymbol: String): String = {
    val regionId = Encoding.regionIds(regionSymbol)
    queryBase + regionId + "?" + format + "&" + varArray + "&year=" + year
  }

  val makeProdQuery: (String, String) => String =
    makeQuery(prodParams)

  val makeUsageQuery: (String, String) => String =
    makeQuery(usageParams)

  val makeBasicQuery: (String, String) => String =
    makeQuery(basicCountryParams)

  def makeAllTimeQuery(regionSymbol: String): String = {
    val regionId: String = Encoding.regionIds(regionSymbol)
    val allYearsArray: String = (for {
      y <- 2001 to 2020
    } yield s"&year=${y}").mkString
    queryBase + regionId + "?format=json&var-id=288086" + allYearsArray
  }

  def getGeneralReport(year: String, regionSymbol: String):
  Future[List[HttpResponse]] = {
    val prodFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = makeProdQuery(year, regionSymbol)))
    val usageFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = makeUsageQuery(year, regionSymbol)))
    val allTimeFuture =
      Http().singleRequest(HttpRequest(uri = makeAllTimeQuery(regionSymbol)))

      for {
        prodResponse <- prodFuture
        usageResponse <- usageFuture
        allTimeResponse <- allTimeFuture
      } yield List(prodResponse, usageResponse, allTimeResponse)
    }

  def getBasicCountryReport(year: String): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = makeBasicQuery(year, "PL")))
}
