package com.explorer.places.explorerservice.datasources.googlemap;

import com.explorer.places.explorerservice.models.DataModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMapData {
    static final Logger logger = LoggerFactory.getLogger(GoogleMapData.class);

    public static Map<String, DataModel> googlePlaceData(String url) {
        logger.info("Google URL-" + url);
        return extractGoogleJson(url);
    }

    public static Map<String, DataModel> extractGoogleJson(String url) {
        Map<String, DataModel> result = new HashMap<>();
        try {
            String baseUrl = "https://www.google.com/maps/place/";
            String startKey = ")]}'\n";
            String startPoint = "window.APP_INITIALIZATION_STATE=";
            String endPoint = ";window.APP_FLAGS";
            Document doc = Jsoup.connect(url).get();
            String scriptString = doc.head().getElementsByTag("script").toString();
            String scriptStringJson = scriptString.substring(scriptString.indexOf(startPoint) + startPoint.length(), scriptString.indexOf(endPoint));
            JSONArray scriptJson = new JSONArray(scriptStringJson);
            if (!scriptJson.isEmpty()){
                String resultString = scriptJson.getJSONArray(3).get(2).toString().replace(startKey, "");
                JSONArray listItemsJson = new JSONArray(resultString).getJSONArray(0).getJSONArray(1);
                for (int i = 1; i < listItemsJson.length(); i++) {
                    JSONArray itemJson = listItemsJson.getJSONArray(i).getJSONArray(14);

                    String title = itemJson.get(11).toString();
                    String address = itemJson.get(39).toString();

                    //Image URL
                    String imageUrl = itemJson.getJSONArray(37).getJSONArray(0).getJSONArray(0).getJSONArray(6).get(0).toString();
                    if (imageUrl.contains("lh5.googleusercontent.com") && imageUrl.contains("=")){
                        imageUrl = imageUrl.substring(0, imageUrl.indexOf("=")+1).concat("w300-h200-k-no");
                    }
                    //Category
                    String[] category = null;
                    if (!itemJson.isNull(13)){
                        JSONArray categoryArray = itemJson.getJSONArray(13);
                        if (!categoryArray.isEmpty()){
                            List<String> categoryList = new ArrayList<>();
                            for (int j = 0; j < categoryArray.length(); j++) {
                                categoryList.add(categoryArray.get(j).toString());
                            }
                            if (!categoryList.isEmpty()){
                                category = categoryList.toArray(new String[categoryList.size()]);
                            }
                        }
                    }

                    String reviewStars = null; String noOfReviews = null; String priceRange = null;
                    if (!itemJson.isNull(4)){
                        reviewStars = itemJson.getJSONArray(4).get(7).toString();
                        noOfReviews = itemJson.getJSONArray(4).get(8).toString();
                        priceRange = itemJson.getJSONArray(4).get(2).toString();
                    }

                    String openState = null; String closeState = null;
                    if (!itemJson.isNull(34)){
                        openState = itemJson.getJSONArray(34).getJSONArray(4).get(4).toString();
                    }

                    String description = itemJson.getJSONArray(88).get(0).toString();

                    String latitude = null; String longitude = null;
                    if (!itemJson.isNull(9)){
                        latitude = itemJson.getJSONArray(9).get(2).toString();
                        longitude = itemJson.getJSONArray(9).get(3).toString();
                    }

                    //Goto URL
                    String gotoUrl = null;
                    if (latitude != null && longitude != null &&  itemJson.get(10).toString() != null){
                        gotoUrl = baseUrl + "@" + latitude + "," + longitude + ",17z/data=!4m5!3m4!1s" +
                                itemJson.get(10).toString() + "!8m2!3d" + latitude + "!4d" + longitude;
                    }

                    if (title != null && address != null && imageUrl != null && latitude != null && longitude != null) {
                        result.put(title,new DataModel(title, address, imageUrl, category, reviewStars, noOfReviews, openState,
                                null, description, priceRange, latitude, longitude, gotoUrl,
                                "google-map", null, null, null, null,
                                null, null, null, null, null));
                    }
                }
            }
        }catch (Exception e){
            logger.error("Exception at getting the Google data, w/ error -", e.getMessage());
        }
        return result;
    }
}
