package com.sap.scala.demo

import akka.actor.ActorRef
import com.felstar.scalajs.leaflet
import com.felstar.scalajs.leaflet.L
import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

/***********************************************************************************************************************
  * General purpose utilities
  */

object Utils {
  /*********************************************************************************************************************
    * Private API
    */

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // OpenWeather endpoint details
  private def openWeatherMapHost = "openweathermap.org"
  private def openWeatherMapAPI  = "https://api." + openWeatherMapHost
  private def openWeatherMapImg  = "https://" + openWeatherMapHost

  private def weatherEndpoint    = openWeatherMapAPI + "/data/2.5/weather"
  private def imageEndpoint      = openWeatherMapImg + "/img/w/"

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // OpenWeatherMap query string parameters
  private var owmQueryParams = scala.collection.mutable.Map[String,String](
    "lat"    -> ""
   ,"lon"    -> ""
   ,"mode"   -> "json"
//   ,"apikey" -> "<Paste your API Key value here>"
   ,"apikey" -> "9ff16c79edd6ad12396c22ed8a7996ec"
  )

  private var mbQueryParams = scala.collection.mutable.Map[String,String](
    "access_token" -> "pk.eyJ1IjoiZmFuY2VsbHUiLCJhIjoiY2oxMHRzZm5zMDAyMDMycndyaTZyYnp6NSJ9.AJ3owakJtFAJaaRuYB7Ukw"
  )

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // ASCII values of various characters
  private def char_0  = 48
  private def char_9  = 57
  private def small_a = 97
  private def small_f = 102

  private def compassPoints = Array(
    "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
    "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
  )

  /*********************************************************************************************************************
    * Public API
    */

  // Has the user updated the source code with their own OpenWeatherMap API Key?
  // If not, this app cannot obtain weather forecast information
  def owmApiKeyInstalled(): Boolean = isHexStr(owmQueryParams.get("apikey").get)

  // Builds and opens an XHR request to a given endpoint
  def buildXhrRequest(targetEndpoint: String): dom.XMLHttpRequest = {
    val xhr = new dom.XMLHttpRequest
    xhr.open("GET", targetEndpoint)
    xhr
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // JSON response handler sends whatever response it receives as a message to some actor
  val jsonResponseHandler = (parentPid: ActorRef, xhrResponse: dom.XMLHttpRequest) =>
    (e: dom.Event) => parentPid ! MessageBox.JsonResponseMsg(js.JSON.parse(xhrResponse.responseText))

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Inclusive "between" value check
  def between(a: Int, b: Int) = (x: Int) => x >= a && x <= b

  def isHexChar = between(small_a, small_f)
  def isNumChar = between(char_0, char_9)

  // Check if a character string is a valid hex number
  def isHexStr(s: String): Boolean = s.toLowerCase.foldLeft(true) {
    (acc, c) => acc && (isHexChar(c) || isNumChar(c))
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Various string formatting functions
  def textListFirstItem(s: String): String = if (s.size > 0) s else ""
  def textListItem(s: String):      String = if (s.size > 0) s", $s" else ""

  // All temperatures are returned in Kelvin
  def kelvinToDegStr(k: Double, min: Double, max: Double):String = {
    val variation = (max - min) / 2
    (k - 272.15).toInt + "˚C" + (if (variation > 0) s" ±${variation}˚C" else "")
  }

  // Format the lat/lon coordinates into a text string
  def formatCoords(lat: Double, lon: Double): String = {
    val latStr = s"${Math.abs(lat)}˚${if (lat >= 0) "N" else "S"}"
    val lonStr = s"${Math.abs(lon)}˚${if (lon >= 0) "E" else "W"}"

    s"$latStr, $lonStr"
  }

  // The weather conditions text string is all lowercase.
  // Convert it to sentence case
  def formatDescription(d: String): String = {
    val (head, tail) = d.splitAt(1)
    head.toUpperCase + tail
  }

  // Convert the wind direction heading in degrees to the nearest compass point
  def formatHeading(h: Double): String = {
    val upper = Math.floor((h + 12.25) / 22.5).toInt % 16
    val lower = Math.floor((h - 12.25) / 22.5).toInt % 16

    h + s"˚ (${compassPoints(Math.max(upper,lower))})"
  }

  // Add the appropriate unit string after various values
  def formatVisibility(v: Int): String    = v + "m"
  def formatVelocity(v: Double): String   = v + "m/s"
  def formatPercentage(p: Double): String = p + "%"
  def formatPressure(p: Double): String   = p + " mBar"

  def parseDouble(s: String): Option[Double] = Try { s.toDouble }.toOption

  def zeropad(s: String, l: Int): String = "000000000" + s takeRight l

  def anyOf(s: String, vals: Seq[String]): Boolean = vals.contains(s)

  // Words in place names that should not be capitalised
  def dontCapitalise = Seq("and","the","of","da","de","do","los")

  // Own version of String.capitalize that omits articles and conjunctions
  def capitalise(s: String): String  = if (anyOf(s, dontCapitalise)) s else s.capitalize
  def toTitleCase(s: String): String = s.toLowerCase.split(" ").map(capitalise).mkString(" ")

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Return OpenWeatherMap URL for a given city
  // OpenWeatherMap knows only about a limited number of city names; therefore weather information must be retrieved via
  // lat/lng coordinates rather than city name
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def getOwmImgUrl(iconName: String): String = s"$imageEndpoint$iconName.png"

  def getOwmUrlForLatLng(lat: Double, lng: Double): String = getOwmUrlForLatLng(lat.toString, lng.toString)
  def getOwmUrlForLatLng(lat: String, lng: String): String = {
    owmQueryParams += ("lat" -> lat)
    owmQueryParams += ("lon" -> lng)

    val queryStr = (
      for (p <- owmQueryParams.keys)
        yield s"$p=${owmQueryParams.get(p).get}"
      ).mkString("?", "&", "")

    weatherEndpoint + queryStr
  }

  def getUrlForCountryIso(iso2: String):        String = s"./country_data/${iso2}/${iso2}.json"
  def getUrlForRegion(iso2: String, r: String): String = s"./country_data/${iso2}/region_$r.json"
}

/***********************************************************************************************************************
  *  Classes for handling geographic information
  */
class GeoBox(geoBox: js.Dynamic) {
  val north = geoBox.North.asInstanceOf[Double]
  val south = geoBox.South.asInstanceOf[Double]
  val east  = geoBox.East.asInstanceOf[Double]
  val west  = geoBox.West.asInstanceOf[Double]

  val centre = (north - (north - south)/2, east - (east - west)/2)
  val center = centre

  val bounds = L.latLngBounds(L.latLng(north, east), L.latLng(south, west))
}

class City(cityInfo: js.Dynamic) {
  val name = cityInfo.cityName.asInstanceOf[String].replaceAll("^\"|\"$", "")
  val lat  = cityInfo.lat.asInstanceOf[Double]
  val lng  = cityInfo.lng.asInstanceOf[Double]
}

