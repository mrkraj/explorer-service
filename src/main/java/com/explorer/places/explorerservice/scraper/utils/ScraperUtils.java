package com.explorer.places.explorerservice.scraper.utils;

import com.explorer.places.explorerservice.models.DataModel;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScraperUtils {
    private static final Logger logger = LoggerFactory.getLogger(ScraperUtils.class);


    public static DataModel scrapePlaces(WebElement web) {
        DataModel entries = null;
        String title = null, description = null, priceRange = null, address = null, category = null, review = null, noOfReview = null, open = null, closing = null, image = null;

        try {
            title = web.findElement(By.className("qBF1Pd")) != null ? web.findElement(By.className("qBF1Pd")).getText() : null;
            review = web.findElement(By.className("MW4etd")) != null ? web.findElement(By.className("MW4etd")).getText() : null;
            noOfReview = web.findElement(By.className("UY7F9")) != null ? web.findElement(By.className("UY7F9")).getText() : null;
            image = web.findElement(By.className("FQ2IWe")).findElement(By.tagName("img")).getAttribute("src");
            priceRange = web.findElement(By.cssSelector("span[aria-label='Price: Moderate']")) != null ? web.findElement(By.cssSelector("span[aria-label='Price: Moderate']")).getText() : null;


            Optional<WebElement> optionalDetails = web.findElements(By.className("W4Efsd")).stream().filter(entry -> entry.getText().contains("\n")).findFirst();
            if (optionalDetails.isPresent()) {
                WebElement details = optionalDetails.get();
                if (details != null) {

                    String[] detailsString = details.getText().split("\n");

                    String[] categorydetail = detailsString[0].split("·");
                    if (categorydetail.length > 1) {
                        category = categorydetail[0];
                        address = categorydetail[1];
                    }

                    String timingString = "";
                    if (detailsString.length > 1) {
                        String temp = detailsString[1].toUpperCase();
                        if (temp.contains("AM") || temp.contains("PM") || temp.contains("OPEN") || temp.contains("CLOSED")) {
                            timingString = detailsString[1];
                        } else {
                            description = detailsString.length > 1 ? detailsString[1] : null;
                        }
                    }

                    String[] timing = detailsString.length > 2 ? detailsString[2].split("⋅") : timingString.split("⋅");
                    if (timing != null && timing.length > 0) {
                        open = timing[0];
                        if (timing.length > 1) {
                            closing = timing[1];
                        }
                    }

                    //Build Place Data Model.
                    if (title != null && address != null && category != null && image != null) {
                        entries = new DataModel(title, address, image, category, review, noOfReview, open, closing, description, priceRange);
                    }

                }
            } else {
                logger.debug("Detail section with class Name 'W4Efsd' Notexist/Changed. !!!CRITICAL!!!");
            }
        } catch (Exception e) {
            logger.error("Exception at scraping the Google map w/ error: {}", e.getStackTrace());
        }
        return entries;
    }
}
