package com.example.eassygo;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button CustomerLoginButton;
    private Button CustomerRegisterButton;
    private TextView CustomerRegisterLink;
    private TextView CustomerStatus;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;
    private FirebaseAuth mAuth;
    private DatabaseReference customerDatabaseRef;

    private String onlineCustomerID;
    private ProgressDialog LoadingBar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mAuth = FirebaseAuth.getInstance();



        CustomerLoginButton = findViewById(R.id.customerLoginButton);
        CustomerRegisterButton = findViewById(R.id.customer_register_button);
        CustomerRegisterLink = findViewById(R.id.register_customer_link);
        CustomerStatus = findViewById(R.id.customer_status);
        EmailCustomer = findViewById(R.id.email_customer);
        PasswordCustomer = findViewById(R.id.password_customer);
        LoadingBar = new ProgressDialog(this);

        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);

        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);
            }
        });

        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                RegisterCustomer(email, password);
            }
        });

        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                SignInCustomer(email, password);
            }
        });
    }

    private void SignInCustomer(String email, String password) {

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
            LoadingBar.setTitle("Customer SignIn");
            LoadingBar.setMessage("Please wait until we verify the credentials.");
            LoadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this, "Customer signed in successfully.", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                        Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                        startActivity(customerIntent);
                    }
                    else {
                        Toast.makeText(CustomerLoginRegisterActivity.this, "Sign in unsuccessful... Try again", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                    }
                }
            });
        }
    }

    private void RegisterCustomer(String email, String password) {

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
            LoadingBar.setTitle("Customer Registration");
            LoadingBar.setMessage("Please wait until we register your data.");
            LoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {

                        onlineCustomerID = mAuth.getCurrentUser().getUid();
                        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().
                                child("Users").child("Customers").child(onlineCustomerID);


                        customerDatabaseRef.setValue(true);

                        Toast.makeText(CustomerLoginRegisterActivity.this, "Customer registered successfully.", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                        Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                        startActivity(customerIntent);
                    }
                    else {
                        Toast.makeText(CustomerLoginRegisterActivity.this, "Registration unsuccessful... Try again", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                    }
                }
            });
        }
    }
}