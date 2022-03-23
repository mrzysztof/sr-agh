package km.sr.rest

object Encoding  {
  val voivodeshipNameMapping = Map(
    "B" -> "Podlaskie",
    "C" -> "Kuyavian-Pomeranian",
    "D" -> "Lower Silesian",
    "E" -> "Lodz",
    "F" -> "Lubusz",
    "G" -> "Pomeranian",
    "K" -> "Lesser Poland",
    "L" -> "Lublin",
    "N" -> "Warmian-Masurian",
    "O" -> "Opole",
    "P" -> "Greater Poland",
    "R" -> "Subcarpathian",
    "S" -> "Silesian",
    "T" -> "Holy Cross",
    "W" -> "Masovian",
    "Z" -> "West Pomeranian"
  )

  val regionIds = Map(
    "PL" -> "000000000000",
    "K" -> "011200000000",
    "S" -> "012400000000",
    "F" -> "020800000000",
    "B" -> "062000000000",
    "C" -> "040400000000",
    "D" -> "030200000000",
    "E" -> "051000000000",
    "G" -> "042200000000",
    "L" -> "060600000000",
    "N" -> "042800000000",
    "O" -> "031600000000",
    "P" -> "023000000000",
    "R" -> "061800000000",
    "T" -> "052600000000",
    "W" -> "071400000000",
    "Z" -> "023200000000"
  )
}
