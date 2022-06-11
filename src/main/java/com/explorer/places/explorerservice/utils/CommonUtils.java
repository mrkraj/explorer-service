package com.explorer.places.explorerservice.utils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static JSONObject getJsonResponse(String url) {
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
            logger.error("Exception at getting json response from URL: {} w/ error: {}", url, e.getMessage());
        }
        return json;
    }

    public static Date convertStringToDate(String date){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (!StringUtils.isEmpty(date)){
            try {
                return formatter.parse(date);
            }catch (Exception e){
                logger.error("Exception at parsing the Date from string - {}, w/ error: {}", date, e.getMessage());
            }
        }
        return null;
    }
}
