package com.example.priceverifier;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
                    if (adminDB.isUsernameExist(username1)) {
                        Toast.makeText(SignUpActivity.this, "Username already exist", Toast.LENGTH_SHORT).show();
                    } else {
                        if (password1.equals(confirmPassword1)) {
                            promptForAdminPassword(username1, password1, confirmPassword1);
                        } else {
                            Toast.makeText(SignUpActivity.this, "password does not match", Toast.LENGTH_SHORT).show();
                        }
                        }
                    }else{
                        Toast.makeText(SignUpActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    }
                }
        });
    }
    private void promptForAdminPassword(final String username, final String password, final String confirmPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Confirmation");
        builder.setMessage("Please enter the admin confirmation password:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String adminConfirmationPassword = input.getText().toString().trim();
                String localAdminPassword = "admin123";

                if (adminConfirmationPassword.equals(localAdminPassword)) {
                    adminDB.saveAdmin(username, password);
                    Toast.makeText(SignUpActivity.this, "Admin account created successfully", Toast.LENGTH_SHORT).show();

                    SignUpActivity.this.username.setText("");
                    SignUpActivity.this.password.setText("");
                    SignUpActivity.this.confirmPassword.setText("");
                } else {
                    Toast.makeText(SignUpActivity.this, "Incorrect admin confirmation password. Admin account creation failed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
