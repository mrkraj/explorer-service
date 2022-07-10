package com.explorer.places.explorerservice.datasources.groupon;

import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.utils.CommonUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrouponApi {

    private static final Logger logger = LoggerFactory.getLogger(GrouponApi.class);

    public static Map<String, DataModel> collectGroupOnData(String url) {
        Map<String, DataModel> result = new HashMap<>();

        JSONObject jsonData = CommonUtils.getJsonResponse(url);
        JSONArray deals = (JSONArray) jsonData.get("deals");
        for (int i = 0; i < deals.length(); i++) {
            DataModel tempData = getGrouponDataModel(deals.getJSONObject(i));
            if (tempData != null) {
                result.put(tempData.getTitle(), tempData);
            }
        }
        return result;
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
                    null, latitude, longitude, url, source, offerTitle, value, price, discountPrice, discount, null, null, null);
        }

        return entries;
    }

}
