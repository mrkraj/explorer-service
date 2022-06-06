package com.explorer.places.explorerservice.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DataModel {

    private final String title;  //merchant/name
    private final String address;
    private final String imageUrl;
    private final String[] category;
    private final String reviewStars;
    private final String noOfReviews;
    private final String openState;
    private final String closeState;
    private final String description;
    private final String priceRange;
    private final String latitude;
    private final String longitude;
    private final String url;

    private final String source; //type
    private final String offerTitle; //options/title
    private final String value;
    private final String price;
    private final String discountPrice;
    private final String discount;


    public DataModel(String title, String address, String imageUrl, String[] category, String reviewStars, String noOfReviews, String openState, String closeState,
                     String description, String priceRange, String latitude, String longitude, String url,
                     String source, String offerTitle, String value, String price,
                     String discountPrice, String discount) {
        this.title = title;
        this.address = address;
        this.imageUrl = imageUrl;
        this.category = category;
        this.reviewStars = reviewStars;
        this.noOfReviews = noOfReviews;
        this.openState = openState;
        this.closeState = closeState;
        this.description = description;
        this.priceRange = priceRange;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.source = source;
        this.offerTitle = offerTitle;
        this.value = value;
        this.price = price;
        this.discountPrice = discountPrice;
        this.discount = discount;
    }

    public String getTitle() {
        return title;
    }
}
