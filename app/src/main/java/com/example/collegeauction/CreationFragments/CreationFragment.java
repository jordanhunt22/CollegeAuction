package com.example.collegeauction.CreationFragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Miscellaneous.BitmapScaler;
import com.example.collegeauction.Miscellaneous.MapHelper;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.transition.MaterialContainerTransform;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class CreationFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 25;
    private EditText etDescription;
    private EditText etBid;
    private EditText etName;
    private Button btnCaptureImage;
    private ImageView ivListingImage;
    private TextView tvLocation;
    private Button btnSubmit;
    private Button btnLocation;
    private Button btnGallery;
    private File photoFile;
    public String photoFileName = "listing.jpg";
    ProgressBar pb;
    private FragmentManager fragmentManager;
    private int imageRotated;
    private Uri photoUri;
    private Boolean onStart;

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1000;

    public static LatLng point;
    private String location;
    Location mCurrentLocation;

    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    public CreationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onStart = true;
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

        // Starts checking for the location so I can autofill the city
        CreationFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

        // Find all of the views
        etDescription = view.findViewById(R.id.etDescription);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        btnLocation = view.findViewById(R.id.btnLocation);
        btnGallery = view.findViewById(R.id.btnGallery);
        tvLocation = view.findViewById(R.id.tvLocation);
        ivListingImage = view.findViewById(R.id.ivListingImage);
        etBid = view.findViewById(R.id.etBid);
        etName = view.findViewById(R.id.etName);

        // Sets up fragment manager for transition to choose location
        fragmentManager = getFragmentManager();

        btnSubmit = view.findViewById(R.id.btnSubmit);
        pb = view.findViewById(R.id.pbLoading);

        // On click listener for the camera button
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        // On click listener to add a location
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new MapsCreationFragment();
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "CreationFragment2")
                        .addToBackStack(null)
                        .commit();
            }
        });

        // On click listener for going to the gallery
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto();
            }
        });

        // On click listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (point == null) {
                    Toast.makeText(getContext(), "You did not enter a location", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseGeoPoint returnPoint = new ParseGeoPoint();
                returnPoint.setLatitude(point.latitude);
                returnPoint.setLongitude(point.longitude);
                // Here I will save the listing to Parse
                String name = etName.getText().toString();
                String description = etDescription.getText().toString();
                Long bid = null;
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "The item name cannot be empty. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "The item description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    bid = Long.parseLong(etBid.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Your bid is not a valid number", e);
                    Toast.makeText(getContext(), "Your bid is invalid. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoFile == null || ivListingImage.getDrawable() == null) {
                    Toast.makeText(getContext(), "There is no image", Toast.LENGTH_SHORT).show();
                    return;
                }
                // on some click or some loading we need to wait for...
                // pb.setVisibility(ProgressBar.VISIBLE);
                saveListing(name, description, bid, photoFile, returnPoint);
                // getActivity().finish();
            }
        });
    }

    private void saveListing(String name, String description, Long minPrice, File photoFile, ParseGeoPoint finalPoint) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        // Sets expire date to 3 days after the current date
        // ParseFile image = new ParseFile(photoFile);
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        // c.add(Calendar.DATE, 3);
        c.add(Calendar.MINUTE, 5);
        // c.set(Calendar.MINUTE, 0);
        // c.add(Calendar.HOUR, 8);
        Date expireDate = c.getTime();
        Listing listing = new Listing();
        listing.setDescription(description);
        listing.setImage(new ParseFile(photoFile));
        listing.setName(name);
        listing.setUser((currentUser));
        listing.put("locationName", location);
        listing.put("minPrice", minPrice);
        listing.setExpireTime(expireDate);
        listing.put("location", finalPoint);
        listing.put("isSold", false);
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(TAG)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.flContainer, new CreationFragment2(listing))
                .commit();
//        listing.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Error while saving!", e);
//                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Log.i(TAG, "Post save was successful!");
//                // run a background job and once complete
//                pb.setVisibility(ProgressBar.INVISIBLE);
//                // Toast.makeText(getContext(), "Your listing was posted successfully!", Toast.LENGTH_SHORT).show();
//                getActivity().finish();
//            }
//        });

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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imageRotated = 1;
                // by this point we have the camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(photoFile.getPath());
                // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Matrix altMatrix = new Matrix();
                altMatrix.postRotate(90);
                Bitmap altRotatedBitmap = Bitmap
                        .createBitmap(rawTakenImage, 0, 0, rawTakenImage.getWidth(), rawTakenImage.getHeight(), altMatrix, true);
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(altRotatedBitmap, 1500);
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Rewrite the bitmap to the current file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(photoFile);
                    // Write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap
                        .createBitmap(takenImage, 0, 0, takenImage.getWidth(), takenImage.getHeight(), matrix, true);
                Drawable imageDrawable = new BitmapDrawable(getResources(), rotatedBitmap);
                Glide.with(getContext())
                        .load(imageDrawable)
                        .transform(new CenterCrop())
                        .into(ivListingImage);
                Toast.makeText(getContext(), "Image was taken!", Toast.LENGTH_SHORT).show();
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if ((data != null) && requestCode == PICK_PHOTO_CODE) {

            imageRotated = 2;

            photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            ivListingImage.setImageBitmap(selectedImage);
            ivListingImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            persistImage(selectedImage, "listing");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (point != null) {
            MapHelper.getAddressFromLocation(point, getContext(), new GeocoderHandler());
        }


        if (imageRotated == 1) {
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            // Load the taken image into a preview
            Drawable imageDrawable = new BitmapDrawable(getResources(), takenImage);
            Glide.with(getContext())
                    .load(imageDrawable)
                    .transform(new CenterCrop())
                    .into(ivListingImage);
        } else if (imageRotated == 2) {
            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            ivListingImage.setImageBitmap(selectedImage);
            ivListingImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            // replace by what you need to do
            location = result;
            tvLocation.setText("Location: " + location);
            btnLocation.setText("Change Location");
        }
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void persistImage(Bitmap bitmap, String name) {
        File filesDir = getContext().getFilesDir();
        photoFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }
        // Report to the UI that the location was updated
        mCurrentLocation = location;

        if (onStart){
            point = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            MapHelper.getAddressFromLocation(point, getContext(), new GeocoderHandler());
            onStart = false;
            btnLocation.setText("Change Location");
        }
    }

}