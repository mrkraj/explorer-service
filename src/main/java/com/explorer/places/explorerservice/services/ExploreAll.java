package com.explorer.places.explorerservice.services;


import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.services.mapper.ExploreAllMapper;
import com.explorer.places.explorerservice.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
public class ExploreAll {

    @Autowired
    private ExploreAllMapper exploreAllMapper;

    @GetMapping("/data/exploreAll")
    List<DataModel> exploreAllData(@RequestParam String lat,
                                   @RequestParam String lng,
                                   @RequestParam String category,
                                   @RequestParam String range) throws ExecutionException, InterruptedException {
        Map<String, DataModel> result = new HashMap<>();

        CompletableFuture googleData = exploreAllMapper.getGoogleMapData(lat, lng, category, range);
        //CompletableFuture groupOnData = exploreAllMapper.getGroupOnData(lat, lng, category, range);
        CompletableFuture ticketMasterData = exploreAllMapper.getTicketMasterData(lat, lng, range);

        //this like wait for all async methods to finish.
        CompletableFuture.allOf(googleData, ticketMasterData).join();

        result.putAll((Map<? extends String, ? extends DataModel>) googleData.get());
        //result.putAll((Map<? extends String, ? extends DataModel>) groupOnData.get());
        result.putAll((Map<? extends String, ? extends DataModel>) ticketMasterData.get());

        CommonUtils.calculateDistance(Double.parseDouble(lat), Double.parseDouble(lng), result);
        return result.values().stream().collect(Collectors.toList());

    }

    @GetMapping("/data/googleMapApi")
    Map<String, DataModel> googlePlaceData(@RequestParam String lat,
                                           @RequestParam String lng,
                                           @RequestParam String category,
                                           @RequestParam String range) throws ExecutionException, InterruptedException {

        CompletableFuture googleData = exploreAllMapper.getGoogleMapData(lat, lng, category, range);
        CommonUtils.calculateDistance(Double.parseDouble(lat), Double.parseDouble(lng), (Map<String, DataModel>) googleData.get());
        return (Map<String, DataModel>) googleData.get();
    }


    @GetMapping("/data/grouponApi")
    Map<String, DataModel> groupOnApi(@RequestParam String lat,
                                      @RequestParam String lng,
                                      @RequestParam String category,
                                      @RequestParam String range) throws ExecutionException, InterruptedException {

        CompletableFuture groupOnData = exploreAllMapper.getGroupOnData(lat, lng, category, range);
        CommonUtils.calculateDistance(Double.parseDouble(lat), Double.parseDouble(lng), (Map<String, DataModel>) groupOnData.get());
        return (Map<String, DataModel>) groupOnData.get();
    }

    @GetMapping("/data/ticketMasterApi")
    Map<String, DataModel> ticketMasterApi(@RequestParam String lat,
                                           @RequestParam String lng,
                                           @RequestParam String category,
                                           @RequestParam String range) throws ExecutionException, InterruptedException {

        CompletableFuture ticketMasterData = exploreAllMapper.getTicketMasterData(lat, lng, range);
        CommonUtils.calculateDistance(Double.parseDouble(lat), Double.parseDouble(lng), (Map<String, DataModel>) ticketMasterData.get());
        return (Map<String, DataModel>) ticketMasterData.get();
    }
}
