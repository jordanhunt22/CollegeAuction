package com.example.collegeauction.Activities;

import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.transition.PathMotion;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.collegeauction.CreationFragments.CreationFragment;
import com.example.collegeauction.R;
import com.example.collegeauction.databinding.ActivityCreationBinding;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

public class CreationActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Implementing ViewBinding
        ActivityCreationBinding binding = ActivityCreationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        // Sets up the container transform
        View finalContainer = findViewById(android.R.id.content);

        finalContainer.setTransitionName("shared_element_fab");

        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.addTarget(finalContainer);
        materialContainerTransform.setAllContainerColors(Color.WHITE);
        materialContainerTransform.setScrimColor(Color.WHITE);
        materialContainerTransform.setFadeMode(MaterialContainerTransform.FADE_MODE_IN);
        materialContainerTransform.setDuration(550L);
        getWindow().setSharedElementEnterTransition(materialContainerTransform);

        MaterialContainerTransform materialContainerTransformReverse = new MaterialContainerTransform();
        materialContainerTransformReverse.addTarget(finalContainer);
        materialContainerTransformReverse.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        materialContainerTransformReverse.setDuration(500L);
        materialContainerTransformReverse.setScrimColor(Color.WHITE);
        materialContainerTransform.setAllContainerColors(Color.WHITE);
        getWindow().setSharedElementReturnTransition(materialContainerTransformReverse);

        setContentView(view);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Sets the toolbar text to nothing
        getSupportActionBar().setTitle("");
        setContentView(R.layout.activity_creation);

        fragment = new CreationFragment();

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
        
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        // overridePendingTransition(R.anim.no_animation, R.anim.slide_down_back);
//    }
}