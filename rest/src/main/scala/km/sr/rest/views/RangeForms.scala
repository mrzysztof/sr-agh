package km.sr.rest.views

import km.sr.rest.Encoding.voivodeshipNameMapping
import scalatags.Text
import scalatags.Text.all._

object RangeForms {
  val formFrag: Text.TypedTag[String] = div(cls:="formContainer")(
    form(name:="rangeForm", id:="rangeForm", method:="get")(
      select(name:="year", id:="yearSelect", required)(
        option(disabled, selected)("Year"),
        for (year <- 2020 to 2001 by -1) yield
          option(value:=year)(year)
      ),
      select(name:="region", id:="regionSelect")(
        option(value:="PL")("Poland"),
        for ((abbr, name) <- voivodeshipNameMapping.toSeq) yield
          option(value:=abbr)(name)
      )
    ),
    script(src:="static/scripts/form.js")
  )
}