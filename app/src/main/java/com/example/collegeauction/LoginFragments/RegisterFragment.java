package com.example.collegeauction.LoginFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.collegeauction.MainActivity;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterFragment extends Fragment {

    public static final String TAG = "RegisterFragment";
    public EditText etUsername;
    private EditText etPassword;
    private Button btnRegister;
    private EditText etNumber;
    private EditText etEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    // Here, we should set up all of the views
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etNumber = view.findViewById(R.id.etNumber);
        etEmail = view.findViewById(R.id.etEmail);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String phoneNumber = etNumber.getText().toString();
                String email = etEmail.getText().toString();
                createUser(username, password, phoneNumber, email);
            }
        });
    }
    private void createUser(final String username, final String password, final String number, final String email) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("phoneNumber", number);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    loginUser(username, password);;
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.e(TAG, "Issue with signing up", e);
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(getContext(), "Issue with login!", Toast.LENGTH_SHORT).show();
                    return;
                }
                goMainActivity();
                Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goMainActivity() {
        Listing.listingsFavoritedByCurrentuser.clear();
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}