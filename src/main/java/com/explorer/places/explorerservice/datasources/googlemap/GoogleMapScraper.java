package com.explorer.places.explorerservice.datasources.googlemap;

import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.utils.ScraperUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMapScraper {
    static final Logger logger = LoggerFactory.getLogger(GoogleMapScraper.class);
    private static final String MAP_URL1 = "https://www.google.com/maps/search/things+to+do+near+morristown+nj";
    private static final String MAP_URL = "https://www.google.com/maps/search/restaurants+near+union+city+nj";

    public static Map<String, DataModel> googlePlaceData() {
        return scrapper();
    }

    public static Map<String, DataModel> scrapper() {
        WebDriver driver = null;
        Map<String, DataModel> result = new HashMap<>();
        try {
            WebDriverManager.chromedriver().setup();

            driver = new ChromeDriver();
            driver.get(MAP_URL1);

            result = iterateElements(driver, result);

            // Click Next button, load next button while scraping.
            try {
                WebElement nextPage = ((ChromeDriver) driver).findElementByCssSelector("button[aria-label=' Next page ']");
                while (result.size() < 31 && nextPage != null && nextPage.isEnabled()) {
                    nextPage.click();
                    result = iterateElements(driver, result);
                }
            } catch (Exception e) {
                logger.error("Exception at loading next page w/ error {}", e.getStackTrace());
            }

        } catch (Exception e) {
            logger.error("Exception at opening the Selenium Web Driver w/ error: {}", e.getMessage());
        } finally {
            if (driver != null) {
                driver.close();
            }
        }

        return result;
    }

    public static Map<String, DataModel> iterateElements(WebDriver driver, Map<String, DataModel> result) {
        try {
            Long startTime = System.currentTimeMillis();
            ScraperUtils.scrollToBottom(driver);

            List<WebElement> nextSections = driver.findElements(By.className("Nv2PK"));
            for (WebElement web : nextSections) {
                DataModel dataModel = ScraperUtils.scrapePlaces(web);
                if (dataModel != null) {
                    result.put(dataModel.getTitle(), dataModel);
                }
            }

            logger.info("Iterator Time {}", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            logger.error("Exception at Iterating the elements w/ error: {}", e.getMessage());
        }
        return result;
    }

}