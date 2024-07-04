package com.example.smartalerts;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    EditText emailV, passwordV;
    ProgressBar progress;
    ConstraintLayout constraintLayout;
    Boolean permission_granted=false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 123){
            if(grantResults[0] == 0){
                startService(new Intent(this, SmartAlertService.class));
                permission_granted=true;
            }
            else{
                showMessage(getResources().getString(R.string.warning),getResources().getString(R.string.permissionMessage1));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        }
        else{
            permission_granted=true;
            startService(new Intent(this, SmartAlertService.class));
        }

        // Initialize Variables
        auth = FirebaseAuth.getInstance();
        emailV = findViewById(R.id.editTextEmailAddress);
        passwordV = findViewById(R.id.editTextPassword);
        progress = findViewById(R.id.progressBar);
        constraintLayout = findViewById(R.id.panel);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
    
        // UI
        progress.setVisibility(View.GONE);

        // Check for existing user session
        if (auth.getCurrentUser() != null) {
            if (permission_granted) {
                LogInUser(auth.getUid());
            }
        }
    }

    public void redirectSignUp(View view) {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

    public void redirectUser(Class<?> cls){
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        finish(); // prevents user for backing to this activity
    }

    public void LogInUser(@NotNull String UID) {

            progress.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.INVISIBLE);

            reference.child("users").child(UID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().getValue() != null) {
                            redirectUser(UserMenuActivity.class);
                        } else {
                            reference.child("employees").child(UID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getValue() != null) {
                                                redirectUser(EmployeeMenuActivity.class);
                                        }
                                    } else {
                                        showMessage("Error", task.getException().getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    } else {
                        showMessage("Error", task.getException().getLocalizedMessage());
                    }
                }

            });
        }

    public void LogIn(View view){
            String email = emailV.getText().toString();
            String password = passwordV.getText().toString();
            if (email.isEmpty()) {
                showMessage("Error", getResources().getString(R.string.emptyFieldErrorMessage));
                return;
            } else if (password.isEmpty()) {
                showMessage("Error", getResources().getString(R.string.emptyFieldErrorMessage));
                return;
            }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (permission_granted) {
                        LogInUser(auth.getUid());
                    }else{
                        showMessage("Error",getResources().getString(R.string.locationAccess));
                    }
                } else {
                    showMessage("Error", task.getException().getLocalizedMessage());
                }
            }
        });
        }
    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}