package com.explorer.places.explorerservice.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DataModel {

    private String title;
    private String address;
    private String imageUrl;
    private String category;
    private String reviewStars;
    private String noOfReviews;
    private String openState;
    private String closeState;
    private String description;
    private String priceRange;

    public DataModel(String title, String address, String imageUrl, String category, String reviewStars, String noOfReviews, String openState, String closeState, String description, String priceRange) {
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
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setReviewStars(String reviewStars) {
        this.reviewStars = reviewStars;
    }

    public void setNoOfReviews(String noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public void setOpenState(String openState) {
        this.openState = openState;
    }

    public void setCloseState(String closeState) {
        this.closeState = closeState;
    }

}
