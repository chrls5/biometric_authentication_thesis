package com.example.thesis_final;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thesis_final.clientSide.Register;

/**
 * The main entrypoint. The register view.
 */
public class RegisterView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);

        EditText username = findViewById(R.id.editTextTextUserName);
        EditText password = findViewById(R.id.editTextTextPassword);

        (findViewById(R.id.buttonLogin)).setOnClickListener(
                v -> startActivity(new Intent(v.getContext(), LoginView.class))
        );

        findViewById(R.id.buttonRegister).setOnClickListener(
                v -> Register.registerNewUserWithBiometrics(username.getText().toString(), password.getText().toString(), v)
        );

    }

}