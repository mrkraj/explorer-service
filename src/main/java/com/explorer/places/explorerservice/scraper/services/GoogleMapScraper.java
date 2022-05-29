package com.explorer.places.explorerservice.scraper.services;

import com.explorer.places.explorerservice.models.DataModel;
import com.explorer.places.explorerservice.scraper.utils.ScraperUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class GoogleMapScraper {
    private static final String MAP_URL = "https://www.google.com/maps/search/things+to+do+near+morristown+nj";
    private static final String MAP_URL1 = "https://www.google.com/maps/search/restaurants+near+morristown+nj";

    Logger logger = LoggerFactory.getLogger(GoogleMapScraper.class);

    @GetMapping("/googleScrape")
    Map<String, DataModel> googlePlaceData() {
        return scrapper();
    }

    public Map<String, DataModel> scrapper() {
        WebDriver driver = null;
        Map<String, DataModel> result = new HashMap<>();
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.get(MAP_URL1);
            int i = 0;

            List<WebElement> sections = driver.findElements(By.className("bfdHYd"));
            for (WebElement web : sections) {
                DataModel dataModel = ScraperUtils.scrapePlaces(web);
                if (dataModel != null) {
                    result.put(Integer.toString(i), dataModel);
                    i++;
                }
            }

            // Click Next button, load next button while scraping.
            try {
                WebElement nextPage = ((ChromeDriver) driver).findElementByCssSelector("button[aria-label=' Next page ']");
                while (result.size() < 19 && nextPage != null && nextPage.isEnabled()) {
                    nextPage.click();
                    List<WebElement> nextSections = driver.findElements(By.className("bfdHYd"));
                    for (WebElement web : nextSections) {
                        DataModel dataModel = ScraperUtils.scrapePlaces(web);
                        if (dataModel != null) {
                            result.put(Integer.toString(i), dataModel);
                            i++;
                        }
                    }
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


}