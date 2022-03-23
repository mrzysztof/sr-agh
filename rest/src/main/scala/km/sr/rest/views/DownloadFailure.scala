package km.sr.rest.views

import scalatags.Text
import scalatags.Text.all._

case class DownloadFailure() extends View {
  def bodyContents: Text.TypedTag[String] = div(cls:="root")(
    h1("Oh no!"),
    p("Data is currently unavailable :("),
    p("Please try again later.")
  )
}
