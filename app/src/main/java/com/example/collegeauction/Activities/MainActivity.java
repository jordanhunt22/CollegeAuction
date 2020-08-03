package com.example.collegeauction.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.example.collegeauction.BidsPurchasesFragments.BidsPurchasesFragment;
import com.example.collegeauction.FavFragments.FavFragment;
import com.example.collegeauction.HomeFragments.HomeFragment;
import com.example.collegeauction.FavFragments.FavoritesFragment;
import com.example.collegeauction.MainFragments.ProfileFragment;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Purchase;
import com.example.collegeauction.ParseApplication;
import com.example.collegeauction.R;
import com.example.collegeauction.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static FloatingActionButton fab;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Context context;
    private int startingPosition;
    private BottomNavigationView bottomNavigationView;
    private String dialogueText;
    private Boolean toPurchases;
    public static final String TAG = "MainActivity";

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);

        super.onCreate(savedInstanceState);

        toPurchases = false;

        builder = new MaterialAlertDialogBuilder(this);

        context = this;

        // Implementing ViewBinding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Sets the toolbar text to nothing
        getSupportActionBar().setTitle("");

        // Sets up action for the floating action button
        fab = binding.fab;

        fab.setTransitionName("shared_element_container");


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent for the new activity
                Intent intent = new Intent(context, CreationActivity.class);
                // Show the activity
                context.startActivity(intent);
                overridePendingTransition(R.anim.right_bottom_up, R.anim.no_animation);
            }
        });

        // Resolves the bottom navigation bar
        bottomNavigationView = binding.bottomNavigation;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                int newPosition = 0;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        newPosition = 1;
                        break;
                    case R.id.action_favorites:
                        fragment = new FavFragment();
                        newPosition = 2;
                        break;
                    case R.id.action_history:
                        fragment = new BidsPurchasesFragment();
                        newPosition = 3;
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        newPosition = 4;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                // fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                // return true;
                return loadFragment(fragment, newPosition);
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private boolean loadFragment(Fragment fragment, int newPosition) {
        if (fragment != null) {
            if (newPosition == 0) {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.flContainer, fragment, fragment.getClass().getSimpleName()).commit();

            }
            if (startingPosition > newPosition) {
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.flContainer, fragment, fragment.getClass().getSimpleName()).commit();

            }
            if (startingPosition < newPosition) {
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.flContainer, fragment, fragment.getClass().getSimpleName())
                        .commit();

            }

            // Sends the BidsPurchases Fragment to the Purchases tab
            if (toPurchases) {
                BidsPurchasesFragment tempFragment = (BidsPurchasesFragment) fragment;
                tempFragment.setToPurchases(true);
                toPurchases = false;
            }
            startingPosition = newPosition;
            return true;
        }

        return false;
    }

    public void queryBuys() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = new ParseQuery(Purchase.class);
        query.whereEqualTo("buyer", currentUser);
        query.whereEqualTo("seenByBuyer", false);
        query.include("finalBid");
        query.include("finalBid.price");
        query.findInBackground(new FindCallback<Purchase>() {
            @Override
            public void done(List<Purchase> purchases, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting purchases", e);
                    e.printStackTrace();
                    return;
                }

                if (purchases.isEmpty()) {
                    return;
                }

                else{
                    for (Purchase purchase : purchases) {
                        purchase.put("seenByBuyer", true);
                        purchase.saveInBackground();
                        // Logs new purchases
                        Bundle bundle = new Bundle();
                        bundle.putString("item_id", purchase.getObjectId());
                        Bid finalBid = (Bid) purchase.getBid();
                        bundle.putInt("price", finalBid.getPrice().intValue());
                        ParseApplication.mFireBaseAnalytics
                                .logEvent("purchase", bundle);
                    }
                }


                if (purchases.size() == 1) {
                    dialogueText = "You have a new purchase.";
                } else {
                    dialogueText = "You have " + purchases.size() + " new purchases.";
                }
                builder
                        // Add customization options here
                        .setTitle(dialogueText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                toPurchases = true;
                                bottomNavigationView.setSelectedItemId(R.id.action_history);
                            }
                        })
                        .show();
            }

        });
    }

    public void querySales() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = new ParseQuery(Purchase.class);
        query.whereEqualTo("seller", currentUser);
        query.whereEqualTo("seenBySeller", false);
        query.whereExists("buyer");
        query.findInBackground(new FindCallback<Purchase>() {
            @Override
            public void done(List<Purchase> purchases, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting sales", e);
                    e.printStackTrace();
                    return;
                }

                for (Purchase purchase : purchases) {
                    purchase.put("seenBySeller", true);
                    purchase.saveInBackground();
                }

                if (purchases.isEmpty()) {
                    return;
                } else {
                    if (purchases.size() == 1) {
                        dialogueText = "You have a new sale.";
                    } else {
                        dialogueText = "You have " + purchases.size() + " new sales.";
                    }
                    builder
                            // Add customization options here
                            .setTitle(dialogueText)
//                            .setNegativeButton("YES", new DialogInterface.OnClickListener() {
//
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                }
//                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    bottomNavigationView.setSelectedItemId(R.id.action_profile);
                                }
                            })
                            .show();
                }
            }
        });
    }
}