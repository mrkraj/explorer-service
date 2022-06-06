package com.explorer.places.explorerservice.datasources.groupon;

import com.explorer.places.explorerservice.datasources.utils.Constants;
import com.explorer.places.explorerservice.datasources.utils.ScraperUtils;
import com.explorer.places.explorerservice.models.DataModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrouponApi {

    private static final Logger logger = LoggerFactory.getLogger(ScraperUtils.class);


    //Getaway Things To Do.
    String getaways1 = "https://partner-api.groupon.com/deals.json?tsToken=US_AFF_0_6162194_212556_0&lat=40.7954198&lng=-74.4794881&radius=10&limit=20&categories=things-to-do";

    public static Map<String, DataModel> collectGroupOnData(String url){
        Map<String, DataModel> result = new HashMap<>();

        JSONObject jsonData = getGrouponData(url);
        JSONArray deals = (JSONArray) jsonData.get("deals");
        for (int i=0; i< deals.length(); i++){
            DataModel tempData = getGrouponDataModel(deals.getJSONObject(i));
            result.put(tempData.getTitle(), tempData);
        }
        return result;
    }

    public static DataModel getGrouponDataModel(JSONObject data){
        DataModel entries = null;
        String title = null, description = null, priceRange = null, address = null, review = null,
                noOfReview = null, open = null, closing = null, image = null, latitude = null, longitude = null, url = null,
                source = null, offerTitle =null, value = null, price = null,
                discountPrice = null, discount = null;
        String[] category = null;

        JSONObject options = data.getJSONArray("options").getJSONObject(0);
        JSONObject addressObject = options.getJSONArray("redemptionLocations").length() > 0 ? options.getJSONArray("redemptionLocations").getJSONObject(0) : null;
        JSONObject merchant = data.getJSONObject("merchant");

        title = merchant.getString("name");
        description = data.get("announcementTitle").toString();
        image = data.getString("mediumImageUrl");
        url = data.getString("dealUrl");
        source = data.getString("type");
        open = options.getString("status");

        if (addressObject != null){
            address = addressObject.get("streetAddress1").toString().concat(", ").
                    concat( addressObject.get("city").toString().concat(", ")).
                    concat( addressObject.get("state").toString());

            latitude = addressObject.get("lat").toString();
            longitude = addressObject.get("lng").toString();
        }

        if (!merchant.isNull("recommendations")){
            review = merchant.getJSONArray("recommendations").getJSONObject(0).get("rating").toString();
            noOfReview = merchant.getJSONArray("recommendations").getJSONObject(0).getString("totalMessage");
        }

        offerTitle = options.has("title")? options.getString("title") : null;

        value = options.has("value") ? options.getJSONObject("value").getString("formattedAmount"): null;
        price = options.has("regularPrice") ? options.getJSONObject("regularPrice").getString("formattedAmount") : null;
        discountPrice = options.has("price") ? options.getJSONObject("price").getString("formattedAmount"): null;

        discount = options.has("discountPercent") ? options.get("discountPercent").toString() : null;

        JSONArray tags = data.getJSONArray("tags");
        List<String> categories = new ArrayList<>();
        for (int i=0; i<tags.length();i++) {
            categories.add(tags.getJSONObject(i).getString("name"));
        }
        category = categories.toArray(new String[categories.size()]);


        entries = new DataModel(title,address,image,category,review,noOfReview,open,null,description,
                null,latitude,longitude,url,source,offerTitle,value,price,discountPrice,discount);

        return entries;
    }

    public static JSONObject getGrouponData(String url){
        JSONObject json = null;
        try {
            InputStream is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            json = new JSONObject(jsonText);
            is.close();
        } catch (Exception e) {
            logger.error("Exception at getting GroupOn Data from URL: {} w/ error: {}", url, e.getMessage());
        }
        return json;
    }

}
