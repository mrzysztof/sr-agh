package km.sr.rest.views

import scalatags.Text
import scalatags.Text.all._

case class BadParams() extends View {
  def bodyContents: Text.TypedTag[String] = div(cls:="root")(
    h1("Oh no!"),
    p("Server did not expect such parameters."),
    p("Are you a hacker???")
  )
}
