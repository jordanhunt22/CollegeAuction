package com.example.collegeauction.CreationFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class CreationFragment2 extends Fragment {

    Listing listing;
    List<String> listingArray;
    Button btnSubmit;
    ChipGroup cgAttributes;
    List<Integer> chipIds;
    ProgressBar pbLoading;

    public CreationFragment2() {
        // Required empty public constructor
    }

    public CreationFragment2(Listing listing) {
        this.listing = listing;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_creation2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSubmit = view.findViewById(R.id.btnSubmit);
        cgAttributes = view.findViewById(R.id.cgAttributes);
        pbLoading = view.findViewById(R.id.pbLoading);
        listingArray = new ArrayList<>();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chipIds = cgAttributes.getCheckedChipIds();
                if (chipIds.size() > 3){
                    Toast.makeText(getContext(), "You cannot select more than 3 categories.", Toast.LENGTH_SHORT)
                            .show();
                }
                else if (!chipIds.isEmpty()){
                    for (int i = 0; i < chipIds.size(); i++){
                        pbLoading.setVisibility(View.VISIBLE);
                        Chip chip = cgAttributes.findViewById(chipIds.get(i));
                        listingArray.add((String) chip.getText());
                    }
                    listing.put("categories", listingArray);
                    listing.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            pbLoading.setVisibility(View.GONE);
                            getActivity().finish();
                        }
                    });
                }
                else{
                    Toast.makeText(getContext(), "You need to select a category.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}