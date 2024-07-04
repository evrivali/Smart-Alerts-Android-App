package com.example.smartalerts;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;

public class SmartAlerts {

    private String emergencyType;
    private String comment;
    private String emergencyTimestamp;
    private String location;
    private String user;
    private int emergencyLevel;
    private boolean approved;
    private boolean resolved;

    public SmartAlerts(String emergencyType, String comment, String emergencyTimestamp, String location, String user) {
        this.emergencyType = emergencyType;
        this.comment = comment;
        this.emergencyTimestamp = emergencyTimestamp;
        this.location = location;
        this.approved = false;
        this.resolved = false;
        this.user = user;
    }

    public SmartAlerts(){}

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEmergencyTimestamp() {
        return emergencyTimestamp;
    }

    public void setEmergencyTimestamp(String emergencyTimestamp) {
        this.emergencyTimestamp = emergencyTimestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public int getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(int emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public boolean sameObject(SmartAlerts thisObject, SmartAlerts smartAlert) {
        return thisObject.emergencyType.equals(smartAlert.emergencyType) && thisObject.comment.equals(smartAlert.comment) && thisObject.location.equals(smartAlert.location) && thisObject.emergencyTimestamp.equals(smartAlert.emergencyTimestamp) && thisObject.user.equals(smartAlert.user);
    }

    public void calculateEmergencyLevel() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("smartAlerts").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                ArrayList<SmartAlerts> readObjects = new ArrayList<SmartAlerts>();
                String childKey = "";
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    for (DataSnapshot child : task.getResult().getChildren()) {
                        if (SmartAlerts.this.emergencyType.equals(child.child("emergencyType").getValue(String.class)) && !child.getValue(SmartAlerts.class).isResolved()) {
                            if (!sameObject(SmartAlerts.this, child.getValue(SmartAlerts.class))) {
                                readObjects.add(new SmartAlerts(child.child("emergencyType").getValue(String.class), child.child("comment").getValue(String.class), child.child("emergencyTimestamp").getValue(String.class), child.child("location").getValue(String.class), child.child("user").getValue(String.class)));
                            } else {
                                childKey = child.getKey();
                            }
                        }
                    }
                    String[] values = SmartAlerts.this.location.split(",");
                    int emergencyLevel = 0;
                    double thisSmartAlertLatitude = Double.parseDouble(values[0]);
                    double thisSmartAlertLongitude = Double.parseDouble(values[1]);
                    int reports = 0;
                    int distance_weight_score = 0;
                    int report_weight_score = 0;
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    for (SmartAlerts object : readObjects) {
                        Date dateNow = new Date(now.getTime());
                        Timestamp objectTS = Timestamp.valueOf(object.emergencyTimestamp);
                        Date objectDate = new Date(objectTS.getTime());
                        long diff = dateNow.getTime() - objectDate.getTime();
                        long hours = TimeUnit.MILLISECONDS.toHours(diff);
                        String[] tokens = object.location.split(",");
                        double latitude = Double.parseDouble(tokens[0]);
                        double longitude = Double.parseDouble(tokens[1]);
                        if (distance(latitude, thisSmartAlertLatitude, longitude, thisSmartAlertLongitude) <= 10000 && hours <= 12) {
                            if (distance_weight_score<100) {
                                distance_weight_score += 1;
                            }
                        }
                        if (distance(latitude, thisSmartAlertLatitude, longitude, thisSmartAlertLongitude) <= 100000 && hours <= 3) {
                            reports += 1;
                        }
                    }
                    if (reports >200){
                            report_weight_score=100;
                        }else {
                            for (int i = 0; i < reports; i ++) {
                                report_weight_score += 0.5;
                            }
                    }
                    double score = report_weight_score * 0.45 + distance_weight_score * 0.65;
                    emergencyLevel = (int) Math.round(score);
                    System.out.println(emergencyLevel);
                    System.out.println(normalize(emergencyLevel,0,100));
                    emergencyLevel=(int) Math.round(normalize(emergencyLevel,0,100));
                    System.out.println(emergencyLevel);
                    Map<String, Object> map = new HashMap<>();
                    map.put("emergencyLevel", emergencyLevel);
                    databaseReference.child("smartAlerts").child(childKey).updateChildren(map);
                }
            }
        });
    }

    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        distance = Math.round(distance * 100);
        distance = distance/100;
        return distance;
    }

    public static void sendNotification(String alertID, String range, String comment){
        // Get reference to alert
        DatabaseReference alertReference = FirebaseDatabase.getInstance().getReference().child("smartAlerts").child(alertID);

        int parsedRange = Integer.parseInt(range);
        // Update Data
        alertReference.child("comment").setValue(comment);
        alertReference.child("range").setValue(parsedRange);

        // Trigger SmartAlertService
        alertReference.child("approved").setValue(true);

    }
    /**
     * Calculates a value between 0 and 1, given the precondition that value
     * is between min and max. 0 means value = max, and 1 means value = min.
     */
    /**
     * Calculates a value between 0 and 1, given the precondition that value
     * is between min and max. 0 means value = max, and 1 means value = min.
     */
    double normalize(double value, double min, double max) {
        return (value-min)/(max-min)*3;
    }

}

