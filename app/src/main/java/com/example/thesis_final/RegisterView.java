package com.example.thesis_final;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thesis_final.clientSide.Login;
import com.example.thesis_final.clientSide.Register;

/**
 * The main entrypoint. The register view.
 */
public class RegisterView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);

        EditText username = (EditText) findViewById(R.id.editTextTextUserName);
        EditText password = (EditText) findViewById(R.id.editTextTextPassword);

        ( findViewById(R.id.buttonLogin)).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(v.getContext(), LoginView.class));

                    }
                }
        );

        ((Button) findViewById(R.id.buttonRegister)).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Register.registerNewUserWithBiometrics(username.getText().toString(), password.getText().toString(), v);
                    }
                }
        );

    }

}