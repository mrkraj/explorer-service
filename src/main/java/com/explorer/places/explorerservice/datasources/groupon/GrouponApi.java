package com.explorer.places.explorerservice.datasources.groupon;

import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.utils.CommonUtils;
import com.explorer.places.explorerservice.utils.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrouponApi {

    private static final Logger logger = LoggerFactory.getLogger(GrouponApi.class);

    public static String getGrouponApiUrl(String baseurl,String lat, String lng, String category, String range) {

        // https://www.groupon.com/browse?category=food-and-drink&lat=40.796&lng=-74.485&locale=en_US&distance=[0.0..10.0]
        return UriComponentsBuilder.fromHttpUrl(baseurl)
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .queryParam("locale", "en_US")
                .queryParam("distance", "%5B0.0..10.0%5D")
                .queryParam("category", category)
                .build(true).toString();
    }

    public static Map<String, DataModel> collectGroupOnData(String lat, String lng, String category, String range) {
        Map<String, DataModel> result = new HashMap<>();

        //scrape preloaded deals
        String dealsUrl =  getGrouponApiUrl(Constants.GROUPON_BASE_URL,lat, lng, category, range);
        result = scrapeGrouponDeals(dealsUrl);

        //Scrape lazy loaded deals
        String lazyLoadedDealsUrl = getGrouponApiUrl(Constants.GROUPON_LAZY_LOAD_BASE_URL,lat, lng, category, range);
        Map<String, DataModel> lazyLoadedDeals = scrapeLazyLoadedDeals(lazyLoadedDealsUrl);
        result.putAll(lazyLoadedDeals);
        return result;
    }

    public static Map<String, DataModel> scrapeLazyLoadedDeals(String url){
        String dealUrl = null, title = null, image = null, address = null, distanceValue = null, review = null, noOfReview = null, description = null,
                price = null, discountPrice = null, discount = null;
        Double distance = null;

        Map<String, DataModel> result = new HashMap<>();
        List<String> lazyLoadedDeals = new ArrayList<>();

        String startPoint = "<figure class";
        String endPoint = "</figure>";

        JSONObject jsonData = CommonUtils.getJsonResponse(url);

        String lazyLoadedData = jsonData.get("cardsHtml").toString();
        while(lazyLoadedData.contains(startPoint)){
            lazyLoadedDeals.add(lazyLoadedData.substring(lazyLoadedData.indexOf(startPoint), lazyLoadedData.indexOf(endPoint)+endPoint.length()));
            lazyLoadedData = lazyLoadedData.replace(lazyLoadedData.substring(lazyLoadedData.indexOf(startPoint), lazyLoadedData.indexOf(endPoint)+endPoint.length()),"");
        }

        for (String deals : lazyLoadedDeals){
            if (deals.contains("data-bhd=")){
                String stringJson = deals.substring(deals.indexOf("{"), deals.lastIndexOf("}")+1);
                if (!stringJson.isEmpty()){
                    String dealJson = stringJson.replace("&quot;","\"");
                    JSONObject json = new JSONObject(dealJson);
                    JSONObject body = (JSONObject) json.get("body");
                    try {
                        //title
                        if(body.has("section1")){
                            title = getContentFromJson(body.getJSONArray("section1"), "title");
                        }
                        //distance, address
                        if(body.has("section2")){
                            address = getContentFromJson(body.getJSONArray("section2"), "location-and-distance");
                            if (!address.isEmpty()){
                                String distanceString = address.substring(address.indexOf("(")+1,address.indexOf(")")).trim();
                                distance = Double.parseDouble(distanceString.split(" ")[0]);
                            }
                        }
                        //review, noOfReview
                        if(body.has("section3")){
                            JSONArray ratingObject = body.getJSONArray("section3");
                            for (int i=0; i<ratingObject.length(); i++){
                                if (ratingObject.getJSONObject(i).get("type").toString().equals("rating")){
                                   JSONObject ratingvalues = (JSONObject) ratingObject.getJSONObject(i).get("content");
                                   review = ratingvalues.get("numeric_value").toString();
                                   noOfReview = ratingvalues.get("count").toString();
                                }
                            }
                        }
                        //price, discount, discountPrice
                        if(body.has("section4")){
                            discount = getContentFromJson(body.getJSONArray("section4"), "discount_percentage").replace("OFF","");

                            String pricedetails = getContentFromJson(body.getJSONArray("section4"), "price");
                            String[] priceSplit = pricedetails.split(" ");

                            if (discount!= null){
                                price = priceSplit[0];
                                discountPrice = priceSplit[1];
                            }else {
                                price = priceSplit[1];
                            }
                        }
                        //description
                        if(body.has("section5")){
                            description = getContentFromJson(body.getJSONArray("section5"), "title");
                        }

                        //image
                        if(deals.contains("img.")){
                            image = "https:".concat(deals.substring(deals.indexOf("//img."), deals.indexOf("jpg")+3));
                        }

                        //dealUrl
                        if (deals.contains("https")) {
                            String dealsSubs = deals.substring(deals.indexOf("https"));
                            dealUrl = dealsSubs.substring(0, dealsSubs.indexOf("\""));
                        }

                        DataModel dataModel = new DataModel(title, address, image, null, review, noOfReview, null, null, description,
                                null, null, null, dealUrl, "GroupOn", description, null, price, discountPrice, discount, null, null, null, distance);

                        if (distance != null && dealUrl != null && image != null){
                            result.put(title, dataModel);
                        }
                    }catch (Exception ex){
                        logger.error("Exception at scraping the deals element ",ex.getMessage());
                    }
                }
            }
        }

        return result;
    }

    public static Map<String, DataModel> scrapeGrouponDeals(String url){
        Map<String, DataModel> result = new HashMap<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Element dealsContainer = doc.getElementById("pull-deal-feed");
            List<Element> dealsElements = dealsContainer.getElementsByClass("cui-content");
            dealsElements.stream().forEach(e -> {
                try {
                     String dealUrl = e.getElementsByTag("a").get(0).attr("href").trim();
                     String title = getTextFromElement(e.getElementsByClass("cui-udc-title"));
                     String imageSet = e.getElementsByClass("cui-image").get(0).toString().trim();
                     String image = "https:".concat(imageSet.substring(imageSet.indexOf("//img."), imageSet.indexOf("jpg")+3));
                     String address = getTextFromElement(e.getElementsByClass("cui-location-name"));
                     String distanceValue = getTextFromElement(e.getElementsByClass("cui-location-distance"));
                     Double distance = distanceValue != null ? Double.parseDouble(distanceValue.split(" ")[0]) : null;
                     String review = getTextFromElement(e.getElementsByClass("numeric-count"));
                     String numberReviews = getTextFromElement(e.getElementsByClass("rating-count"));
                     String noOfReview = numberReviews != null ? numberReviews.split(" ")[0] : null;
                     String description = getTextFromElement(e.getElementsByClass("cui-udc-subtitle"));
                     String price = getTextFromElement(e.getElementsByClass("cui-price-original"));
                     String discountPrice = getTextFromElement(e.getElementsByClass("cui-price-discount"));
                     String discount = getTextFromElement(e.getElementsByClass("cui-discount-badge"));

                     DataModel dataModel = new DataModel(title, address, image, null, review, noOfReview, null, null, description,
                                null, null, null, dealUrl, "GroupOn", description, null, price, discountPrice, discount, null, null, null, distance);

                     if (distance != null && dealUrl != null && image != null){
                         result.put(title, dataModel);
                     }
                }catch (Exception ex){
                    logger.error("Exception at scraping the deals element ",ex.getMessage());
                }
            });
        }catch (Exception e){
            logger.error("Exception at scraping Groupon data.",e.getMessage());
        }
        return result;
    }

    public static String getTextFromElement(Elements elements){
        if(elements.size()>0){
            if(!elements.get(0).text().isEmpty()){
                return elements.get(0).text().trim();
            }else{
                return null;
            }
        }
        return null;
    }

    public static String getContentFromJson(JSONArray jsonArray, String type){
         for (int i=0; i<jsonArray.length(); i++){
             if (jsonArray.getJSONObject(i).get("type").toString().equals(type)){
                 return jsonArray.getJSONObject(i).get("content").toString();
             }
         }
         return null;
    }

    public static DataModel getGrouponDataModel(JSONObject data) {
        DataModel entries = null;
        String title = null, description = null, priceRange = null, address = null, review = null,
                noOfReview = null, open = null, closing = null, image = null, latitude = null, longitude = null, url = null,
                source = null, offerTitle = null, value = null, price = null,
                discountPrice = null, discount = null;
        String[] category = null;

        JSONObject options = data.getJSONArray("options").getJSONObject(0);
        JSONObject addressObject = options.getJSONArray("redemptionLocations").length() > 0 ? options.getJSONArray("redemptionLocations").getJSONObject(0) : null;
        JSONObject merchant = data.getJSONObject("merchant");

        title = merchant.getString("name");
        description = data.get("announcementTitle").toString();
        image = data.getString("largeImageUrl");
        url = data.getString("dealUrl");
        source = data.getString("type");
        open = options.getString("status");

        if (addressObject != null) {
            address = addressObject.get("streetAddress1").toString().concat(", ").
                    concat(addressObject.get("city").toString().concat(", ")).
                    concat(addressObject.get("state").toString());

            latitude = addressObject.get("lat").toString();
            longitude = addressObject.get("lng").toString();
        }

        if (!merchant.isNull("recommendations")) {
            review = merchant.getJSONArray("recommendations").getJSONObject(0).get("rating").toString();
            noOfReview = merchant.getJSONArray("recommendations").getJSONObject(0).getString("totalMessage");
        }

        offerTitle = options.has("title") ? options.getString("title") : null;

        value = options.has("value") ? options.getJSONObject("value").getString("formattedAmount") : null;
        price = options.has("regularPrice") ? options.getJSONObject("regularPrice").getString("formattedAmount") : null;
        discountPrice = options.has("price") ? options.getJSONObject("price").getString("formattedAmount") : null;

        discount = options.has("discountPercent") ? options.get("discountPercent").toString() : null;

        JSONArray tags = data.getJSONArray("tags");
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < tags.length(); i++) {
            categories.add(tags.getJSONObject(i).getString("name"));
        }
        category = categories.toArray(new String[categories.size()]);


        if (latitude != null) {
            entries = new DataModel(title, address, image, category, review, noOfReview, open, null, description,
                    null, latitude, longitude, url, source, offerTitle, value, price, discountPrice, discount, null, null, null, null);
        }

        return entries;
    }

}
