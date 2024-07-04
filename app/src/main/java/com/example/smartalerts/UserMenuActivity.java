package com.example.smartalerts;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;

public class UserMenuActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);
        auth=FirebaseAuth.getInstance();

    }

    public void logout(View view){
        auth.signOut();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void userEmergency(View view){
        Intent intent = new Intent(this,UserEmergencyActivity.class);
        startActivity(intent);
    }

    public void goToStatistics(View view){
        Intent intent = new Intent(this,StatisticsActivity.class);
        startActivity(intent);
    }

}