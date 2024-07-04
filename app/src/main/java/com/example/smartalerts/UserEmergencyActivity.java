package com.example.smartalerts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Objects;


public class UserEmergencyActivity extends AppCompatActivity implements LocationListener {
    Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    ImageButton imageButton;
    LocationManager locationManager;
    String currentLocation;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_emergency_activity);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        imageButton = findViewById(R.id.imageButton);
        auth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                TextView otherTV = findViewById(R.id.editTextEmergencyType);
                if (checkedId == R.id.other) {
                    otherTV.setEnabled(true);
                } else {
                    otherTV.setEnabled(false);
                }
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void openGallery() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            selectImageActivityResultLauncher.launch(photoPickerIntent);

        } else {
            toastMessage(getResources().getString(R.string.grantAccessErrorMessage));
        }

    }
    ActivityResultLauncher<Intent> selectImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    Intent data = result.getData();
                    imageUri = Objects.requireNonNull(data).getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BitmapFactory.decodeStream(imageStream);
                    imageButton.setImageURI(imageUri);
                }
            });

    public void logout(View view){
        auth.signOut();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToStatistics(View view){
        Intent intent = new Intent(this,StatisticsActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

    // UploadImage method
    private void uploadImage(String emId)
    {
        ProgressBar progressBar= findViewById(R.id.uploadPhotoLoading);
        if (imageUri != null) {

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + emId);
            ref.putFile(imageUri)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressBar.setVisibility(View.GONE);
                                    toastMessage(getResources().getString(R.string.imageUploadedMessage));
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressBar.setVisibility(View.GONE);
                            toastMessage(getResources().getString(R.string.failErrorMessage) + e.getMessage());
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            });
        }
    }

    public void onLocationChanged(@NonNull Location location) {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        currentLocation = latitude.substring(0, 8) + "," + longitude.substring(0, 8);
        TextView locationTV = findViewById(R.id.LocationtextView);
        locationTV.setText(getResources().getString(R.string.currentLocation)+" "+currentLocation);
    }
    public void submitEmergency(View view){
        String emergencyType;
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        Timestamp timestamp= new Timestamp(System.currentTimeMillis());
        EditText comment = findViewById(R.id.editTextComments);
        if (selectedId == R.id.other){
            EditText editTextEmergency = findViewById(R.id.editTextEmergencyType);
            emergencyType= editTextEmergency.getText().toString();
        }else{
            RadioButton rb = findViewById(selectedId);
            emergencyType=rb.getText().toString();
            if (emergencyType.equals("Πλυμμήρα")) {
                emergencyType = "Flood";
            }else if (emergencyType.equals("Φωτιά")){
                emergencyType="Fire";
            }else if (emergencyType.equals("Σεισμός")){
                emergencyType="Earthquake";
            }
        }
        if (emergencyType.isEmpty()||comment.getText().toString().isEmpty()||currentLocation==null) {
            if(currentLocation==null){
                toastMessage(getResources().getString(R.string.locatingErrorMessage));
            }else {
                toastMessage(getResources().getString(R.string.emptyFieldErrorMessage));
            }
        }else{
            SmartAlerts smartAlert = new SmartAlerts(emergencyType, comment.getText().toString(), timestamp.toString(), currentLocation, auth.getCurrentUser().getUid());
            addDatatoFirebase(smartAlert);
            smartAlert.calculateEmergencyLevel();
        }
    }
    private void  addDatatoFirebase(SmartAlerts smartAlert) {
        DatabaseReference smartAlertsref = databaseReference.child("smartAlerts");
        DatabaseReference newSmartAlertsRef = smartAlertsref.push();
        String newKey= newSmartAlertsRef.getKey();
        newSmartAlertsRef.setValue(smartAlert);
        toastMessage(getResources().getString(R.string.saveMessage));
        uploadImage(newKey);
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();

                } else {
                    showMessage("Error",getResources().getString(R.string.grantAccessErrorMessage));

                }

            });


    public void makePermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    public void checkPermissionRequest(View view) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showAlertDialog();
        } else {
            makePermissionRequest();
        }
    }

    // is called if the permission is not given.
    public void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.locationAccess));
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        makePermissionRequest();
                    }
                });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

}