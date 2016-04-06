#!/usr/bin/env scala -deprecation -J-Xmx512m -J-XX:NewRatio=4

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

// import play.api.libs.json._

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.TimeZone

// import org.apache.log4j.{Logger,Level}

import com.ebay.services.client.ClientConfig
import com.ebay.services.client.FindingServiceClientFactory
import com.ebay.services.finding.FindItemsByKeywordsRequest
import com.ebay.services.finding.ItemFilterType
import com.ebay.services.finding.ItemFilter
import com.ebay.services.finding.FindItemsByKeywordsResponse
// import com.ebay.services.finding.FindingServicePortType
import com.ebay.services.finding.PaginationInput
import com.ebay.services.finding.SearchItem
import com.ebay.services.finding.Condition
import com.ebay.services.finding.SellingStatus
import com.ebay.services.finding.ListingInfo
import com.ebay.services.finding.ShippingInfo
import com.ebay.services.finding.Category


object FindItem {

  // this is a special key in the map of the command-line arguments:
  // the value of this key in the parsing of the command-line arguments will
  // the keywords to search for items in auctions
  val argOptionForKeywordSearch: String = "auctionKeywords"
  val argOptionNumbItemsToReturn = "numb_items_to_return"

  val defaultItemsPerResultPage = 10

  def main(cmdLineArgs: Array[String]) : Unit = {

    // these are the available item filters to request as necessary conditions
    // to satisfy by the server. E.g., filters as to a max-price limit, etc,
    // on the items returned.
    val availableItemFilters =
      ItemFilterType.values().map(_.toString.toLowerCase).to[ArrayBuffer]
    availableItemFilters.prepend(argOptionNumbItemsToReturn)

    if (cmdLineArgs.length == 0) {
      // show usage lines and exit with exit-code 1
      showUsage(availableItemFilters, 1)
    }

    // a required environment variable with the value of your
    // eBay API Application ID
    val eBayApplicationId = sys.env.get("EBAY_API_APP_ID")

    if (eBayApplicationId.isEmpty) {
      errorMissingEnvirVar()    // and exits this program
    }

    val cmdLineOpts = parseCmdLine(cmdLineArgs, availableItemFilters)
    // The search keywords must have been given in the command-line
    val keywordsSearch = cmdLineOpts.find(_._1 == argOptionForKeywordSearch)
    if (keywordsSearch isEmpty) {
      errorCantFindSearchKeywords(cmdLineOpts)
    }

    eBayWork(eBayApplicationId.get, cmdLineOpts)
  }

  def showUsage(availableFilters: Seq[String], exitCode: Int): Unit = {

    var usageStr = new StringBuilder(4*1024)

    usageStr.append("Usage:\n\tFindItem [--filter1 value1] " +
                    "[--filter2 value2 ...] keywords to search...\n\n")

    usageStr.append("Possible options:\n")
    availableFilters foreach
      (filter => usageStr.append(s"    --$filter value\n"))

    usageStr.append("\nYou need to set the environment variable EBAY_API_APP_ID " +
                    "with the value of a valid eBay API Application ID.")

    println(usageStr)
    sys.exit(exitCode)
  }

  def errorMissingEnvirVar(): Unit = {
    System.err.println("Error: Environment variable EBAY_API_APP_ID must " +
                       "have the value of a valid eBay API Application ID.")
    sys.exit(2)
  }

  def errorCantFindSearchKeywords(foundOptions: List[(String, String)]): Unit = {
    System.err.println("Error: Couldn't find item search keywords in the " +
                       "command-line. Found these options:\n\t" +
                       foundOptions.toString)
    sys.exit(3)
  }

  def parseCmdLine(cmdLineArgs: Array[String], validOptions: Seq[String]): List[(String, String)] =
    {
      // return a list of tuples (optionName, optionValue) ... with the options and their values
      // given in the cmdLineArgs and which are in the universe of validOptions. (For an option
      // --optName where this optName is not in validOptions, then exit the program, since it is
      // not an allowable option.)
      //  Note: we could have used 'scopt' package to parse the cmd-line, but we don't know in fact
      //        the validOptions and their type-values they should have: we just take all eBay's
      //        ItemFilterType.values(), which are many, as allowable cmd-line options for this
      //        program, whose values are String, and pass these values as the user gives then to
      //        the underlying eBay Finding Filter service to validate it, because there are many
      //        and we can't know the syntactic/semantic restrictions for each.

      // all auction filters takes two tokens, '--filterName value', and the last string contains the
      // search keywords for the auction: we add an empty string to make the length of the array even

      var cmdArgs = cmdLineArgs.to[ArrayBuffer]
      if (cmdArgs.length % 2 == 1) { cmdArgs += "" }

      var auctionKeywords: String = ""    // the item search keywords given by the user

      val listOptVals = cmdArgs.iterator.sliding(2,1).toList.collect {
          case Seq("--help", ignored: String) => { showUsage(validOptions, 0) }
          case Seq(auctionKeyword: String, "") => {
            if (auctionKeywords.isEmpty) {
              auctionKeywords = auctionKeyword
              argOptionForKeywordSearch -> auctionKeyword
            } else {
              // This is not expected to happen, although there is no side effect since the program
              // exits immediately (and it is difficult to check the formal specification, since it
              // is only given by whatever filter names the auction search accepts as conditions)
              System.err.println(s"ERROR: Auction search keywords already set: $auctionKeywords")
              sys.exit(2)
            }
          }
          case Seq(option: String, value: String) => {
            if (option.startsWith("--")) {
              val optionName = option.stripPrefix("--")
              if (validOptions contains optionName ) { optionName -> value }
              else { invalidOption(option, validOptions) }
            } else {
              // System.err.println(s"Ignoring unknown option-value: '$option' '$value'")
            }
          }
        }

      // remove the empty tuples (optName, value) inside the collection, and then convert it to
      // a List, which is the one to be returned
      listOptVals.filterNot(tupleOptVal => tupleOptVal == (())).
        map { case (k: String, v: String) => (k -> v) }.toList
    }

  def invalidOption(invalidOption: String, validOptions: Seq[String]): Unit = {

    var errMsg = new StringBuilder(4*1024)

    errMsg.append(s"ERROR: Unknown option '$invalidOption'\n\n")

    errMsg.append("Valid command-line options are:\n")
    validOptions foreach (option => errMsg.append(s"    --$option value\n"))

    System.err.println(errMsg)
    sys.exit(3)
  }

  def eBayWork(eBayApplicationId: String, cmdLineOpts: List[(String, String)]): Unit = {
    try {
      // initialize service end-point configuration
      val config = new ClientConfig()

      config.setApplicationId(eBayApplicationId)

      // create a service client
      val serviceClient =
        FindingServiceClientFactory.getServiceClient(config)

      // create request object
      val request = new FindItemsByKeywordsRequest()

      // set the keywords to search for the items of interest
      val keywordsSearch = cmdLineOpts.find(_._1 == argOptionForKeywordSearch)
      request.setKeywords(keywordsSearch.get._2)

      val cmdLnOptItemsPerPage = cmdLineOpts.find(_._1 == argOptionNumbItemsToReturn)
      val resultsReturnedPerPage = cmdLnOptItemsPerPage match {
          case Some((argOptionNumbItemsToReturn, valueItemsPerPage)) => {
            valueItemsPerPage.toInt
          }
          case None => { defaultItemsPerResultPage }
        }

      val pi = new PaginationInput()
      pi.setEntriesPerPage(resultsReturnedPerPage)
      request.setPaginationInput(pi)

      buildItemFilters(cmdLineOpts, request.getItemFilter())

      // call service (rate-limiting by the eBay API can still occur)
      val result = serviceClient.findItemsByKeywords(request)

      println("Query Results = " + result.getAck())  // acknowledge status from server

      val proceedToReport = result.getAck().value().toLowerCase != "failure"   // or == "success"
      if (proceedToReport) {
        println("Found " + result.getSearchResult().getCount() + " items.")

        val items = result.getSearchResult().getItem()

        for {item <- items} {
          reportItem(item)
        }
      }
    } catch {
      case ex: Exception => { ex.printStackTrace() }
    }
  }

  def buildItemFilters(cmdLineOpts: List[(String, String)],
                       accumulRes: java.util.List[ItemFilter]): Unit = {

    var options2Filters = scala.collection.mutable.Map[String, ItemFilter]()

    for {(k, v) <- cmdLineOpts
         if (k != argOptionForKeywordSearch && k != argOptionNumbItemsToReturn) } {
      // eBay uses formal uppercase in its Enum values, while we took them to lowercase as
      // more adequate for command-line options to the end-user, so she/he doesn't need
      // to use Caps-Locks while entering them: now we have to take it back to upper-case

      var eBayFilter: ItemFilter = null

      if (options2Filters isDefinedAt k.toUpperCase) {
        eBayFilter = options2Filters(k.toUpperCase)
      } else {
        eBayFilter = new ItemFilter()
        // initialize the new ItemFilter:
        val javaEnum = ItemFilterType.valueOf(k.toUpperCase)
        eBayFilter.setName(javaEnum)
        // keep track of this new ItemFilter
        options2Filters += k.toUpperCase -> eBayFilter
        accumulRes.add(eBayFilter)
      }
      val valList = eBayFilter.getValue
      valList.append(v)
    }
  }

  def reportItem(item: SearchItem): Unit = {

    // this report eBay Item is long, since the eBay Java SDK returns many fields in an
    // eBay SearchItem instance: it tries to build a temporary string with all the fields
    // to report, before printing it

    var reportStr = new StringBuilder(32*1024)

    reportStr.append("itemId: " + item.getItemId + "\n")
    reportStr.append("title: " + item.getTitle + "\n")
    reportStr.append("globalId: " + item.getGlobalId + "\n")

    reportStr.append(conditionToStr(Option(item.getCondition)))

    reportStr.append("viewItemURL: " + item.getViewItemURL + "\n")
    reportStr.append("galleryURL: " + item.getGalleryURL + "\n")
    reportStr.append("galleryPlusPictureURL: " + item.getGalleryPlusPictureURL + "\n")
    reportStr.append("returnsAccepted: " + item.isReturnsAccepted + "\n")

    reportStr.append("subtitle: " + item.getSubtitle + "\n")

    reportStr.append(sellingStatusToStr(Option(item.getSellingStatus)))
    reportStr.append(listingInfoToStr(Option(item.getListingInfo)))
    reportStr.append(shippingInfoToStr(Option(item.getShippingInfo)))

    reportStr.append(categoryToStr("primaryCategory", Option(item.getPrimaryCategory)))
    reportStr.append(categoryToStr("secondaryCategory", Option(item.getSecondaryCategory)))

    reportStr.append("charityId: " + item.getCharityId + "\n" +
                     "productId: " + item.getProductId + "\n" +
                     "paymentMethod: " + item.getPaymentMethod + "\n" +
                     "autoPay: " + item.isAutoPay + "\n" +
                     "postalCode: " + item.getPostalCode + "\n" +
                     "location: " + item.getLocation + "\n" +
                     "country: " + item.getCountry + "\n" +
                     "storeInfo: " + item.getStoreInfo + "\n" +
                     "sellerInfo: " + item.getSellerInfo + "\n" +
                     "compatibility: " + item.getCompatibility + "\n" +
                     "distance: " + item.getDistance + "\n" +
                     "delimiter: " + item.getDelimiter + "\n" +
                     "any: " + item.getAny + "\n" +
                     "----")

    println(reportStr)
  }

  /* Below are some auxiliary functions to get the string representation
   * to print from some attribute objects returned by SearchItem.
   *
   * To use play.api.libs.json._ is possible to print in JSON, although it
   * requires more dependent libraries
   */

  def conditionToStr(condition: Option[Condition]): String = {

    condition match {
      case Some(cond) => {
        "condition:\n" +
        "  conditionDisplayName: " + cond.getConditionDisplayName + "\n" +
        "  any: " + cond.getAny + "\n"
      }
      case None => {
        "condition: null\n"
      }
    }
  }

  def categoryToStr(classification: String, category: Option[Category]): String = {

    category match {
      case None => {
        classification + ":\n"
      }
      case Some(categ) => {
        classification + ":\n" +
        "  categoryName: " + categ.getCategoryName + "\n" +
        "  categoryId: " + categ.getCategoryId + "\n"
      }
    }
  }

  def sellingStatusToStr(sellingStatus: Option[SellingStatus]): String = {

    sellingStatus match {
      case None => {
        "sellingStatus: null" + "\n"
      }
      case Some(sellStatus) => {
        var tempStr = new StringBuilder(4*1024)

        tempStr.append("sellingStatus:\n" +
                       "  currentPrice: value: " + sellStatus.getCurrentPrice.getValue + "\n" +
                       "  currentPrice: currencyId: " +
                       sellStatus.getCurrentPrice.getCurrencyId + "\n" +
                       "  convertedCurrentPrice: value: " +
                       sellStatus.getConvertedCurrentPrice.getValue + "\n" +
                       "  convertedCurrentPrice: currencyId: " +
                       sellStatus.getConvertedCurrentPrice.getCurrencyId + "\n" +
                       "  getBidCount: " + sellStatus.getBidCount + "\n" +
                       "  sellingState: " + sellStatus.getSellingState + "\n")

        val timeLeft = Option(sellStatus.getTimeLeft)
        timeLeft match {
          case Some(tmLeft) => {
            tempStr.append("  timeLeft:")
            if (tmLeft.getMonths != 0) {
              tempStr.append(" " + tmLeft.getMonths + " months")
            }
            if (tmLeft.getDays != 0) {
              tempStr.append(" " + tmLeft.getDays + " days")
            }
            val hours = tmLeft.getHours
            val minutes = tmLeft.getMinutes
            val seconds = tmLeft.getSeconds
            tempStr.append(f" $hours%02d:$minutes%02d:$seconds%02d\n")
          }
          case None => {
            tempStr.append("  timeLeft: null" + "\n")
          }
        }
        tempStr.append("  any: " + sellStatus.getAny + "\n")

        tempStr.toString
      }
    }
  }

  def listingInfoToStr(listingInfo: Option[ListingInfo]): String = {

    listingInfo match {
      case None => {
        "listingInfo: null" + "\n"
      }
      case Some(listing) => {
        var tempStr = new StringBuilder(2*1024)
        tempStr.append("listingInfo:\n" +
                         "   listingType: " + listing.getListingType + "\n" +
                         "   buyItNowAvailable: " + listing.isBuyItNowAvailable + "\n" +
                         "   buyItNowPrice: " + listing.getBuyItNowPrice + "\n" +
                         "   bestOfferEnabled: " + listing.isBestOfferEnabled + "\n")
        val endTime = Option(listing.getEndTime)
        endTime match {
          case Some(endTm) => {
            val dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:MM:SS z")
            tempStr.append("   endTime: " + dateFormatter.format(endTm.getTime()) + "\n")
          }
          case None => {
            tempStr.append("   endTime: null\n")
          }
        }
        tempStr.append("   any: " + listing.getAny + "\n")

        tempStr.toString
      }
    }
  }

  def shippingInfoToStr(shippingInfo: Option[ShippingInfo]): String = {

    shippingInfo match {
      case None => {
        "shippingInfo: null\n"
      }
      case Some(shipping) => {
        "shippingInfo:\n" +
        "  type: " + shipping.getShippingType + "\n" +
        "  shipToLocations: " + shipping.getShipToLocations + "\n" +
        "  expeditedShipping: " + shipping.isExpeditedShipping + "\n" +
        "  oneDayShippingAvailable: " + shipping.isOneDayShippingAvailable + "\n" +
        "  handlingTime: " + shipping.getHandlingTime + "\n" +
        "  any: " + shipping.getAny + "\n"
      }
     }
  }

}
