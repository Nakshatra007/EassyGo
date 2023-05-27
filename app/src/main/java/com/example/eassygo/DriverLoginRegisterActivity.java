package com.example.eassygo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    private Button DriverLoginButton;
    private Button DriverRegisterButton;
    private TextView DriverRegisterLink;
    private TextView DriverStatus;
    private EditText EmailDriver;
    private EditText PasswordDriver;

    private FirebaseAuth mAuth;
    private ProgressDialog LoadingBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        mAuth = FirebaseAuth.getInstance();

        DriverLoginButton = findViewById(R.id.driverLoginButton);
        DriverRegisterButton = findViewById(R.id.driver_register_button);
        DriverRegisterLink = findViewById(R.id.driver_register_link);
        DriverStatus = findViewById(R.id.driver_status);
        EmailDriver = findViewById(R.id.email_driver);
        PasswordDriver = findViewById(R.id.password_driver);
        LoadingBar = new ProgressDialog(this);

        DriverRegisterButton.setVisibility(View.INVISIBLE);
        DriverRegisterButton.setEnabled(false);

        DriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DriverLoginButton.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Register Driver");

                DriverRegisterButton.setVisibility(View.VISIBLE);
                DriverRegisterButton.setEnabled(true);
            }
        });

        DriverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailDriver.getText().toString();
                String password = PasswordDriver.getText().toString();

                RegisterDriver(email, password);
            }
        });

        DriverLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailDriver.getText().toString();
                String password = PasswordDriver.getText().toString();

                SignInDriver(email, password);
            }
        });
    }

    private void SignInDriver(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter your e-mail.", Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }

        else
        {
            LoadingBar.setTitle("Driver SignIn");
            LoadingBar.setMessage("Please wait until we verify the credentials.");
            LoadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(DriverLoginRegisterActivity.this, "Driver signed in successfully.", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                        Intent driverIntent = new Intent(DriverLoginRegisterActivity.this, DriversMapActivity.class);
                        startActivity(driverIntent);
                    }
                    else {
                        Toast.makeText(DriverLoginRegisterActivity.this, "Sign in unsuccessful... Try again", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                    }
                }
            });
        }


    }


    private void RegisterDriver(String email, String password) {

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter your e-mail.", Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }

        else
        {
            LoadingBar.setTitle("Driver Registration");
            LoadingBar.setMessage("Please wait until we register your data.");
            LoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(DriverLoginRegisterActivity.this, "Driver registered successfully.", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();
                        Intent driverIntent = new Intent(DriverLoginRegisterActivity.this, DriversMapActivity.class);
                        startActivity(driverIntent);
                    }
                    else {
                        Toast.makeText(DriverLoginRegisterActivity.this, "Registration unsuccessful... Try again", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                    }
                }
            });
        }
    }
}