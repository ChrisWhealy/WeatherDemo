package com.sap.scala.demo

import akka.actor.ActorRef
import org.scalajs.dom.html.{DataList, Div, Image, Input, Option, Paragraph, Select, Table, TableCaption, TableDataCell, TableRow}
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, Node, document}

import scala.collection.mutable.{ListBuffer, Map}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/***********************************************************************************************************************
  * CountryList is a pre-existing JavaScript array created by an HTML <script> element in index.html that retrieves
  * countryList.js
  *
  * The Scala object name must be the same as the JavaScript object name
  */
@js.native
@JSGlobal
object CountryList extends js.Array[js.Array[js.Dynamic]]


/***********************************************************************************************************************
  * Utilities for accessing and manipulating the DOM
  */
object DOMUtils {
  private val objName = "DOMUtils"

  private val traceActive = false
  private val trace       = Trace.flow(traceActive)(objName)(_: String)(_: scala.Option[Boolean])
  private val traceInfo   = Trace.flowInfo(traceActive)(objName)(_: String)(_: String)

  private val enter = scala.Option(true)
  private val exit  = scala.Option(false)

  /*********************************************************************************************************************
    * Access DOM elements
    */
  def elementById[A <: js.Any](id: String): A = document.getElementById(id).asInstanceOf[A]

  /*********************************************************************************************************************
    * Hide/Reveal DOM elements
    */
  def hideElement[A <: HTMLElement](elName: String): Unit = elementById[A](elName).className = "hidden"
  def showElement[A <: HTMLElement](elName: String): Unit = elementById[A](elName).className = "visible"

  /*********************************************************************************************************************
    * Delete DOM elements
    */
  def deleteChild(parentId: String, childId: String): Unit = {
    traceInfo("deleteChild",s"Deleting $childId from $parentId")
    var childEl = elementById[Select](childId)

    if (childEl != null)
      elementById[Div](parentId).removeChild(childEl)
  }

  /*********************************************************************************************************************
    * Create DOM elements
    */
  def createElement[A <: js.Any](tagName: String): A = document.createElement(tagName).asInstanceOf[A]

  def createParagraph(txt: String): Paragraph = {
    var p = createElement[Paragraph]("p")
    p.textContent = txt
    p
  }

  def createImage(src: String): Image = {
    var img = createElement[Image]("img")
    img.setAttribute("src",src)
    img
  }

  def createDropdownCombo(inputId: String, dlId: String, txt: String): (Input, DataList) = {
    var el1 = createElement[Input]("input")
    var el2 = createElement[DataList]("datalist")

    el1.setAttribute("type","text")
    el1.setAttribute("id",inputId)
    el1.setAttribute("list",dlId)
    el1.setAttribute("placeholder",txt)

    el2.setAttribute("id",dlId)

    return (el1, el2)
  }

  def createSelect(id: String): Select = {
    var el = createElement[Select]("select")
    el.id  = id
    el
  }

  def createDiv(id: String): Div = {
    var el = createElement[Div]("div")
    el.id = id
    el
  }

  def createDataListOption(id: String, attrs: Seq[(String, String)]): Option = {
    var el = createElement[Option]("option")
    el.id          = id
    el.textContent = id

    attrs.map {(attr: (String, String)) => el.setAttribute(attr._1, attr._2)}

    el
  }

  def createTableRow(): TableRow = createElement[TableRow]("tr")

  def createTableCaption(title: String): TableCaption = {
    var c = createElement[TableCaption]("caption")
    c.textContent = title
    c
  }

  def createTableCell(content: Node): TableDataCell = {
    var el = createElement[TableDataCell]("td")
    el.appendChild(content)
    el
  }

  def createTableCell(text: String): TableDataCell = {
    var el = createElement[TableDataCell]("td")
    el.textContent = text
    el
  }

  def createTableCellWithId(text: String, id: String): TableDataCell = {
    var el = createTableCell(text)
    el.id = id
    el
  }

  // Build a row for the weather table
  def createWeatherTableRow(rowData: Map[String, String]): TableRow =
    rowData.keys.foldLeft(createTableRow()) { (acc: TableRow, key: String) =>
      acc.appendChild(createTableCellWithId(key,"label"))
      acc.appendChild(createTableCell(rowData.get(key).get))
      acc
    }

  /*********************************************************************************************************************
    * Build country input field and associated datalist
    *
    * This function should only be called once per app invocation
    */
  def buildCountryList(self: ActorRef): Unit = {
    val fnName = "buildCountryList"
    trace(fnName, enter)

    // The country list div is guaranteed to exist
    val countryListDiv = elementById[Div](MessageBox.countryListDiv)

    // Create new input field and associated datalist elements
    var (countryInput, countryDataList) =
      createDropdownCombo(MessageBox.countryInput, MessageBox.countryDataList, "Select a country")

    CountryList.map {
      country: js.Array[js.Dynamic] =>
        countryDataList.appendChild(
          createDataListOption(country(0).asInstanceOf[String], Seq(("iso", country(1).asInstanceOf[String])))
        )
    }

    countryInput.addEventListener("input", (msg: Event) => self ! msg)

    countryListDiv.appendChild(countryInput)
    countryListDiv.appendChild(countryDataList)

    trace(fnName, exit)
  }

  /*********************************************************************************************************************
    * Build region input field and associated datalist
    *
    * This function should only be called for countries that contain at least one region
    */
  def buildRegionList(self: ActorRef, countryInfo: js.Dynamic): Unit = {
    val fnName = "buildRegionList"
    trace(fnName, enter)

    var regionCount: Int = countryInfo.regionList.length.asInstanceOf[Int]

    traceInfo(fnName,s"${countryInfo.name.asInstanceOf[String]} contains" +
      s" ${countryInfo.regionList.length.asInstanceOf[Int]} regions")

    // Get reference to the region HTML elements.
    // Only the region list div is guaranteed to exist
    val regionListDiv = elementById[Div](MessageBox.regionListDiv)

    // Throw away the existing inputfield & datalist elements and create a new ones
    deleteChild(MessageBox.regionListDiv, MessageBox.regionInput)
    deleteChild(MessageBox.regionListDiv, MessageBox.regionDataList)

    // Create new input field and associated datalist elements
    var (regionInput, regionDataList) =
      createDropdownCombo(MessageBox.regionInput, MessageBox.regionDataList, "Select a region")

    // If the country has only one named region, then don't bother adding a placeholder option.
    // Make the only region the selected option
    if (regionCount > 1)
      countryInfo.regionList.map {
        region: js.Dynamic =>
          regionDataList.appendChild(
            createDataListOption(
              region.name.asInstanceOf[String],
              Seq(("region_no", region.id.asInstanceOf[Integer].toString))
            )
          )
      }
    else {
      regionDataList.appendChild(
        createDataListOption(countryInfo.region(0).asInstanceOf[String], Seq(("region_no", "00"),("selected","true")))
      )
    }

    regionInput.addEventListener("input", (msg: Event) => self ! msg)

    regionListDiv.appendChild(regionInput)
    regionListDiv.appendChild(regionDataList)
    trace(fnName, exit)
  }

  /*********************************************************************************************************************
    * Build city input field and associated datalist
    *
    * This function should only be called for countries that contain at least one region
    */
  def buildCityList(self: ActorRef, citiesInRegion: js.Dynamic): Unit = {
    val fnName = "buildCityList"
    trace(fnName, enter)

    // Get reference to the city HTML elements.
    // Only the city list div is guaranteed to exist
    val cityListDiv = elementById[Div](MessageBox.cityListDiv)

    // Throw away the inputfield + datalist and create a new ones
    deleteChild(MessageBox.cityListDiv, MessageBox.cityInput)
    deleteChild(MessageBox.cityListDiv, MessageBox.cityDataList)

    // Create a new input field and associated datalist elements
    var (cityInput, cityDataList) =
      createDropdownCombo(MessageBox.cityInput, MessageBox.cityDataList, "Select a city/town/village")

    // Transform the cityList parameter into a sorted sequence of City instances
    var cities: Seq[City] = Seq.empty

    citiesInRegion.map { cityInfo: js.Dynamic => cities :+= new City(cityInfo) }
    cities = cities.sortWith(_.name < _.name)

    traceInfo(fnName, s"Found ${cities.size} cities")

    // If the country has only one city, then don't bother adding a placeholder option.
    // Make the only city the selected option
    if (cities.size > 1) {
      cities.map {
        city: City => cityDataList.appendChild(
          createDataListOption(city.name, Seq(("lat", city.lat.toString), ("lng", city.lng.toString)))
        )
      }

      // If the city list contains more than 512 entries, switch on warning about 512 item display limit
      if (cities.size > 512)
        showElement(MessageBox.itemCountWarning)
      else
        hideElement(MessageBox.itemCountWarning)
      }
    else {
      var opt = createDataListOption(
        cities(0).name,
        Seq(("lat", cities(0).lat.toString), ("lng", cities(0).lng.toString),("selected","true")))
      cityDataList.appendChild(opt)
    }

    cityInput.addEventListener("input", (msg: Event) => self ! msg)

    cityListDiv.appendChild(cityInput)
    cityListDiv.appendChild(cityDataList)

    trace(fnName, exit)
  }

  /*********************************************************************************************************************
   * Build weather report
   */
  def buildWeatherReport(w: WeatherReportBuilder): Unit = {
    val fnName = "buildWeatherReport"
    trace(fnName, enter)

    showElement[Div](MessageBox.weatherReportDiv)

    var weatherDiv = elementById[Div](MessageBox.weatherReportDiv)

    var tab   = createElement[Table]("table")
    var tbody = tab.createTBody()
    var rows  = ListBuffer[Map[String,String]]()

    // The caption text varies depending on whether we're playing in a posh theatre or the village hall
    val captionTxt =
      // Are we playing in a posh theatre?
      if (StageManager.showCountryRegionCity) {
        // Yup, so define table caption text
        "Weather for " +
        s"${Utils.textListFirstItem(StageManager.thisCity)}" +
        s"${Utils.textListItem(StageManager.thisRegion)}" +
        s"${Utils.textListItem(StageManager.thisCountry)}"
      }
      else
        // Nope, its the village hall
        s"Weather for (${StageManager.thisLat},${StageManager.thisLng})"

    tab.appendChild(createTableCaption(captionTxt))

    // Weather report table must be identifiable in order to delete it
    tab.id = MessageBox.weatherReportTable

    // Temperature is always supplied in Kelvin
    rows += Map("Temperature" -> Utils.kelvinToDegStr(w.main.temp, w.main.temp_min, w.main.temp_max))

    // Atmospheric pressure is supplied either as a single value, or as a
    // sea level/ground level pair
    if (w.main.grnd_level == 0)
      rows += Map("Atmospheric Pressure" -> Utils.formatPressure(w.main.airPressure))
    else {
      rows += Map("Atmospheric Pressure (Ground Level)" -> Utils.formatPressure(w.main.grnd_level))
      rows += Map("Atmospheric Pressure (Sea Level)"    -> Utils.formatPressure(w.main.sea_level))
    }

    rows += Map("Humidity"       -> Utils.formatPercentage(w.main.humidity))
    rows += Map("Visibility"     -> Utils.formatVisibility(w.visibility))
    rows += Map("Wind Speed"     -> Utils.formatVelocity(w.wind.speed))
    rows += Map("Wind Direction" -> Utils.formatHeading(w.wind.heading))
    rows += Map("Cloud Cover"    -> Utils.formatPercentage(w.clouds))

    // Transform each weather value into a table row and add them to the table body
    rows.
      map(createWeatherTableRow).
      map((r: TableRow) => tbody.appendChild(r))

    // Add the icons for (potentially) multiple weather conditions directly to
    // the table body
    w.weatherConditions.map((w: WeatherCond) => {
      var row = createTableRow()

      // Column 1 contains the weather condition's description
      // Column 2 contains the image for that weather condition
      row.appendChild(createTableCellWithId(Utils.formatDescription(w.desc), "label"))
      row.appendChild(createTableCell(createImage(Utils.getOwmImgUrl(w.icon))))

      tbody.appendChild(row)
    })

    tab.appendChild(tbody)
    weatherDiv.appendChild(tab)

    trace(fnName, exit)
  }

  /*********************************************************************************************************************
    * Show "API Key Missing" error message
    */
  def apiKeyMissing(): Unit = {
    trace("apiKeyMissing", None)
    showElement[Div]("missing_api_key")
  }

  /*********************************************************************************************************************
    * Show message for Safari users
    */
  def safariMsg(): Unit = {
    trace("safariMsg", None)
    showElement[Div]("safari_msg")
  }
}

