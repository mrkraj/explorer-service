package com.explorer.places.explorerservice.services.mapper;

import com.explorer.places.explorerservice.datasources.googlemap.GoogleMapScraper;
import com.explorer.places.explorerservice.datasources.groupon.GrouponApi;
import com.explorer.places.explorerservice.datasources.utils.Constants;
import com.explorer.places.explorerservice.models.DataModel;
import org.springframework.web.util.UriComponentsBuilder;

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
            result = GrouponApi.collectGroupOnData(getGrouponApiUrl(lat, lng, category_key, "category:" .concat(category), range));

            Map<String, DataModel> getawayResult = GrouponApi.collectGroupOnData(getGrouponApiUrl(lat, lng, "categories", category, range));

            for (Map.Entry<String, DataModel> entry : getawayResult.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else {
            result = GrouponApi.collectGroupOnData(getGrouponApiUrl(lat, lng, category_key, "category:" .concat(category), range));
        }

        return result;
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


