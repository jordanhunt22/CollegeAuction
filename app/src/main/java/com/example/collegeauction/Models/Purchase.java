package com.example.collegeauction.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.Date;

@Parcel(analyze={Purchase.class})
@ParseClassName("Purchase")
public class Purchase extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_LISTING = "listing";
    public static final String KEY_CREATED = "createdAt";

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public ParseObject getListing() { return getParseObject(KEY_LISTING); }

    public void setListing(Listing listing) { put(KEY_LISTING, listing); }

    public Date getCreatedAt() { return getDate(KEY_CREATED); }
}

