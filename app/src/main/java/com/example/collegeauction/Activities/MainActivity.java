package com.example.collegeauction.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.collegeauction.BidsPurchasesFragments.BidsPurchasesFragment;
import com.example.collegeauction.HomeFragments.HomeFragment;
import com.example.collegeauction.MainFragments.FavoritesFragment;
import com.example.collegeauction.HomeFragments.SoonHomeFragment;
import com.example.collegeauction.MainFragments.ProfileFragment;
import com.example.collegeauction.R;
import com.example.collegeauction.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static FloatingActionButton fab;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Context context;
    private int startingPosition;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;

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
                        fragment = new FavoritesFragment();
                        newPosition = 2;
                        break;
                    case R.id.action_history:
                        fragment = new BidsPurchasesFragment(fragmentManager);
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
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        return true;
    }

    private boolean loadFragment(Fragment fragment, int newPosition) {
        if(fragment != null) {
            if(newPosition == 0) {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.flContainer, fragment).commit();

            }
            if(startingPosition > newPosition) {
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right )
                        .replace(R.id.flContainer, fragment).commit();

            }
            if(startingPosition < newPosition) {
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.flContainer, fragment)
                        .commit();

            }
            startingPosition = newPosition;
            return true;
        }

        return false;
    }

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }
}