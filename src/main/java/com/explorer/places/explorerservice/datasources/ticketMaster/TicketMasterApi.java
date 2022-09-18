package com.explorer.places.explorerservice.datasources.ticketMaster;

import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.utils.CommonUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TicketMasterApi {

    private static final Logger logger = LoggerFactory.getLogger(TicketMasterApi.class);

    public static Map<String, DataModel> collectTicketMasterData(String url) {
        Map<String, DataModel> result = new HashMap<>();

        JSONObject jsonData = CommonUtils.getJsonResponse(url);
        JSONArray events = jsonData.has("_embedded") ? jsonData.getJSONObject("_embedded").getJSONArray("events") : new JSONArray();
        for (int i = 0; i < events.length(); i++) {
            DataModel tempData = getTicketMasterDataModel(events.getJSONObject(i));
            if (tempData != null && !result.containsKey(tempData.getTitle())) {
                result.put(tempData.getTitle(), tempData);
            }
            if (tempData != null && result.containsKey(tempData.getTitle()) && tempData.getDate() != null &&
                    result.get(tempData.getTitle()).getDate().compareTo(tempData.getDate()) > 0) {
                result.put(tempData.getTitle(), tempData);
            }
        }
        return result;
    }

    public static DataModel getTicketMasterDataModel(JSONObject data) {
        DataModel entries = null;
        try{
            String title = null, description = null, priceRange = null, address = null, review = null,
                    noOfReview = null, open = null, closing = null, image = null, latitude = null, longitude = null, url = null,
                    source = null, offerTitle = null, value = null, price = null,
                    discountPrice = null, discount = null, type = null, date = null, time = null;

            JSONObject venue = data.getJSONObject("_embedded").getJSONArray("venues").getJSONObject(0);
            JSONObject tags = data.has("classifications") ? data.getJSONArray("classifications").getJSONObject(0) : null;
            JSONObject startDate = data.has("dates") ? data.getJSONObject("dates").getJSONObject("start") : null;
            JSONObject priceData = data.has("priceRanges") ? data.getJSONArray("priceRanges").getJSONObject(0) : null;

            title = data.getString("name");
            type = data.getString("type");
            image = data.getJSONObject("_embedded").getJSONArray("attractions").getJSONObject(0).
                    getJSONArray("images").getJSONObject(0).getString("url");
            url = data.getString("url");
            source = "ticketMaster";

            if (venue != null) {
                description = venue.getString("name");

                address = venue.getJSONObject("address").getString("line1").concat(", ").
                        concat(venue.getJSONObject("city").getString("name").concat(", ")).
                        concat(venue.getJSONObject("state").getString("stateCode"));

                latitude = venue.getJSONObject("location").getString("latitude");
                longitude = venue.getJSONObject("location").getString("longitude");
            }

            if (startDate != null) {
                date = startDate.has("localDate") ? startDate.getString("localDate") : null;
                time = startDate.has("localTime") ? startDate.getString("localTime") : null;
                try {
                    DateFormat f1 = new SimpleDateFormat("HH:mm:ss");
                    Date d = f1.parse(time);
                    DateFormat f2 = new SimpleDateFormat("h:mma");
                    time = f2.format(d).toLowerCase();
                }catch (Exception e){
                    logger.error("Exception at formating time {} with error message- {}",time,e.getMessage());
                }
            }

            if (priceData != null) {
                price = priceData.get("min").toString();
            }

            if (latitude != null) {
                entries = new DataModel(title, address, image, getTicketMasterCategory(tags), review, noOfReview, open, null, description,
                        null, latitude, longitude, url, source, offerTitle, value, price, discountPrice, discount, type, date, time,null);
            }
        }catch (Exception e){
            logger.error("Exception at creating ticketmaster data model");
        }
        return entries;
    }

    public static String[] getTicketMasterCategory(JSONObject tags) {
        List<String> categories = new ArrayList<>();
        if (tags != null) {
            if (tags.has("segment")) {
                categories.add(tags.getJSONObject("segment").getString("name"));
            }
            if (tags.has("genre")) {
                categories.add(tags.getJSONObject("genre").getString("name"));
            }
            if (tags.has("subGenre")) {
                categories.add(tags.getJSONObject("subGenre").getString("name"));
            }
            if (tags.has("type") && tags.getJSONObject("type").getString("name") != "Undefined") {
                categories.add(tags.getJSONObject("type").getString("name"));
            }
            if (tags.has("subType") && tags.getJSONObject("subType").getString("name") != "Undefined") {
                categories.add(tags.getJSONObject("subType").getString("name"));
            }
        }
        return categories.toArray(new String[categories.size()]);
    }

}
