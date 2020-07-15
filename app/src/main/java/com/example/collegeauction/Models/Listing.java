package com.example.collegeauction.Models;


import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

@Parcel(analyze={Listing.class})
@ParseClassName("Listing")
public class Listing extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "photo";
    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_CREATED = "createdAt";
    public static final String KEY_BID = "mostRecentBid";
    public static final String KEY_EXPIRATION = "expiresAt";
    public static final String KEY_FAVS = "favorites";

    public static ArrayList<String> listingsFavoritedByCurrentuser = new ArrayList<String>();

    public String getDescription(){
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description){
        put(KEY_DESCRIPTION, description);
    }

    public String getName(){ return getString(KEY_NAME); }

    public void setName(String name){
        put(KEY_NAME, name);
    }

    public ParseFile getImage(){
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile){
        put(KEY_IMAGE, parseFile);
    }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public ParseObject getRecentBid(){
        return getParseObject(KEY_BID);
    }

    public void setRecentBid(ParseUser user){
        put(KEY_BID, user);
    }

    public Date getExpireTime(){
        return getDate(KEY_EXPIRATION);
    }

    public void setExpireTime(Date expire){
        put(KEY_EXPIRATION, expire);
    }

}

