package com.example.blogapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blogapp.MainHome;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginName, loginPassword;
    private Button loginBtn;
    private ProgressBar loginProgress;
    private ImageView loginImg;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init view
        loginName = findViewById(R.id.loginName);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        loginProgress = findViewById(R.id.loginProgress);
        loginImg = findViewById(R.id.loginPhoto);
        mAuth = FirebaseAuth.getInstance();

        loginImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = loginName.getText().toString();
                String password = loginPassword.getText().toString();
                if (!email.isEmpty() && !password.isEmpty())
                {
                    loginBtn.setVisibility(View.INVISIBLE);
                    loginProgress.setVisibility(View.VISIBLE);
                    signIn(email, password);
                }

            }
        });

    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    showMessage("Login Success");
                    updateUI();
                }

                else
                {
                    showMessage(task.getException().getMessage());
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void register()
    {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    private void updateUI()
    {
        Intent intent = new Intent(getApplicationContext(), MainHome.class);
        startActivity(intent);
    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            // user already login
            updateUI();
        }
    }
}
