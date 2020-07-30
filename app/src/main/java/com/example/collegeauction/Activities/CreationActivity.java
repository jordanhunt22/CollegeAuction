package com.example.collegeauction.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.collegeauction.CreationFragments.CreationFragment;
import com.example.collegeauction.CreationFragments.CreationFragment2;
import com.example.collegeauction.R;
import com.example.collegeauction.databinding.ActivityCreationBinding;

public class CreationActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Implementing ViewBinding
        ActivityCreationBinding binding = ActivityCreationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Sets the toolbar text to nothing
        getSupportActionBar().setTitle("");
        setContentView(R.layout.activity_creation);

        fragment = new CreationFragment();

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
        
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down_back);
    }
}