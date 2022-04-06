package com.example.thesis_final;

import static com.example.thesis_final.clientSide.Login.loginUsingPassword;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thesis_final.clientSide.Login;
import com.example.thesis_final.serverSide.UsersService;
import com.example.thesis_final.serverSide.UsersServiceAPI;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

/**
 * View for login using password
 */
public class LoginView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        UsersServiceAPI.dbConn = new UsersService();   //initialize from DB

        EditText username = findViewById(R.id.editTextTextUserName);
        EditText password = findViewById(R.id.editTextTextPassword);

        (findViewById(R.id.buttonRegister)).setOnClickListener(
                v -> startActivity(new Intent(v.getContext(), RegisterView.class))
        );

        findViewById(R.id.buttonLogin).setOnClickListener(
                v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Login using biometrics?")
                            .setCancelable(true)
                            .setNegativeButton("No",
                                    (dialog, id) -> loginUsingPassword(username.getText().toString(), password.getText().toString(), v))
                            .setPositiveButton("Yes",
                                    (dialog, id) -> {
                                        //asynchronous call
                                        Login.loginWithBiometrics(username.getText().toString(), v);
                                    })
                            .show();

                    CurrentState.setUsername(username.getText().toString());
                }
        );
    }
}