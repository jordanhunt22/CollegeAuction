package com.example.collegeauction.LoginFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.ParseApplication;
import com.example.collegeauction.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterFragment extends Fragment {

    public static final String TAG = "RegisterFragment";
    public TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etName;
    private Button btnRegister;
    private TextInputEditText etNumber;
    private TextInputEditText etEmail;
    private TextInputLayout tlUsername;

    public RegisterFragment(){
        // Required constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Sets up the transitions
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        super.onCreate(savedInstanceState);
    }

    // Here, we should set up all of the views
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etName = view.findViewById(R.id.etName);
        etNumber = view.findViewById(R.id.etNumber);
        etEmail = view.findViewById(R.id.etEmail);
        btnRegister = view.findViewById(R.id.btnRegister);
        tlUsername = view.findViewById(R.id.tlUsername);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String phoneNumber = etNumber.getText().toString();
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                if (username.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() || name.isEmpty() || email.isEmpty()){
                    Toast.makeText(getContext(), "You must fill in every field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (username.contains(" ")){
                    tlUsername.setError("Your username cannot have a space");
                    return;
                }
                createUser(username, password, phoneNumber, email, name);
            }
        });

        etNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }
    private void createUser(final String username, final String password, final String number, final String email, final String name) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("phoneNumber", number);
        user.put("name", name);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    loginUser(username, password);
                    // Logs the registration of a new user
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Android Application");
                    ParseApplication.mFireBaseAnalytics
                            .logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
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