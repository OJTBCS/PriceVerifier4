package com.example.priceverifier;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    private Button logIn;
    private Button createAccount;
    private Button backButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private AdminDB adminDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logIn = findViewById(R.id.login);
        createAccount = findViewById(R.id.signup);
        usernameEditText = findViewById(R.id.et_username);
        passwordEditText = findViewById(R.id.et_password);
        backButton = findViewById(R.id.backButton);
        adminDB = new AdminDB(this);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if(!username.isEmpty() && !password.isEmpty()){
                boolean isValidCredentials = adminDB.checkCredentials(username,password);

                if(isValidCredentials){
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    startActivity(intent);

                    usernameEditText.setText("");
                    passwordEditText.setText("");
                    Toast.makeText(LogInActivity.this, "Welcome Admin", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(LogInActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
                }
                else{
                    Toast.makeText(LogInActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                }
                }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }
}
