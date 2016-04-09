# Find items in eBay

Find items and other information from eBay, prices, locations, end times of auctions or buy-it-now, from the command-line.

# WIP

This project is a *work in progress*. The implementation is *incomplete* and
subject to change. The documentation can be inaccurate.

# Example:

To find for an auction for a new Apple 15 inches MacBook laptop:

     # set required environment variable

     EBAY_API_APP_ID="my eBay API Application ID"
     export EBAY_API_APP_ID

     # The program also supports command-line options to filter the
     # request, e.g., to set a minimun and/or maximum price, etc.
     # (See the section "Command-line options" below.)

     ./FindItem.scala  "new apple macbook 15 laptop"
          [... previous results ...]
          itemId: 291555615515
          title: NEW SEALED 2015 APPLE 15" MACBOOK PRO 2.2GHz i7 16GB 256GB MJLQ2LL/A RETINA
          globalId: EBAY-US
          condition:
            conditionDisplayName: New
            any: []
          viewItemURL: http://www.ebay.com/itm/NEW-SEALED-2015-APPLE-15-MACBOOK-PRO-2-2GHz-i7-16GB-256GB-MJLQ2LL-A-RETINA-/291555615515
          galleryURL: http://thumbs4.ebaystatic.com/m/mhNU6VAlmCSGcHK_Zq4QOzw/140.jpg
          galleryPlusPictureURL: [http://galleryplus.ebayimg.com/ws/web/291555615515_1_4_1.jpg]
          returnsAccepted: true
          subtitle: BRAND NEW FACTORY SEALED WITH FORCE TOUCH TRACKPAD!
          sellingStatus:
            currentPrice: value: 1789.0
            currentPrice: currencyId: USD
            convertedCurrentPrice: value: 1789.0
            convertedCurrentPrice: currencyId: USD
            getBidCount: null
            sellingState: Active
            timeLeft: 23 days 14:37:36
            any: []
          listingInfo:
            listingType: FixedPrice
            buyItNowAvailable: false
            buyItNowPrice: null
            bestOfferEnabled: false
            endTime: 04/30/2016 14:04:00 EDT
            any: []
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
            any: []
          primaryCategory:
            categoryName: Apple Laptops
            categoryId: 111422
          secondaryCategory:
          charityId: null
          productId: null
          paymentMethod: [PayPal]
          autoPay: true
          postalCode: null
          location: USA
          country: US
          storeInfo: null
          sellerInfo: null
          compatibility: null
          distance: null
          delimiter: null
          any: [[isMultiVariationListing: null], [discountPriceInfo: null], [topRatedListing: null]]
          ... [other results]...

# Command-line options

The command-line options allow to set constraints in the query sent to the
eBay server. For example, to return items only between a minimum and/or
maximum price, and/or condition (New, Used, etc), and/or which accept only
certain type of payment (like PayPal), etc.

These command-line options are not given by the program itself: they are
given by the underlying eBay Finding Kit for Enhaced Search SDK for Java
as item-filters, and what this program does is to expose all these
item-filters as possible command-line options to the user.

The format of these command-line options is:

        --option_name  value   [... --other_option_name value ...]   "keywords for item"

where `value` is the value given for the `option_name`: the interpretation
and validation of the `value` according to the `option_name` is left to the
underlying eBay Finding Kit for Enhaced Search SDK for Java, in the client-side,
which would report an exception if a `value` is invalid.

These are the possible `--option_name` that this program understands:

        --numb_items_to_return value
        --condition value
        --currency value
        --end_time_from value
        --mod_time_from value
        --end_time_to value
        --exclude_auto_pay value
        --best_offer_only value
        --featured_only value
        --feedback_score_max value
        --feedback_score_min value
        --free_shipping_only value
        --get_it_fast_only value
        --hide_duplicate_items value
        --available_to value
        --located_in value
        --local_pickup_only value
        --local_search_only value
        --listing_type value
        --lots_only value
        --max_bids value
        --min_bids value
        --max_price value
        --min_price value
        --payment_method value
        --max_quantity value
        --min_quantity value
        --seller value
        --exclude_seller value
        --exclude_category value
        --world_of_good_only value
        --max_distance value
        --seller_business_type value
        --top_rated_seller_only value
        --sold_items_only value
        --charity_only value
        --listed_in value
        --expedited_shipping_type value
        --max_handling_time value
        --returns_accepted_only value
        --value_box_inventory value
        --outlet_seller_only value
        --authorized_seller_only value
        --start_time_from value
        --start_time_to value

The option `--numb_items_to_return value` is the only option
processed by this program itself (ie., not passed to the
underlying eBay SDK).

When command-line options are given, the program interprets them all as
a single `AND`-conjunction, so each returned item must satisfy all of
these options. The only exception to this rule is when there appears
multiple times a same `--option_name`, in which case it is understood
as to request those items which have any of `value[i]` for
`option_name`. For example, in the request:

         ... --located_in US --located_in CN ...

it is understood those items whose location is either the USA or China,
but the rest of the command-line is a single `AND`-conjunction with
any of these values in `located_in`. (Or, alternatively, when a same
`--option_name` appears multiple times in the command-line, it may be
understood as set membership, e.g., in the previous example, to return
those items whose location is in the set { USA, China }, AND-ed with
any other option(s) which may also appear in the above command-line.)

As a more general example we may see:

        ./FindItem.scala --numb_items_to_return 10 --condition New \
                         --currency USD --min_price 5 --max_price 20  \
                         "lead holder 2 mm"

to query eBay for `lead holder 2mm`, in `New` condition, whose
prices are between 5 and 20 USD, and to request only 10 items.

Some options in the list above depend on the version of the eBay
Finding Kit for Enhaced Search SDK for Java that you are using,
so you may have less or more options available than those show
above: if you want to compile the latest version, with all the
current item filters (command-line options), please see the
section
[Updating the Finding Kit for Enhaced Search client-side JAR](#updating-the-finding-kit-for-enhaced-search-client-side-jar)
below in this page.

Since the command-line options depend on the version of the eBay
SDK you have, then some options may be missing (e.g., older
versions did not support or expose `--authorized_seller_only value`),
and this program only reflects all those underlying item filters and
does not explain, validate or interpret them, then this program
does not use a command-line parsing package for Scala, like `scopt`,
etc., which is useful in the more normal case where the program
controls its own command-line options (which is not the case here,
since this program only exposes all and whatever filters the
underlying eBay Finding Kit for Enhaced Search SDK supports).

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

# Debug Communication with the eBay Backend Service

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

eBay also offers `OutputSelectorType` as to what output
to return in the replies, and `AspectFilter`s, where the
aspects of an item have this role, as eBay says:

*[an aspect is the] characteristic of an item in a category.
For example, "Shoes Size" or "Style" might be aspects of
the Men's Shoes category, while "Genre" and "Album Type"
could be aspects of the Music CDs category.*

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

