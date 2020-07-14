package com.example.collegeauction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.collegeauction.LoginFragments.LoginFragment;
import com.example.collegeauction.MainFragments.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Sets the toolbar text to nothing
        getSupportActionBar().setTitle("");

        // Sets up action for the floating action button
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // code to open the camera application
            }
        });

        fragment = new HomeFragment();

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}