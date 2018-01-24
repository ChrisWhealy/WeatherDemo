package com.sap.scala.demo.actors

import akka.actor.Actor
import org.scalajs.dom.Event
import org.scalajs.dom.html.{Input, DataList}
import com.sap.scala.demo._

/***********************************************************************************************************************
  * Actor to handle the input field and associated datalist of regions within a country
  */
class RegionListActor extends Actor {
  private val className = "RegionListActor"
  private val fnName    = "receive"

  private val traceActive = false
  private val traceMsg    = Trace.flowMsg(traceActive)(className)(fnName)(_: String)
  private val traceInfo   = Trace.flowInfo(traceActive)(className)(fnName)(_: String)

  def receive = {
    /*******************************************************************************************************************
      * Client event
      * The user has typed something into the region field
      */
    case evt: Event =>
      traceMsg(s"dom.Event ${evt.`type`} detected from ${evt.target.toLocaleString}")

      val input   = DOMUtils.elementById[Input](MessageBox.regionInput)
      val dl      = DOMUtils.elementById[DataList](MessageBox.regionDataList)
      val thisOpt = dl.options.namedItem(input.value)

      // Can the user's input be found in the list of regions?
      if (thisOpt != null) {
        // Yup, so fetch the city list for this region
        val regionUrl = Utils.getUrlForRegion(StageManager.thisCountryIso, thisOpt.getAttribute("region_no"))

        StageManager.thisRegion = thisOpt.textContent
        StageManager.thisCity   = ""

        traceInfo(s"Region ${thisOpt.textContent} (${thisOpt.getAttribute("region_no")}) selected")

        // Remove any existing city list and weather report, then fetch the region information
        StageManager.cityListActor  ! MessageBox.ClearList
        StageManager.fetchJsonActor ! MessageBox.FetchJsonMsg(StageManager.cityListActor, regionUrl)
      }
      // Nope, so has the user blanked out the region field?
      else if (input.textContent.size == 0) {
        // Yup, so throw the existing city list away and reset the map view back to the country
        StageManager.cityListActor ! MessageBox.ClearList
        StageManager.mapActor      ! MessageBox.ShowCountryMapMsg
      }

    /*******************************************************************************************************************
      * JSON response handler for incoming country information
      */
    case MessageBox.JsonResponseMsg(countryInfo) =>
      traceMsg("JsonResponseMsg")

      // Build a rectangle for the selected country and tell the map actor to reposition the map
      StageManager.thisCountryBox = new GeoBox(countryInfo.geoRectangle)
      StageManager.mapActor ! MessageBox.ShowCountryMapMsg

      // Some countries are not large enough to have any regions
      // How many regions does this country have?
      if (countryInfo.regionList == null) {
        // None, so skip building the region list and forward the country information directly to the cityListActor
        StageManager.thisRegion = ""
        StageManager.cityListActor ! MessageBox.JsonResponseMsg(countryInfo)
      }
      else
        DOMUtils.buildRegionList(self, countryInfo)

    /*******************************************************************************************************************
      * Throw away the region input field and its datalist
      */
    case MessageBox.ClearList =>
      traceMsg("ClearList")

      StageManager.thisRegion = ""
      StageManager.thisCity   = ""

      DOMUtils.deleteChild(MessageBox.regionListDiv, MessageBox.regionInput)
      DOMUtils.deleteChild(MessageBox.regionListDiv, MessageBox.regionDataList)
  }
}
