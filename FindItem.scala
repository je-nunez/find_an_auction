#!/usr/bin/env scala -deprecation -J-Xmx4g -J-XX:NewRatio=4

import scala.collection.JavaConversions._

// import play.api.libs.json._

import java.lang.Exception

import com.ebay.services.client.ClientConfig
import com.ebay.services.client.FindingServiceClientFactory
import com.ebay.services.finding.FindItemsByKeywordsRequest
import com.ebay.services.finding.FindItemsByKeywordsResponse
// import com.ebay.services.finding.FindingServicePortType
import com.ebay.services.finding.PaginationInput
import com.ebay.services.finding.SearchItem


object FindItem {

  def main(cmdLineArgs: Array[String]) {

    if (cmdLineArgs.length == 0)
      // show usage lines and exit
      usage()

    try {
      // initialize service end-point configuration
      val config = new ClientConfig()

      val eBayApplicationId = "put-here-your-eBay-API-application-ID"

      config.setApplicationId(eBayApplicationId)

      //create a service client
      val serviceClient =
        FindingServiceClientFactory.getServiceClient(config)

      //create request object
      val request = new FindItemsByKeywordsRequest()

      //set request parameters
      request.setKeywords(cmdLineArgs.mkString(" "))

      val pi = new PaginationInput()

      pi.setEntriesPerPage(2)

      request.setPaginationInput(pi)

      //call service
      val result = serviceClient.findItemsByKeywords(request)

      //output result
      println("Ack = " + result.getAck())

      println("Found " + result.getSearchResult().getCount() + " items.")

      val items = result.getSearchResult().getItem()

      for(item <- items) {
        reportItem(item)
      }
    } catch {
      case ex: Exception => { ex.printStackTrace() }
    }
  }

  def usage() {
    println("Usage:\n\tFindItem keywords to search...")
    sys.exit(1)
  }

  def reportItem(item: SearchItem) {

    var reportStr = new StringBuilder(32*1024)

    reportStr.append("itemId: " + item.getItemId + "\n")
    reportStr.append("title: " + item.getTitle + "\n")
    reportStr.append("globalId: " + item.getGlobalId + "\n")
    reportStr.append("condition: " + item.getCondition + "\n")
    reportStr.append("viewItemURL: " + item.getViewItemURL + "\n")
    reportStr.append("galleryURL: " + item.getGalleryURL + "\n")

    reportStr.append("subtitle: " + item.getSubtitle + "\n")
    reportStr.append("primaryCategory: " + item.getPrimaryCategory + "\n")
    reportStr.append("secondaryCategory: " + item.getSecondaryCategory + "\n")
    reportStr.append("charityId: " + item.getCharityId + "\n")
    reportStr.append("productId: " + item.getProductId + "\n")
    reportStr.append("paymentMethod: " + item.getPaymentMethod + "\n")
    reportStr.append("autoPay: " + item.isAutoPay + "\n")
    reportStr.append("postalCode: " + item.getPostalCode + "\n")
    reportStr.append("location: " + item.getLocation + "\n")
    reportStr.append("country: " + item.getCountry + "\n")
    reportStr.append("storeInfo: " + item.getStoreInfo + "\n")
    reportStr.append("sellerInfo: " + item.getSellerInfo + "\n")

    val shippingInfo = item.getShippingInfo
    if (shippingInfo != null) {   // result is from Java, hence != null
      reportStr.append("shippingInfo:" + "\n")
      reportStr.append("  type: " + shippingInfo.getShippingType + "\n")
      reportStr.append("  shipToLocations: " + shippingInfo.getShipToLocations + "\n")
      reportStr.append("  expeditedShipping: " + shippingInfo.isExpeditedShipping + "\n")
      reportStr.append("  oneDayShippingAvailable: " + shippingInfo.isOneDayShippingAvailable + "\n")
      reportStr.append("  handlingTime: " + shippingInfo.getHandlingTime + "\n")
    } else {
      reportStr.append("shippingInfo: null" + "\n")
    }

    reportStr.append("sellingStatus: " + item.getSellingStatus + "\n")
    reportStr.append("listingInfo: " + item.getListingInfo + "\n")
    reportStr.append("returnsAccepted: " + item.isReturnsAccepted + "\n")
    reportStr.append("galleryPlusPictureURL: " + item.getGalleryPlusPictureURL + "\n")
    reportStr.append("compatibility: " + item.getCompatibility + "\n")
    reportStr.append("distance: " + item.getDistance + "\n")
    reportStr.append("delimiter: " + item.getDelimiter + "\n")
    reportStr.append("any: " + item.getAny + "\n")

    reportStr.append("----")

    println(reportStr)
  }

}

