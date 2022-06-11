package com.explorer.places.explorerservice.services.mapper;

import com.explorer.places.explorerservice.datasources.googlemap.GoogleMapScraper;
import com.explorer.places.explorerservice.datasources.groupon.GrouponApi;
import com.explorer.places.explorerservice.datasources.ticketMaster.TicketMasterApi;
import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.utils.Constants;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExploreAllMapper {

    public static Map<String, DataModel> mapAll(String lat, String lng, String category, String range) {
        Map<String, DataModel> result = GoogleMapScraper.googlePlaceData();

        return result;
    }

    public static Map<String, DataModel> mapGroupOnData(String lat, String lng, String category, String range) {
        Map<String, DataModel> result = new HashMap<>();

        String category_key = "filters";

        if (category.contains("things-to-do")) {
            result = GrouponApi.collectGroupOnData(getGrouponApiUrl(lat, lng, category_key, "category:".concat(category), range));

            Map<String, DataModel> getawayResult = GrouponApi.collectGroupOnData(getGrouponApiUrl(lat, lng, "categories", category, range));

            for (Map.Entry<String, DataModel> entry : getawayResult.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else {
            result = GrouponApi.collectGroupOnData(getGrouponApiUrl(lat, lng, category_key, "category:".concat(category), range));
        }

        return result;
    }

    public static Map<String, DataModel> getTicketMasterData(String lat, String lng, String range) {
        Map<String, DataModel> result = new HashMap<>();
        String url = getTicketMasterUrl(lat, lng, range);

        result = TicketMasterApi.collectTicketMasterData(url);
        return result;
    }


    public static String getTicketMasterUrl(String lat, String lng, String range) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 7);

        return UriComponentsBuilder.fromHttpUrl(Constants.TICKETMASTER_BASE_URL)
                .queryParam("latlong", lat + "," + lng)
                .queryParam("radius", range)
                .queryParam("endDateTime", sdf.format(c.getTime()))
                .build(true).toString();
    }

    public static String getGrouponApiUrl(String lat, String lng, String key, String category, String range) {
        return UriComponentsBuilder.fromHttpUrl(Constants.GROUPON_BASE_URL)
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .queryParam("radius", range)
                .queryParam("limit", 30)
                .queryParam(key, category)
                .build(true).toString();
    }
}


