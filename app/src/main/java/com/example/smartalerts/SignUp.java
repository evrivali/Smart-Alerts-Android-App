package com.example.smartalerts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class SignUp extends AppCompatActivity {
    FirebaseAuth auth;
    String userID;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void UserSignUp(View view) {
        TextView emailTV = findViewById(R.id.editTextEmailAddressSignUp);
        TextView passwordTV = findViewById(R.id.editTextPersonPassword);
        String email = emailTV.getText().toString();
        String password = passwordTV.getText().toString();
        TextView nameTV = findViewById(R.id.editTextPersonName);
        TextView surnameTV = findViewById(R.id.editTextPersonSurname);
        String name = nameTV.getText().toString();
        String surname = surnameTV.getText().toString();
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toastMessage(getResources().getString(R.string.emptyFieldErrorMessage));
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        toastMessage(getResources().getString(R.string.registrationSuccessful));
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        userID = firebaseUser.getUid();
                        addDataToFirebase(name, surname, userID);

                    } else {
                        showMessage("Error", getResources().getString(R.string.registrationFailed));
                    }
                }
            });
        }
    }

    public void redirectToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void addDataToFirebase(String name, String surname, String uid) {
        User signedUp = new User(name, surname);
        databaseReference.child("users").child(uid).setValue(signedUp);
        toastMessage(getResources().getString(R.string.saveMessage));
        redirectToMain();
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}