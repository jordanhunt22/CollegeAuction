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
    public static final String KEY_SELLER = "seller";
    public static final String KEY_BUYER = "buyer";
    public static final String KEY_BID = "finalBid";

    public ParseUser getBuyer(){
        return getParseUser(KEY_BUYER);
    }

    public void setBuyer(ParseUser user){
        put(KEY_BUYER, user);
    }

    public ParseUser getSeller(){
        return getParseUser(KEY_SELLER);
    }

    public void setSeller(ParseUser user){
        put(KEY_SELLER, user);
    }

    public ParseObject getListing() { return getParseObject(KEY_LISTING); }

    public void setListing(Listing listing) { put(KEY_LISTING, listing); }

    public Date getCreatedAt() { return getDate(KEY_CREATED); }

    public ParseObject getBid() { return getParseObject(KEY_BID); }

    public void setBid(Bid bid) { put(KEY_BID, bid); }
}

