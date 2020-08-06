package com.example.collegeauction.MainFragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.collegeauction.Activities.LoginActivity;
import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.ParseApplication;
import com.example.collegeauction.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SettingsFragment extends Fragment {

    private Button btnLogOut;
    private Button btnChangeUsername;
    private Button btnChangeName;
    private TextInputEditText etUsername;
    private TextInputEditText etName;
    private TextInputEditText etPassword;
    private TextInputLayout tlName;
    private TextInputLayout tlUsername;
    private TextInputLayout tlPassword;
    private ParseUser currentUser;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Hides the SerachView
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnChangeUsername = view.findViewById(R.id.btnChangeUsername);
        btnChangeName = view.findViewById(R.id.btnChangeName);
        etUsername = view.findViewById(R.id.etUsername);
        etName = view.findViewById(R.id.etName);
        etPassword = view.findViewById(R.id.etPassword);
        tlName = view.findViewById(R.id.tlName);
        tlUsername = view.findViewById(R.id.tlUsername);
        tlPassword = view.findViewById(R.id.tlPassword);

        // Makes the fab invisible whenever a new fragment starts
        MainActivity.fab.hide();

        // Sets the current user to the user that is logged in
        currentUser = ParseUser.getCurrentUser();

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logs whenever a user logs out
                Bundle bundle = new Bundle();
                bundle.putString("user_id", ParseUser.getCurrentUser().getObjectId());
                ParseApplication.mFireBaseAnalytics
                        .logEvent("logout", bundle);
                ParseUser.logOut();
                goLogIn();
            }
        });

        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks to see if the EditText is empty
                final String name = etName.getText().toString();
                if (name.isEmpty()){
                    tlName.setError("New name cannot be empty.");
                }
                else{
                    currentUser.put("name", name);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null){
                                Toast.makeText(getContext(), "There was an error saving your name", Toast.LENGTH_SHORT)
                                        .show();
                            }
                            else{
                                tlName.setPlaceholderText(name);
                                tlName.setError("");
                                etName.setText("");
                                Toast.makeText(getContext(), "Your new name was saved!", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }
            }
        });

        btnChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks to see if the EditText is empty
                final String username = etUsername.getText().toString();
                if (username.isEmpty()){
                    tlUsername.setError("New username cannot be empty");
                }
                else{
                    currentUser.setUsername(username);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null){
                                Toast.makeText(getContext(), "There was an error saving your username", Toast.LENGTH_SHORT)
                                        .show();
                            }
                            else{
                                tlUsername.setPlaceholderText(username);
                                tlUsername.setError("");
                                etUsername.setText("");
                                Toast.makeText(getContext(), "Your new username was saved!", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }
            }
        });

        tlName.setPlaceholderText(currentUser.getString("name"));
        tlUsername.setPlaceholderText(currentUser.getUsername());
    }

    private void goLogIn() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        if(item != null)
            item.setVisible(false);
    }
}