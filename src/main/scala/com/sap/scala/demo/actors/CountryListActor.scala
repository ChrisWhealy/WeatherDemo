package com.sap.scala.demo.actors

import akka.actor.{Actor, ActorRef}
import org.scalajs.dom.Event
import org.scalajs.dom.html.{DataList, Input}
import com.sap.scala.demo._

/***********************************************************************************************************************
  * Actor to handle the input field and associated datalist of countries
  */
class CountryListActor extends Actor {
  private val className = "CountryListActor"
  private val fnName    = "receive"

  private val traceActive = false
  private val traceMsg    = Trace.flowMsg(traceActive)(className)(fnName)(_: String)

  /*********************************************************************************************************************
    * Wait for messages
    */
  def receive = {
    /*******************************************************************************************************************
      * Start message.
      *
      * This message is received from the top-level Weather object when it is happy that:
      * a) We are not running in Safari
      * b) The OpenWeatherMap API Key has been added to the source code
      */
    case MessageBox.Initialise =>
      traceMsg("Initialise")
      DOMUtils.buildCountryList(self)

    /*******************************************************************************************************************
      * Client event
      * The user has typed something into the country input field
      */
    case evt: Event =>
      traceMsg(s"dom.Event ${evt.`type`} detected from ${evt.target.toLocaleString}")

      // Get references to the country name input field and datalist elements
      val input = DOMUtils.elementById[Input](MessageBox.countryInput)
      val dl    = DOMUtils.elementById[DataList](MessageBox.countryDataList)

      val thisOpt = dl.options.namedItem(Utils.toTitleCase(input.value))

      // Can the user's input be found in the list of countries?
      if (thisOpt != null) {
        // Yup, so remember the selected country information
        StageManager.thisCountry    = thisOpt.id
        StageManager.thisCountryIso = thisOpt.getAttribute("iso")
        StageManager.thisRegion     = ""
        StageManager.thisCity       = ""

        val countryUrl = Utils.getUrlForCountryIso(StageManager.thisCountryIso)

        // Fetch country info and remove any existing region or city lists
        StageManager.fetchJsonActor  ! MessageBox.FetchJsonMsg(StageManager.regionListActor, countryUrl)
        StageManager.regionListActor ! MessageBox.ClearList
        StageManager.cityListActor   ! MessageBox.ClearList
      }
      // Nope, so did the user blank out the country input field?
      else if (input.value.size == 0) {
        // Yup, so clear the region and city lists, and reset the map back to the world view
        StageManager.regionListActor    ! MessageBox.ClearList
        StageManager.cityListActor      ! MessageBox.ClearList
        StageManager.weatherReportActor ! MessageBox.ClearList
        StageManager.mapActor           ! MessageBox.RepositionMapMsg("0.0", "0.0")
      }
  }
}
