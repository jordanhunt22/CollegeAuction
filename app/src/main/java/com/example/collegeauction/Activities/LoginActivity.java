package com.example.collegeauction.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.collegeauction.LoginFragments.LoginFragment;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "loginActivity";
    public final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment fragment;

    // Handles the login for users
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Logs out user for testing
        // ParseUser.logOut();

        // Goes to the login screen if the user is currently logged in
        if (ParseUser.getCurrentUser() != null){
            goMainActivity();
        }

        fragment = new LoginFragment();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    private void goMainActivity() {
        Listing.listingsFavoritedByCurrentuser.clear();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}