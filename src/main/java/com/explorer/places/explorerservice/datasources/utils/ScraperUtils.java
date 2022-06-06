package com.explorer.places.explorerservice.datasources.utils;

import com.explorer.places.explorerservice.models.DataModel;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScraperUtils {
    private static final Logger logger = LoggerFactory.getLogger(ScraperUtils.class);


    public static DataModel scrapePlaces(WebElement web) {
        DataModel entries = null;
        String title = null, description = null, priceRange = null, address = null, review = null,
                noOfReview = null, open = null, closing = null, image = null, latitude = null, longitude = null, url = null;
        String[] category = null;

        try {
            title = web.findElement(By.className("qBF1Pd")) != null ? web.findElement(By.className("qBF1Pd")).getText() : null;
            review = web.findElement(By.className("MW4etd")) != null ? web.findElement(By.className("MW4etd")).getText() : null;
            noOfReview = web.findElement(By.className("UY7F9")) != null ? web.findElement(By.className("UY7F9")).getText() : null;
            image = web.findElement(By.className("FQ2IWe")).findElement(By.tagName("img")).getAttribute("src");
            Optional<WebElement> priceElement = web.findElements(By.cssSelector("span[aria-label]")).stream().filter(entry -> entry.getAttribute("aria-label").contains("Price:")).findFirst();
            priceRange = priceElement.isPresent() ? priceElement.get().getText() : null;


            Optional<WebElement> optionalDetails = web.findElements(By.className("W4Efsd")).stream().filter(entry -> entry.getText().contains("\n")).findFirst();
            if (optionalDetails.isPresent()) {
                WebElement details = optionalDetails.get();
                if (details != null) {

                    String[] detailsString = details.getText().split("\n");

                    String[] categorydetail = detailsString[0].split("·");
                    if (categorydetail.length > 1) {
                        category = new String[]{categorydetail[0]};
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

                    //Extract lat/long from achortag.
                    WebElement goToElement = web.findElement(By.tagName("a"));
                    url = goToElement != null ? goToElement.getAttribute("href") : null;

                    String[] latLong = scrapeLatLong(web, url);
                    if (latLong != null) {
                        latitude = latLong[0];
                        longitude = latLong[1];
                    }

                    //Build Place Data Model.
                    if (title != null && address != null && category != null && image != null) {
                        entries = new DataModel(title, address, image, category, review, noOfReview, open,
                                closing, description, priceRange, latitude, longitude, url,
                                null, null, null, null, null,
                                null);
                    }

                }
            } else {
                logger.debug("Detail section with class Name 'W4Efsd' Notexist/Changed. !!!CRITICAL!!!");
            }
        } catch (Exception e) {
            logger.error("Exception at scraping the Google map w/ error: {}", e.getMessage());
        }
        return entries;
    }

    public static void scrollToBottom(WebDriver driver) {
        try {
            Long startTime = System.currentTimeMillis();

            String script = "return document.getElementsByClassName('ecceSd')[1].scrollHeight";
            int height = 0;
            //while (Integer.parseInt(((JavascriptExecutor) driver).executeScript(script).toString()) > height){
            for (int i = 0; i < 5; i++) {
                height = Integer.parseInt(((JavascriptExecutor) driver).executeScript(script).toString());
                String scrollScript = "document.getElementsByClassName('ecceSd')[1].scrollBy(0, document.getElementsByClassName('ecceSd')[1].scrollHeight)";
                ((JavascriptExecutor) driver).executeScript(scrollScript);
                TimeUnit.MILLISECONDS.sleep(500);
            }
            logger.info("Scroll Time {}", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            logger.error("Exception at Scrolling to bottom w/ error {}", e.getMessage());
        }
    }

    public static String[] scrapeLatLong(WebElement web, String href) {
        if (!StringUtils.isEmpty(href)) {
            String latLongString = href.substring(href.indexOf("!3d") + 3, href.indexOf("!16"));
            if (!StringUtils.isEmpty(latLongString)) {
                return latLongString.split("!4d-");
            }
        }
        return null;
    }
}
