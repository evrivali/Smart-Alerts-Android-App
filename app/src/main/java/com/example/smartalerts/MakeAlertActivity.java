package com.example.smartalerts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MakeAlertActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;

    SmartAlerts smartAlert;

    //UI
    ImageView imageViewLevel, imageViewDisaster;
    TextView textViewLevel, textViewLocation, textViewType, textViewTime;
    EditText editText, editTextNumber;
    ProgressBar imageProgress;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_alert);

        // UI
        imageViewLevel = findViewById(R.id.imageViewAlert);
        imageViewDisaster = findViewById(R.id.imageViewDisaster);
        textViewLevel = findViewById(R.id.textViewELevel);
        textViewLocation = findViewById(R.id.textViewELoc);
        textViewType = findViewById(R.id.textViewEType);
        textViewTime = findViewById(R.id.textViewTime);
        editText = findViewById(R.id.editTextTextMultiLine);
        editTextNumber = findViewById(R.id.editTextNumber);
        imageProgress = findViewById(R.id.progressBarImage);
        seekBar = findViewById(R.id.seekBar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference imageR = storageReference.child("images").child(getIntent().getStringExtra("alertID"));
        imageR.getBytes(500000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if(task.isSuccessful()){
                    imageViewDisaster.setImageBitmap(BitmapFactory.decodeByteArray(task.getResult(), 0,task.getResult().length));
                    imageProgress.setVisibility(View.GONE);
                }
                else{
                    imageViewDisaster.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
                    imageProgress.setVisibility(View.GONE);
                }
            }
        });

        reference.child("smartAlerts").child(getIntent().getStringExtra("alertID")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                smartAlert = snapshot.getValue(SmartAlerts.class);
                textViewLevel.setText(Integer.toString(smartAlert.getEmergencyLevel()));
                textViewLocation.setText(smartAlert.getLocation());
                textViewType.setText(smartAlert.getEmergencyType());
                textViewTime.setText(smartAlert.getEmergencyTimestamp());
                editText.setText(smartAlert.getComment());

                if (smartAlert.getEmergencyLevel() == 0) {
                    imageViewLevel.setImageResource(R.drawable.ic_baseline_warning_24_green);
                }
                else if(smartAlert.getEmergencyLevel() == 1){
                    imageViewLevel.setImageResource(R.drawable.ic_baseline_warning_24_yellow);
                }
                else{
                    imageViewLevel.setImageResource(R.drawable.ic_baseline_warning_24_red);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        editTextNumber.setText(Integer.toString(seekBar.getProgress() * 10));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextNumber.setText(Integer.toString(i * 10));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void sendNotification(View view){
        Toast.makeText(this,getString(R.string.notifySend),Toast.LENGTH_LONG).show();
        SmartAlerts.sendNotification(getIntent().getStringExtra("alertID"),editTextNumber.getText().toString(),editText.getText().toString());
        delete(null);

        double alertLat1 = Double.parseDouble(textViewLocation.getText().toString().split(",")[0]);
        double alertLon1 = Double.parseDouble(textViewLocation.getText().toString().split(",")[1]);

        FirebaseDatabase.getInstance().getReference().child("smartAlerts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SmartAlerts alert = dataSnapshot.getValue(SmartAlerts.class);
                    if(alert.isResolved()) continue;
                    double alertLat2 = Double.parseDouble(alert.getLocation().split(",")[0]);
                    double alertLon2 = Double.parseDouble(alert.getLocation().split(",")[1]);
                    if(SmartAlerts.distance(alertLat1,alertLat2,alertLon1,alertLon2) <= Integer.parseInt(editTextNumber.getText().toString()) && alert.getEmergencyType().equals(textViewType.getText().toString())){
                        resolveAlert(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void resolveAlert(String alertID){
        reference.child("smartAlerts").child(alertID).child("resolved").setValue(true);
    }

    public void delete(View view){
        resolveAlert(getIntent().getStringExtra("alertID"));
        finish();
    }

    public void ignore(View view){
        finish();
    }
}