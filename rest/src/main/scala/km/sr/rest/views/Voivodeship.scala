package km.sr.rest.views

import km.sr.rest.{AllTimeReport, BasicCountryReport, ProductionReport, UsageReport}
import scalatags.Text
import scalatags.Text.all._

case class Voivodeship(productionReport: ProductionReport,
                       usageReport: UsageReport,
                       allTimeReport: AllTimeReport,
                       basicCountryReport: BasicCountryReport) extends View{
  def bodyContents: Text.TypedTag[String] = {
    val minContrib = allTimeReport.vals.min
    val maxContrib = allTimeReport.vals.max
    div(cls:="root")(
      RangeForms.formFrag,
      hr,
      h2("Production"),
      ul(
        li(s"Total[GWh] - ${productionReport.total}"),
        s"${round( 100 * productionReport.total / basicCountryReport.totalProduction, 2)}% of polish production",
        li(s"Total from renewable resources[GWh] - ${productionReport.renewableTotal}"),
        s"${round(100 * productionReport.renewableTotal / basicCountryReport.totalRenewable, 2)}% of polish production",
        li(s"Renewable energy contribution - ${productionReport.renewableContrib}%"),
        s"Poland's average - ${basicCountryReport.renewableContrib}%"
      ),
      h2("Usage"),
      ul(
        li(s"Total[GWh] - ${usageReport.total}"),
        s"${usageReport.usageRel}% of polish usage",
        li(s"Usage per capita[kWh] - ${usageReport.usagePerCapita}"),
        s"Poland's average - ${basicCountryReport.usagePerCapita}",
        li(s"Production/usage ratio - ${round(productionReport.total / usageReport.total, 2)}")
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
