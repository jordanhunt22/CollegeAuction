package com.example.collegeauction;

import android.app.Application;

import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Favorite;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.Models.Purchase;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Register Parse models
        ParseObject.registerSubclass(Listing.class);
        ParseObject.registerSubclass(Bid.class);
        ParseObject.registerSubclass(Favorite.class);
        ParseObject.registerSubclass(Purchase.class);

        // Set applicationId, and server server based on the values in the Heroku settings.
        // ClientKey is not needed unless explicitly configured
        // Any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("yVLVEGlJcV6yQhGb5Sh9WCpQ3kH97PsLoUDbt9cw") // should correspond to APP_ID env variable
                .clientKey("6wpEeaxihttrjKyR4NCKtHLglRkNTrdfrEytFEoW")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://parseapi.back4app.com/").build());
    }
}

