package com.sap.scala.demo

import akka.actor.ActorRef
import com.felstar.scalajs.leaflet.LMap

/***********************************************************************************************************************
  * Global reference object
  */
object StageManager {
  // Map reference
  var mapRef: LMap = null

  // The type of browser we're running in determines how the UI behaves.
  // If we're running in Chrome, Firefox, or Edge (I.E. the actors are playing in a posh theatre), then the <datalist>
  // element is supported, and the country/region/city information will be displayed in filterable dropdown lists.
  // If however, the actors are playiong in the village hall (Safari or iOS), then the <datalist> element is not
  // supported and the user must click on an area of the map to obtain the weather report

  // We start by assuming we're playing in a posh theatre, not the village hall
  var showCountryRegionCity: Boolean = true

  // List of all the actors involved in this performance
  var countryListActor:   ActorRef = null
  var regionListActor:    ActorRef = null
  var cityListActor:      ActorRef = null
  var fetchJsonActor:     ActorRef = null
  var mapActor:           ActorRef = null
  var weatherReportActor: ActorRef = null

  // The currently selected country, region, city and lat/lng
  var thisCountry:    String = ""
  var thisCountryBox: GeoBox = null
  var thisCountryIso: String = ""
  var thisRegion:     String = ""
  var thisCity:       String = ""
  var thisLat:        String = ""
  var thisLng:        String = ""
}
