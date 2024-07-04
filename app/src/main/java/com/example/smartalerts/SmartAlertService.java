package com.example.smartalerts;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SmartAlertService extends Service implements LocationListener {
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;
    Location userLocation;
    LocationManager locationManager;

    public SmartAlertService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        // Create notification channel so it asks for permission
        NotificationChannel channel = new NotificationChannel("80085", "SmartAlertsChannel", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        // location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("smartAlerts");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    if (snapshot.child("approved").getValue().toString().equals("true")) {
                        String alertLocation = snapshot.child("location").getValue().toString();
                        double alertLat = Double.parseDouble(alertLocation.split(",")[0]);
                        double alertLon = Double.parseDouble(alertLocation.split(",")[1]);
                        if(SmartAlerts.distance(userLocation.getLatitude(),alertLat,userLocation.getLongitude(),alertLon)<= snapshot.child("range").getValue(Integer.class) * 1000){
                            //TODO ADD GREEK
                            //NotificationChannel channel = new NotificationChannel("80085", "SmartAlertsChannel", NotificationManager.IMPORTANCE_HIGH);
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.getNotificationChannel("80085");
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "80085");

                            int icon = R.drawable.ic_baseline_warning_24_yellow;
                            int color = Color.YELLOW;
                            int level = snapshot.child("emergencyLevel").getValue(Integer.class);
                            if (level == 0) {
                                icon = R.drawable.ic_baseline_warning_24_green;
                                color = Color.GREEN;
                            }
                            else if(level == 1) {
                                icon = R.drawable.ic_baseline_warning_24_yellow;
                                color = Color.YELLOW;
                            }
                            else {
                                icon = R.drawable.ic_baseline_warning_24_red;
                                color = Color.RED;
                            }

                            String emergency;
                            String[] emergencyTypes = getResources().getStringArray(R.array.emergencyTypes);
                            switch (snapshot.child("emergencyType").getValue().toString()){
                                case "Fire":
                                    emergency = emergencyTypes[1];
                                    break;
                                case "Flood":
                                    emergency = emergencyTypes[0];
                                    break;
                                case "Earthquake":
                                    emergency = emergencyTypes[2];
                                    break;
                                case "Tornado":
                                    emergency = emergencyTypes[3];
                                    break;
                                default:
                                    emergency = getResources().getString(R.string.other);
                            }
                            builder.setContentTitle(emergency)
                                    .setContentText(snapshot.child("comment").getValue().toString()) // maybe change thist
                                    .setSmallIcon(icon)
                                    .setColor(color);

                            storage = FirebaseStorage.getInstance();
                            storageReference = storage.getReference();
                            System.out.println(snapshot.getKey());

                            StorageReference imageR = storageReference.child("images").child(snapshot.getKey());
                            imageR.getBytes(500000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                @Override
                                public void onComplete(@NonNull Task<byte[]> task) {
                                    if(task.isSuccessful()){
                                        builder .setLargeIcon(BitmapFactory.decodeByteArray(task.getResult(),0,task.getResult().length))
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(snapshot.child("comment").getValue().toString()))
                                                .setSubText(getString(R.string.dangerNearby))
                                                .setPriority(NotificationCompat.PRIORITY_MAX);
                                        notificationManager.notify(1, builder.build());
                                    }
                                    else{
                                        builder .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_baseline_image_not_supported_24))
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(snapshot.child("comment").getValue().toString()))
                                                .setSubText(getString(R.string.dangerNearby))
                                                .setPriority(NotificationCompat.PRIORITY_MAX);
                                        notificationManager.notify(1, builder.build());
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.getNotificationChannel("80085");
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "80085");
                    builder.setContentTitle(getString(R.string.error1))
                            .setContentText(getString(R.string.error2))
                            .setSmallIcon(R.drawable.ic_baseline_warning_24_yellow)
                            .setPriority(NotificationCompat.PRIORITY_MAX);
                    notificationManager.notify(1, builder.build());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.userLocation = location;
    }
}