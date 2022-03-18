package com.example.thesis_final;

import static com.example.thesis_final.clientSide.Login.loginUsingPassword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.thesis_final.clientSide.Login;
import com.example.thesis_final.serverSide.UsersRegistered;
import com.example.thesis_final.serverSide.UsersService;

/**
 * View for login using password
 */
public class LoginView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        UsersService.dbConn =  new UsersRegistered();   //initialize from DB

        EditText username = (EditText) findViewById(R.id.editTextTextUserName);
        EditText password = (EditText) findViewById(R.id.editTextTextPassword);

        ( findViewById(R.id.buttonRegister)).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(v.getContext(), RegisterView.class));
                    }
                }
        );

        ((Button) findViewById(R.id.buttonLogin)).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Login using biometrics?")
                                .setCancelable(true)
                                .setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                loginUsingPassword(username.getText().toString(), password.getText().toString(), v);
                                            }
                                        })
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //asynchronous calls
                                                Login.loginWithBiometrics(username.getText().toString(), v);
                                            }
                                        })
                                .show();

                        CurrentState.setUsername(username.getText().toString());


                    }
                }
        );
    }
}