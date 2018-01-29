package com.sap.scala.demo.actors

import akka.actor.Actor
import org.scalajs.dom.Event
import org.scalajs.dom.html.{Input, DataList}
import com.sap.scala.demo._

import scala.scalajs.js

/***********************************************************************************************************************
  * Actor to handle the input field and associated datalist of cities
  */
class CityListActor extends Actor {
  private val className = "CityListActor"
  private val fnName    = "receive"

  private val traceActive = false
  private val traceMsg    = Trace.flowMsg(traceActive)(className)(fnName)(_: String)
  private val traceInfo   = Trace.flowInfo(traceActive)(className)(fnName)(_: String)

  /*********************************************************************************************************************
    * Wait for messages
    */
  def receive = {
    /*******************************************************************************************************************
      * Client event
      * The user has selected a city
      */
    case evt: Event =>
      traceMsg(s"dom.Event ${evt.`type`} detected from ${evt.target.toLocaleString}")

      // Get a reference to the region datalist and input fields
      val input = DOMUtils.elementById[Input](MessageBox.cityInput)
      val dl    = DOMUtils.elementById[DataList](MessageBox.cityDataList)

      val thisOpt = dl.options.namedItem(input.value)
      var lat     = ""
      var lng     = ""

      // Can the user's input be found in the list of cities?
      if (thisOpt != null) {
        // Yup, so fetch the weather information for the selected location
        traceInfo(s"Fetching weather information for ${StageManager.thisCity} at ($lat,$lng)")
        StageManager.thisCity = thisOpt.textContent

        lat = thisOpt.getAttribute("lat")
        lng = thisOpt.getAttribute("lng")

        StageManager.mapActor           ! MessageBox.RepositionMapMsg(lat, lng)
        StageManager.weatherReportActor ! MessageBox.ClearList
        StageManager.fetchJsonActor     ! MessageBox.FetchJsonMsg(
          StageManager.weatherReportActor, Utils.getOwmUrlForLatLng(lat, lng)
        )
      }
      // Nope, so has the city name input field been blanked out?
      else if (input.textContent.size == 0) {
        // Yup, so remove the current weather report and reset the city name
        StageManager.weatherReportActor ! MessageBox.ClearList
        StageManager.thisCity = ""

        // If this country has no regions (I.E. only a city list), then when the city name is blanked out, reset the
        // map view to the country view
        if (StageManager.thisRegion == "")
          StageManager.mapActor ! MessageBox.ShowCountryMapMsg
      }

    /*******************************************************************************************************************
      * JSON response handler for incoming list of cities
      */
    case MessageBox.JsonResponseMsg(countryInfo) =>
      traceMsg("JsonResponseMsg")

      // Is there more than one city in this region?
      if (countryInfo.cities.length.asInstanceOf[Int] > 1)
        // Yup, so build a normal city list
        DOMUtils.buildCityList(self, countryInfo.cities)
      else {
        // Nope, so there's no reason to build a city list.
        // Immediately display the weather information for the only city in this region
        val city = new City(countryInfo.cities.asInstanceOf[js.Array[js.Dynamic]].head)

        traceInfo(s"${StageManager.thisCity} is the only city in region ${StageManager.thisRegion}")

        // Reposition map on the city and request the weather information
        StageManager.thisCity = city.name
        StageManager.mapActor ! MessageBox.RepositionMapMsg(city.lat.toString, city.lng.toString)
        StageManager.fetchJsonActor  ! MessageBox.FetchJsonMsg(
          StageManager.weatherReportActor, Utils.getOwmUrlForLatLng(city.lat, city.lng)
        )
      }

    /*******************************************************************************************************************
      * Throw away the city input field, its associated datalist and the old weather report
      */
    case MessageBox.ClearList =>
      traceMsg("ClearList")

      StageManager.thisCity = ""
      StageManager.weatherReportActor ! MessageBox.ClearList

      DOMUtils.deleteChild(MessageBox.cityListDiv, MessageBox.cityInput)
      DOMUtils.deleteChild(MessageBox.cityListDiv, MessageBox.cityDataList)
      DOMUtils.hideElement(MessageBox.itemCountWarning)
  }
}
