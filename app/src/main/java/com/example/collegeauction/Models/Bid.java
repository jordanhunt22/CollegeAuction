package com.example.collegeauction.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel(analyze={Bid.class})
@ParseClassName("Bid")
public class Bid extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_PRICE = "price";
    public static final String KEY_LISTING = "listing";
    public static final String KEY_CREATED = "createdAt";

    public String getDescription(){
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description){
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage(){
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile){
        put(KEY_IMAGE, parseFile);
    }

    public ParseUser getUser(){ return getParseUser(KEY_USER); }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public Long getPrice(){
        return getLong(KEY_PRICE);
    }

    public void setPrice(Long price){
        put(KEY_PRICE, price);
    }

    public ParseObject geListing(){ return getParseObject(KEY_LISTING); }

    public void setListing(Listing listing){
        put(KEY_LISTING, listing);
    }
}

