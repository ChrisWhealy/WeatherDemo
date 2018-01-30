package com.sap.scala.demo

import akka.actor.{ActorSystem, Props}
import com.sap.scala.demo.actors._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/***********************************************************************************************************************
  * Top level object
  */
@JSExportTopLevel("Weather")
object Weather {
  private val objName     = "Weather"
  private val traceActive = false
  private val trace       = Trace.flow(traceActive)(objName)(_: String)(_: scala.Option[Boolean])
  private val traceInfo   = Trace.flowInfo(traceActive)(objName)(_: String)(_: String)

  private val enter = scala.Option(true)
  private val exit  = scala.Option(false)

  private val system = ActorSystem("weather-ui")

  /*********************************************************************************************************************
    * Entry point
    * Invoked from JavaScript in index.html
    */
  @JSExport
  def main(browserName: String, owmApiKey: String): Unit = {
    val fnName = "main"
    trace(fnName, enter)

    // The Open Weather Map API key must be supplied as the 2nd parameter to Weather.main() from the HTML page
    Utils.setOwmApiKey(owmApiKey)

    try   { dramatisPersonae(browserName) }
    catch { case th: Throwable => th.printStackTrace() }

    trace(fnName, exit)
  }

  /*********************************************************************************************************************
    * What actors will make up tonight's show?
    */
  def dramatisPersonae(browserName: String): Unit = {
    val fnName = "dramatisPersonae"

    trace(fnName, enter)
    traceInfo(fnName,s"Running in ${browserName}")

    // Is tonight's performance running is a posh theatre, or the village hall?
    // I.E. Are we running in something other than Safari?
    StageManager.showCountryRegionCity = (browserName != "safari")

    // Has the user updated the source code with their own OpenWeatherMap API Key?
    if (Utils.owmApiKeyInstalled) {
      traceInfo(fnName, s"API Key test passed: ${Utils.getOwmApiKey}")

      // Yup, so create actors for handling map events, JSON requests and building weather reports
      StageManager.mapActor           = system.actorOf(Props(new MapActor()),           MessageBox.actor_map)
      StageManager.fetchJsonActor     = system.actorOf(Props(new FetchJsonActor()),     MessageBox.actor_fetch_json)
      StageManager.weatherReportActor = system.actorOf(Props(new WeatherReportActor()), MessageBox.actor_weather_report)

      // Tell the map actor to create the world map
      StageManager.mapActor ! MessageBox.Initialise

      // Does the browser support the HTML element <datalist>?
      if (StageManager.showCountryRegionCity) {
        // Yup, so we're in a posh theatre this evening...
        // Create the country/region/city actors and store their references in the stage manager
        StageManager.countryListActor = system.actorOf(Props(new CountryListActor()), MessageBox.actor_country_list)
        StageManager.regionListActor  = system.actorOf(Props(new RegionListActor()),  MessageBox.actor_region_list)
        StageManager.cityListActor    = system.actorOf(Props(new CityListActor()),    MessageBox.actor_city_list)

        // Act 1, scene 1, enter the country list actor...
        StageManager.countryListActor ! MessageBox.Initialise
      }
      else {
        // Rats, we're in the village hall this evening...
        // Safari doesn't support the HTML element <datalist>, therefore a weather report con only be obtained by
        // clicking on a map location
        DOMUtils.safariMsg
      }
    }
    else {
      // Nope, the Open Weather Map API key is missing, so this app will remain non-functional
      traceInfo(fnName, s"API Key test failed: ${Utils.getOwmApiKey}")
      DOMUtils.apiKeyMissing
    }

    trace(fnName, exit)
  }
}



