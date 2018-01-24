package com.sap.scala.demo

import akka.actor.ActorRef
import com.felstar.scalajs.leaflet._
import scala.scalajs.js

/***********************************************************************************************************************
  * Central reference for all:
  *   o UI element names used by actors
  *   o Actor names
  *   o Messages sent between actors
  */
object MessageBox {
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // UI elements referenced by the actors
  val countryListDiv  = "country_list_div"
  val countryInput    = "country_input"
  val countryDataList = "country_datalist"

  val regionListDiv  = "region_list_div"
  val regionInput    = "region_input"
  val regionDataList = "region_datalist"

  val cityListDiv  = "city_list_div"
  val cityInput    = "city_input"
  val cityDataList = "city_datalist"

  val mapDiv = "world_map"

  val weatherReportDiv   = "weather_info_div"
  val weatherReportTable = "weather_info_table"

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Actor names
  val actor_country_list   = "country_list_actor"
  val actor_region_list    = "region_list_actor"
  val actor_city_list      = "city_list_actor"
  val actor_fetch_json     = "fetch_json_actor"
  val actor_map            = "map_actor"
  val actor_weather_report = "weather_report_actor"

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Messages sent between actors
  case class         ClearList()
  case class      FetchJsonMsg(parentPid: ActorRef, url: String)
  case class        Initialise()
  case class   JsonResponseMsg(jsonData: js.Dynamic)
  case class ShowCountryMapMsg()
  case class  RepositionMapMsg(lat: String, lng: String)
}
