package km.sr.rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.actor.typed.ActorSystem
import akka.util.Timeout

import scala.util.{Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import km.sr.rest.views._

import scala.concurrent.Future

class Routes(downloader: Downloader, unmarshaller: ReportUnmarshaller)(implicit val system: ActorSystem[_]) {
  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("energinfo.routes.ask-timeout"))
  val htmlType = ContentTypes.`text/html(UTF-8)`
  val frontendPrefix = "static"

  val downloadExceptionHandler: ExceptionHandler = ExceptionHandler {
      case downloadFailure: Throwable =>
        complete(HttpEntity(htmlType, DownloadFailure().render))
    }

  lazy val topLevelRoute: Route = get {
    concat(
      parameters("year", "region".withDefault("PL")) {
        (year, regionSymbol) => infoPageRoute(year, regionSymbol)
      },
      indexRoute,
      staticResourcesRoute
    )
   }

  def infoPageRoute(year: String, regionSymbol: String): Route = {
    if (!validParams(year, regionSymbol))
      complete(HttpEntity(htmlType, BadParams().render))
    else {
      handleExceptions(downloadExceptionHandler) {
        val responseFuture: Future[List[HttpResponse]] =
          downloader.getGeneralReport(year, regionSymbol)
        onSuccess(responseFuture) {
          responses =>
            if (responses.forall(_.status == StatusCodes.OK)) {
              completeWithInfoPage(year, regionSymbol, responses)
            }
            else {
              responses.foreach(resp => resp.discardEntityBytes())
              complete(HttpEntity(htmlType, DownloadFailure().render))
            }
        }
      }
    }
  }

  lazy val indexRoute: Route = pathEndOrSingleSlash {
    complete {
      HttpEntity(htmlType, Index().render)
    }
  }

  lazy val staticResourcesRoute: Route =
    path(frontendPrefix / Remaining) { resource =>
    getFromResource(resource)
  }

  def validParams(year: String, regionSymbol: String): Boolean =
    (Encoding.regionIds.contains(regionSymbol)
      && (2001 until 2021).contains(year.toInt))

  def completeWithInfoPage(year: String,
                           regionSymbol: String,
                           responses: List[HttpResponse]): Route = {
    val prodFuture = unmarshaller.getProductionReport(responses(0))
    val usageFuture = unmarshaller.getUsageReport(responses(1))
    val allTimeFuture = unmarshaller.getAllTimeReport(responses(2))
    val reports = for {
      prodRep <- prodFuture
      useRep <- usageFuture
      allTimeRep <- allTimeFuture
    } yield (prodRep, useRep, allTimeRep)

    onSuccess(reports) {
      (prodRep, usRep, atRep) => {
        if (regionSymbol == "PL")
          complete(HttpEntity(htmlType, Country(prodRep, usRep, atRep).render))
        else {
          // Voivodeship case, require additional basic country info
          val basicCountryReport = downloader.getBasicCountryReport(year)
          onSuccess(basicCountryReport) { response =>
            if (response.status == StatusCodes.OK) {
              val basicCountryFuture =
                unmarshaller.getBasicCountryReport(response)
              onSuccess(basicCountryFuture){ bcReport =>
                complete(HttpEntity(htmlType,
                                    Voivodeship(prodRep, usRep, atRep, bcReport).render))
                }
              } else {
                response.discardEntityBytes()
                complete(HttpEntity(htmlType, DownloadFailure().render))
              }
          }
        }
      }
    }
  }
}
