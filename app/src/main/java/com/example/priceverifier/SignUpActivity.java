package com.example.priceverifier;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
    private Button backButton;
    private Button createButton;
    private AdminDB adminDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        backButton = findViewById(R.id.backButton);
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_password2);
        createButton = findViewById(R.id.createAcc);

        adminDB = new AdminDB(this);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username1 = username.getText().toString().trim();
                String password1 = password.getText().toString().trim();
                String confirmPassword1 = confirmPassword.getText().toString().trim();

                if (!username1.isEmpty() && !password1.isEmpty() && !confirmPassword1.isEmpty()) {
                    if(password1.equals(confirmPassword1)){
                    adminDB.saveAdmin(username1, password1);
                    username.setText("");
                    password.setText("");
                    confirmPassword.setText("");

                    Toast.makeText(SignUpActivity.this, "Admin saved successfully", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(SignUpActivity.this, "password does not match", Toast.LENGTH_SHORT).show();
                }
                }else {
                    Toast.makeText(SignUpActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
