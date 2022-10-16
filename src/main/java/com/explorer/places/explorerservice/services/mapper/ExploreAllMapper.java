package com.explorer.places.explorerservice.services.mapper;

import com.explorer.places.explorerservice.datasources.googlemap.GoogleMapData;
import com.explorer.places.explorerservice.datasources.googlemap.GoogleMapScraper;
import com.explorer.places.explorerservice.datasources.groupon.GrouponApi;
import com.explorer.places.explorerservice.datasources.ticketMaster.TicketMasterApi;
import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ExploreAllMapper {
    private final Logger logger = LoggerFactory.getLogger(ExploreAllMapper.class);

    @Async
    public CompletableFuture<Map<String, DataModel>> getGoogleMapData(String lat, String lng, String category, String range) {
        logger.info("started googlemap:-", System.currentTimeMillis());
        String url = "";
        if (category.contains("things-to-do")) {
            url = "https://www.google.com/maps/search/things+to+do+near+me+within+" + range + "+miles/@" + lat + "," + lng;

        } else {
            url = "https://www.google.com/maps/search/restaurants+near+me+within+" + range + "+miles/@" + lat + "," + lng;
        }

        Map<String, DataModel> result = GoogleMapData.googlePlaceData(url);

        logger.info("ended googlemap :-", System.currentTimeMillis());
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Map<String, DataModel>> getGroupOnData(String lat, String lng, String category, String range) {
        Map<String, DataModel> result = new HashMap<>();
        logger.info("started groupon :-", System.currentTimeMillis());

        String category_key = "filters";

        if (category.contains("things-to-do")) {
            result = GrouponApi.collectGroupOnData(lat, lng, "things-to-do", range);
        } else {
            result = GrouponApi.collectGroupOnData(lat, lng,"food-and-drink", range);
        }
        logger.info("ended groupon :-", System.currentTimeMillis());

        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Map<String, DataModel>> getTicketMasterData(String lat, String lng, String range) {
        logger.info("started ticketmaster :-", System.currentTimeMillis());

        Map<String, DataModel> result = new HashMap<>();
        String url = getTicketMasterUrl(lat, lng, range);

        result = TicketMasterApi.collectTicketMasterData(url);
        logger.info("ended ticketmaster :-", System.currentTimeMillis());

        return CompletableFuture.completedFuture(result);
    }


    public String getTicketMasterUrl(String lat, String lng, String range) {

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
}


