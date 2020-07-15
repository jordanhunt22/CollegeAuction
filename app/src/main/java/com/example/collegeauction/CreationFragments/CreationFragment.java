package com.example.collegeauction.CreationFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.transition.MaterialContainerTransform;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class CreationFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 25;
    private EditText etDescription;
    private EditText etBid;
    private EditText etName;
    private Button btnCaptureImage;
    private ImageView ivListingImage;
    private Button btnSubmit;
    private File photoFile;
    public String photoFileName = "listing.jpg";
    ProgressBar pb;

    public CreationFragment(){
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_creation, container, false);
    }

    // Here I set up all of the views

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Find all of the views
        etDescription = view.findViewById(R.id.etDescription);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        ivListingImage = view.findViewById(R.id.ivListingImage);
        etBid = view.findViewById(R.id.etBid);
        etName = view.findViewById(R.id.etName);

        btnSubmit = view.findViewById(R.id.btnSubmit);
        pb = view.findViewById(R.id.pbLoading);

        // On click listener for the camera button
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here I will save the listing to Parse
                String description = etDescription.getText().toString();
                String name = etName.getText().toString();
                Long bid = Long.parseLong(etBid.getText().toString());
                if (description.isEmpty()){
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || ivListingImage.getDrawable() == null){
                    Toast.makeText(getContext(), "There is no image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // on some click or some loading we need to wait for...
                pb.setVisibility(ProgressBar.VISIBLE);
                saveListing(name, description, bid, photoFile);
            }
        });
    }

    private void saveListing(String name, String description, Long minPrice, File photoFile) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        // Sets expire date to 3 days after the current date
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, 3);
        Date expireDate = c.getTime();
        Listing listing = new Listing();
        listing.setDescription(description);
        listing.setImage(new ParseFile(photoFile));
        listing.setName(name);
        listing.setUser((currentUser));
        listing.put("minPrice", minPrice);
        listing.setExpireTime(expireDate);
        // Set isSold equal to false
        listing.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    Log.e(TAG, "Error while saving!", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful!");
                etDescription.setText("");
                etBid.setText("");
                etName.setText("");
                ivListingImage.setImageResource(0);
                // run a background job and once complete
                pb.setVisibility(ProgressBar.INVISIBLE);
                getActivity().finish();
            }
        });

    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.CollegeAuction", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        // if (intent.resolveActivity(getContext().getPackageManager()) != null) {
        // Start the image capture intent to take photo
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        // }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(takenImage, 0, 0, takenImage.getWidth(), takenImage.getHeight(), matrix, true);
                Drawable imageDrawable = new BitmapDrawable(getResources(), rotatedBitmap);
                Glide.with(getContext())
                        .load(imageDrawable)
                        .transform(new CenterCrop())
                        .into(ivListingImage);
                // ivListingImage.setImageBitmap(takenImage);
                Toast.makeText(getContext(), "Image was taken!", Toast.LENGTH_SHORT).show();
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
}