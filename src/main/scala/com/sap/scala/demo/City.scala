package com.sap.scala.demo

import scala.scalajs.js

class City(cityInfo: js.Dynamic) {
  val name = cityInfo.cityName.asInstanceOf[String].replaceAll("^\"|\"$", "")
  val lat  = cityInfo.lat.asInstanceOf[Double]
  val lng  = cityInfo.lng.asInstanceOf[Double]
}
