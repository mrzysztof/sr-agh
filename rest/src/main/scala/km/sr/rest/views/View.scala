package km.sr.rest.views
import scalatags.Text
import scalatags.Text.all._
import scalatags.Text.tags2.title

import scala.math.pow

trait View {
  val minYear = 2005

  def bodyContents: Text.TypedTag[String]

  def round(value: Double, n: Int)=
    (value * pow(10, n)).round / pow(10, n)

  val render =
    "<!DOCTYPE html>" + html(lang:="en")(
      head(
        meta(charset:="UTF-8"),
        meta(name:="viewport", content:="width=device-width, initial-scale=1.0"),
        title("Energinfo")
      ),
      body(bodyContents)
    )
}
