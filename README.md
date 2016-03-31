# Find an auction

Find items if you want to buy them from eBay

# WIP

This project is a *work in progress*. The implementation is *incomplete* and
subject to change. The documentation can be inaccurate.

# Example:

To find for an auction for a new Apple 15 inches MacBook laptop:

     # set required environment variable
     
     EBAY_API_APP_ID="my eBay API Application ID"
     export EBAY_API_APP_ID
      
     ./FindItem.scala  "new apple macbook 15 laptop"
          itemId: 291555615515
          title: NEW SEALED 2015 APPLE 15" MACBOOK PRO 2.2GHz i7 16GB 256GB MJLQ2LL/A RETINA
          globalId: EBAY-US
          condition: com.ebay.services.finding.Condition@489115ef
          viewItemURL: http://www.ebay.com/itm/NEW-SEALED-2015-APPLE-15-MACBOOK-PRO-2-2GHz-i7-16GB-256GB-MJLQ2LL-A-RETINA-/291555615515
          galleryURL: http://thumbs4.ebaystatic.com/m/mhNU6VAlmCSGcHK_Zq4QOzw/140.jpg
          subtitle: BRAND NEW FACTORY SEALED WITH FORCE TOUCH TRACKPAD!
          primaryCategory: com.ebay.services.finding.Category@3857f613
          secondaryCategory: null
          charityId: null
          productId: null
          paymentMethod: [PayPal]
          autoPay: true
          postalCode: null
          location: USA
          country: US
          storeInfo: null
          sellerInfo: null
          shippingInfo:
            type: FlatDomesticCalculatedInternational
            shipToLocations: [US, CA, GB, AU, AT, BE, FR, DE,
                              IT, JP, ES, NL, CN, HK, MX, DK,
                              RO, SK, BG, CZ, FI, HU, LV, LT,
                              MT, EE, GR, PT, CY, SI, SE, KR,
                              ID, ZA, TH, IE, PL, IL, NZ, SG]
            expeditedShipping: true
            oneDayShippingAvailable: true
            handlingTime: 1
          sellingStatus: com.ebay.services.finding.SellingStatus@198b6731
          listingInfo:
             listingType: FixedPrice
             buyItNowAvailable: false
             buyItNowPrice: null
             bestOfferEnabled: false
             endTime: 03/31/2016 14:03:00 EDT
             any: []
          returnsAccepted: true
          galleryPlusPictureURL: [http://galleryplus.ebayimg.com/ws/web/291555615515_1_4_1.jpg]
          compatibility: null
          distance: null
          delimiter: null
          any: [[isMultiVariationListing: null], [discountPriceInfo: null], [topRatedListing: null]]

# Requirements

This program has been tested with Scala 2.11.6 and Scala 2.11.8

You need to create a free eBay API account at [https://go.developer.ebay.com/what-ebay-api] (https://go.developer.ebay.com/what-ebay-api)

Create your Application ID in eBay there.

You need to download the eBay Finding Kit for Enhaced Search SDK for Java at [https://go.developer.ebay.com/javasdk](https://go.developer.ebay.com/javasdk)

Unzip the archive and add the `src/lib/finding.jar`
and the `src/lib/log4j-1.2.16.jar` JAR files to
your CLASSPATH.

Set the environment variable `EBAY_API_APP_ID` with
the value of your eBay API Application ID you created
above.

# Debug Communication from the client with the eBay Backend Service

eBay compiles its client Finding Kit for Enhaced Search SDK in Java
using the Apache Log4j Logging Framework (the eBay Java Trading SDK
seems to use Simple Logging Facade for Java (SLF4J) though, but we don't
use the latter, only the former: see
[http://developer.ebay.com/DevZone/javasdk-jaxb/docs/readme.htm](http://developer.ebay.com/DevZone/javasdk-jaxb/docs/readme.htm)
for the eBay Java Trading SDK).

This repository has a simple `log4j.properties` file to debug the
communication from the client with the eBay Backend Service. It will
show the raw SOAP XML messages used in the requests from the client
and the responses from the server, like the request:

       [INFO ] 2016-03-30 21:19:54,886 [main][JAXWSHandler.java:62]: com.ebay.common.handler.JAXWSHandler: sending soap request message ...
           [...omitted ...]
           <findItemsByKeywordsRequest xmlns="http://www.ebay.com/marketplace/search/v1/services">
             <paginationInput>
               <entriesPerPage>100</entriesPerPage>
             </paginationInput>
             <keywords>new apple macbook 15 laptop</keywords>
             <itemFilter>
               <name>MaxPrice</name>
               <value>2000</value>
             </itemFilter>
             <itemFilter>
               <name>Condition</name>
               <value>Used</value>
             </itemFilter>
             <itemFilter>
               <name>Currency</name>
               <value>USD</value>
             </itemFilter>
             ...
           </findItemsByKeywordsRequest>
           [...omitted ...]

so that debugging and also some performance analysis (using the
timestamps from the dumps) can be done.

# Possible improvements to this program

Report the eBay item auctions in JSON format, using the
`play.api.libs.json._` library for this. The issue is
that the auctions are returned by eBay paginated, hence,
all these pages have to be requested first from eBay (eBay
have a limit on the number of API of requests per day, in
the order of thousands only), then concatenated, and the
result converted into JSON string to output.

# Updating the Finding Kit for Enhaced Search client-side JAR

It is possible that there are new updates to the server-side
Finding Kit for Enhaced Search API, and then you may want to
update the provided `finding.jar`.

To do so:

     1. Download the new FindingService.wsdl from

          https://developer.ebay.com/webservices/finding/latest/FindingService.wsdl

     2. Copy this new file FindingService.wsdl over the existing FindingService.wsdl
        in the source directory tree of the Finding Kit for Enhaced Search SDK

     3. Run:

             ant compile-wsdl
             ant compile
             ant build
             ant jar

It will print at the last of these instructions something like:

     jar:
           [jar] Building jar: <path-to-new>/lib/finding.jar

Make sure this new `finding.jar` is in your CLASSPATH to use it.

# Inspiration

The initial idea of this program is a Java sample
authored by boyang inside eBay Java SDK archive.

This SDK also supports more options, like the Trading
API (Buying, Selling and After Sale support), besides
the Finding API, which supports Searching for products
and items (besides Buying), and is the one we are
using here (we don't use Buying though, the Find gives
the URL where the auction can be seen). Details are
here: [https://go.developer.ebay.com/api-features-comparison](https://go.developer.ebay.com/api-features-comparison)

There are APIs for other programming languages besides
Java (used in Scala through the JVM), here:
[https://go.developer.ebay.com/ebay-sdks](https://go.developer.ebay.com/ebay-sdks)

The initial idea of this program is a Java sample
authored by boyang inside eBay Java SDK archive.

This SDK also supports more options, like the Trading
API (Buying, Selling and After Sale support), besides
the Finding API, which supports Searching for products
and items (besides Buying), and is the one we are
using here (we don't use Buying though, the Find gives
the URL where the auction can be seen). Details are
here:
[https://go.developer.ebay.com/api-features-comparison](https://go.developer.ebay.com/api-features-comparison)

There are APIs for other programming languages besides
Java (used in Scala through the JVM), here:
[https://go.developer.ebay.com/ebay-sdks](https://go.developer.ebay.com/ebay-sdks)

