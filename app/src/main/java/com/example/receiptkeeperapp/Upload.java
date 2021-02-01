package com.example.receiptkeeperapp;

import com.google.firebase.database.Exclude;

public class Upload {

    private String mName;
    private String mPrice;
    private String mDate;
    private String mCategory;
    private String mNip;
    private String mImageUrl;
    private String mKey;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        this.mPrice = price;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public String getNip() {
        return mNip;
    }

    public void setNip(String nip) {
        this.mNip = nip;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String ImageUrl) {
        this.mImageUrl = ImageUrl;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        this.mKey = key;
    }

    public Upload(){
        //empty constructor
    }

    public Upload(String name, String price, String date, String nip, String category, String imageUrl){

        mNip = nip;
        mImageUrl = imageUrl;
        mName = name;
        mPrice = price;
        mDate = date;
        mCategory = category;
    }

}
