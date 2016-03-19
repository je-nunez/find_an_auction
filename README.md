# Find an auction

Find items if you want to buy them from eBay

# Example:

To find for an auction for a new Apple 15 inches MacBook laptop:

     ./FindItem.scala  new apple macbook 15 laptop

          itemId: 291555615515
          title: NEW SEALED 2015 APPLE 15" MACBOOK PRO 2.2GHz i7 16GB 256GB MJLQ2LL/A RETINA
          globalId: EBAY-US
          condition: com.ebay.services.finding.Condition@43bc63a3
          viewItemURL: http://www.ebay.com/itm/NEW-SEALED-2015-APPLE-15-MACBOOK-PRO-2-2GHz-i7-16GB-256GB-MJLQ2LL-A-RETINA-/291555615515
          galleryURL: http://thumbs4.ebaystatic.com/m/mhNU6VAlmCSGcHK_Zq4QOzw/140.jpg
          subtitle: BRAND NEW FACTORY SEALED WITH FORCE TOUCH TRACKPAD!
          primaryCategory: com.ebay.services.finding.Category@702657cc
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
          sellingStatus: com.ebay.services.finding.SellingStatus@6a6cb05c
          listingInfo: com.ebay.services.finding.ListingInfo@40a4337a
          returnsAccepted: true
          galleryPlusPictureURL: [http://galleryplus.ebayimg.com/ws/web/291555615515_1_4_1.jpg]
          compatibility: null
          distance: null
          delimiter: null
          any: [[isMultiVariationListing: null], [discountPriceInfo: null], [topRatedListing: null]]

# Requirements

This program has been tested with Scala 2.11.6.

You need to create a free eBay API account:

     https://go.developer.ebay.com/what-ebay-api

Create your Application ID in eBay there.

You need to download the eBay Java SDK archive at:

     https://go.developer.ebay.com/javasdk

Unzip the archive and add the `src/lib/finding.jar`
and the `src/lib/log4j-1.2.16.jar` JAR files to
your CLASSPATH.

# Inspiration

The initial idea of this program is a Java sample
authored by boyang inside eBay Java SDK archive.

This SDK also supports more options, like the Trading
API (Buying, Selling and After Sale support), besides
the Finding API, which supports Searching for products
and items (besides Buying), and is the one we are
using here (we don't use Buying though, the Find gives
the URL where the auction can be seen). Details are
here:

   https://go.developer.ebay.com/api-features-comparison

There are APIs for other programming languages besides
Java (used in Scala through the JVM), here:

   https://go.developer.ebay.com/ebay-sdks
