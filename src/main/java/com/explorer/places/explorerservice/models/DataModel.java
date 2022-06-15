package com.explorer.places.explorerservice.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDate;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DataModel {

    private final String title;
    private final String address;
    private final String imageUrl;
    private final String[] category;
    private final String reviewStars;
    private final String noOfReviews;
    private final String openState;
    private final String closeState;
    private final String description;
    private final String priceRange;
    private final Double latitude;
    private final Double longitude;
    private final String url;
    private final String source;
    private final String offerTitle;
    private final String value;
    private final String price;
    private final String discountPrice;
    private final String discount;
    private final String type;
    private final LocalDate date;
    private final String time;
    private Double distance;


    public DataModel(String title, String address, String imageUrl, String[] category, String reviewStars, String noOfReviews, String openState, String closeState,
                     String description, String priceRange, String latitude, String longitude, String url,
                     String source, String offerTitle, String value, String price,
                     String discountPrice, String discount, String type, String date, String time) {
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
        this.latitude = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);
        this.url = url;
        this.source = source;
        this.offerTitle = offerTitle;
        this.value = value;
        this.price = price;
        this.discountPrice = discountPrice;
        this.discount = discount;
        this.type = type;
        this.date = date != null ? LocalDate.parse(date) : null;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setDistance(Double val) {
        this.distance = val;
    }

}
