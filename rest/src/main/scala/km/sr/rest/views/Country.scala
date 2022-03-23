package km.sr.rest.views

import scalatags.Text
import scalatags.Text.all._
import km.sr.rest.{AllTimeReport, ProductionReport, UsageReport}

case class Country(productionReport: ProductionReport,
                   usageReport: UsageReport,
                   allTimeReport: AllTimeReport) extends View{

  def bodyContents: Text.TypedTag[String] = {
    val minContrib = allTimeReport.vals.min
    val maxContrib = allTimeReport.vals.max
    div(cls:="root")(
      RangeForms.formFrag,
      hr,
      h2("Production"),
      ul(
        li(s"Total[GWh] - ${productionReport.total}"),
        li(s"Total from renewable resources[GWh] - ${productionReport.renewableTotal}"),
        li(s"Renewable energy contribution - ${productionReport.renewableContrib}%"),
      ),
      h2("Usage"),
      ul(
        li(s"Total[GWh] - ${usageReport.total}"),
        li(s"Usage per capita[kWh] - ${usageReport.usagePerCapita}"),
      ),
      hr,
      h2("Statistics"),
      "Renewable energy contribution over the years",
      ul(
        li(s"Mean - ${round(allTimeReport.vals.sum / allTimeReport.vals.length, 2)}%"),
        li(s"Minimum - ${minContrib}% (${minYear + allTimeReport.vals.indexOf(minContrib)})"),
        li(s"Maximum - ${maxContrib}% (${minYear + allTimeReport.vals.indexOf(maxContrib)})")
      )
    )
  }
}
