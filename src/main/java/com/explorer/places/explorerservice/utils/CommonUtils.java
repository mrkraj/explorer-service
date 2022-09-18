package com.explorer.places.explorerservice.utils;

import com.explorer.places.explorerservice.models.DataModel;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Map;

public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static JSONObject getJsonResponse(String link) {
        JSONObject json = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Mozilla/4.0");
            inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            inputStream.close();
            String jsonText = sb.toString();
            json = new JSONObject(jsonText);
        } catch (Exception e) {
            logger.error("Exception at getting json response from URL: {} w/ error: {}", link, e.getMessage());
        }
        return json;
    }

    public static void calculateDistance(double lat1, double lng1, Map<String, DataModel> items) {
        double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
        DecimalFormat df = new DecimalFormat("#.#");

        items.entrySet().stream().forEach(e -> {
            if (e.getValue().getLatitude() != null){
                double lat2 = e.getValue().getLatitude();
                double lng2 = e.getValue().getLongitude();

                double dLat = Math.toRadians(lat2 - lat1);
                double dLng = Math.toRadians(lng2 - lng1);
                double sindLat = Math.sin(dLat / 2);
                double sindLng = Math.sin(dLng / 2);
                double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                        * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(e.getValue().getLatitude()));
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double dist = earthRadius * c;
                dist = dist > 0.1 ? dist : 0.1;
                e.getValue().setDistance(Double.parseDouble(df.format(dist)));
            }
        });
    }
}
