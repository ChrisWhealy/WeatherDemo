package com.sap.scala.demo.actors

import akka.actor.Actor
import org.scalajs.dom.html.Div
import com.sap.scala.demo._

import scala.scalajs.js

/***********************************************************************************************************************
  * Actor to handle building weather reports
  */
class WeatherReportActor extends Actor {
  private val className = "WeatherReportActor"
  private val fnName    = "receive"

  private val traceActive = false
  private val traceMsg    = Trace.flowMsg(traceActive)(className)(fnName)(_: String)

  /*********************************************************************************************************************
    * Wait for messages
    */
  def receive = {
    /*******************************************************************************************************************
      * JSON response handler for incoming weather data
      */
    case MessageBox.JsonResponseMsg(jsonResponse) =>
      traceMsg("JsonResponseMsg")
      DOMUtils.buildWeatherReport(new WeatherReportBuilder(jsonResponse))

    /*******************************************************************************************************************
      * Throw away the existing weather report
      */
    case MessageBox.ClearList =>
      traceMsg("ClearList")
      DOMUtils.hideElement[Div](MessageBox.weatherReportDiv)
      DOMUtils.deleteChild(MessageBox.weatherReportDiv, MessageBox.weatherReportTable)
  }
}
