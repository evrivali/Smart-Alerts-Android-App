package com.example.smartalerts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class EmployeeMenuActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_menu);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        // Get alerts
        ArrayList<AlertViewModel> alertViewModels = createAlertModelRecycler();

        // Make recycler View
        recyclerView = findViewById(R.id.recyclerView);
        AlarmRecyclerViewAdapter alarmRecyclerViewAdapter = new AlarmRecyclerViewAdapter(getBaseContext(), alertViewModels, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),MakeAlertActivity.class);
                intent.putExtra("alertID",view.getTag().toString());
                startActivity(intent);
            }
        });

        // Put alerts to recycler view
        recyclerView.setAdapter(alarmRecyclerViewAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Add lines between alerts
        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),linearLayoutManager.getOrientation()));
    }

    public void logout(View view){
        auth.signOut();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private ArrayList<AlertViewModel> createAlertModelRecycler() {
        ArrayList<AlertViewModel> alertViewModels = new ArrayList<>();
        String[] emergencyTypes = getResources().getStringArray(R.array.emergencyTypes);
        reference.child("smartAlerts").orderByChild("emergencyLevel").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alertViewModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SmartAlerts alert = dataSnapshot.getValue(SmartAlerts.class);

                    if(alert.isResolved()){
                        continue;
                    }

                    // Translate emergency
                    String emergency;
                    switch (alert.getEmergencyType()){
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
                    alertViewModels.add(new AlertViewModel(alert.getLocation().toString(), alert.getEmergencyLevel(), emergency,dataSnapshot.getKey(),alert.getEmergencyTimestamp()));
                }

                // Reverse List
                Collections.reverse(alertViewModels);
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return alertViewModels;
    }
}