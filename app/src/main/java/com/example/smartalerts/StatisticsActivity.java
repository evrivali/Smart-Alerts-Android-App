package com.example.smartalerts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference smartAlertsRef = database.getReference("smartAlerts");


        smartAlertsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int flood_counter = 0;
                int fire_counter = 0;
                int earthquake_counter = 0;
                int other_counter = 0;
                int all_counter = 0;
                ArrayList<StatisticsActivity> readObjects2 = new ArrayList<StatisticsActivity>();
                for (DataSnapshot alert: snapshot.getChildren()){
                    String myStr = alert.child("approved").getValue().toString();
                    boolean boolValue = Boolean.parseBoolean(myStr);
                    if (boolValue) {
                        Object emergency = alert.child("emergencyType").getValue();
                        if (emergency.equals("Flood")) {
                            flood_counter++;
                            TextView textView14 = findViewById(R.id.textView14);
                            textView14.setText(String.valueOf(flood_counter));
                            all_counter++;
                        } else if (emergency.equals("Fire")) {
                            fire_counter++;
                            TextView textView15 = findViewById(R.id.textView15);
                            textView15.setText(String.valueOf(fire_counter));
                            all_counter++;
                        } else if (emergency.equals("Earthquake")) {
                            earthquake_counter++;
                            TextView textView16 = findViewById(R.id.textView16);
                            textView16.setText(String.valueOf(earthquake_counter));
                            all_counter++;
                        } else {
                            other_counter++;
                            TextView textView17 = findViewById(R.id.textView17);
                            textView17.setText(String.valueOf(other_counter));
                            all_counter++;
                        }
                        System.out.println(emergency);
                        TextView textView13 = findViewById(R.id.textView13);
                        textView13.setText("TOTAL "+String.valueOf(all_counter));
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void goBack(View view){
        finish();
    }

}