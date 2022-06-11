package com.explorer.places.explorerservice.services;


import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.services.mapper.ExploreAllMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ExploreAll {

    @GetMapping("/data/exploreAll")
    Map<String, DataModel> googlePlaceData(@RequestParam String lat,
                                           @RequestParam String lng,
                                           @RequestParam String category,
                                           @RequestParam String range) {
        return ExploreAllMapper.mapAll(lat, lng, category, range);
    }

    @GetMapping("/data/grouponApi")
    Map<String, DataModel> groupOnApi(@RequestParam String lat,
                                      @RequestParam String lng,
                                      @RequestParam String category,
                                      @RequestParam String range) {
        return ExploreAllMapper.mapGroupOnData(lat, lng, category, range);
    }

    @GetMapping("/data/ticketMasterApi")
    Map<String, DataModel> ticketMasterApi(@RequestParam String lat,
                                           @RequestParam String lng,
                                           @RequestParam String category,
                                           @RequestParam String range) {
        return ExploreAllMapper.getTicketMasterData(lat, lng, range);
    }
}
