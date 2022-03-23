package km.sr.rest.views

import scalatags.Text
import scalatags.Text.all._

case class Index() extends View {
  def bodyContents: Text.TypedTag[String] = div(cls:="root")(
    h1("Energinfo"),
    hr,
    RangeForms.formFrag
  )
}