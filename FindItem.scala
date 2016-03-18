#!/usr/bin/env scala -deprecation -J-Xmx4g -J-XX:NewRatio=4

import scala.collection.JavaConversions._

import java.lang.Exception

import com.ebay.services.client.ClientConfig
import com.ebay.services.client.FindingServiceClientFactory
import com.ebay.services.finding.FindItemsByKeywordsRequest
import com.ebay.services.finding.FindItemsByKeywordsResponse
// import com.ebay.services.finding.FindingServicePortType
import com.ebay.services.finding.PaginationInput
// import com.ebay.services.finding.SearchItem


object FindItem {

  def main(cmdLineArgs: Array[String])
    {
       if (cmdLineArgs.length == 0) {
           // show usage information and exit
           usage()
         }

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
         System.out.println("Ack = " + result.getAck())

         System.out.println("Found " + result.getSearchResult().getCount() +
                            " items.")

         val items = result.getSearchResult().getItem()

         for(item <- items) {
           System.out.println(item.getTitle())
         }
       } catch {
         case ex: Exception => { ex.printStackTrace() }
       }
    }

  def usage()
    {
      println("Usage:\n\tFindItem keywords to search...")
      sys.exit(1)
    }

}
