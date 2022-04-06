package com.example.thesis_final.clientSide;


import static com.example.thesis_final.clientSide.KeystoreUtils.isSensorAvailable;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.example.thesis_final.Response;
import com.example.thesis_final.serverSide.UsersServiceAPI;

import java.nio.charset.StandardCharsets;
import java.security.Signature;

/**
 * Client side class for login options. Uses the Users Service API.
 */
public class Login {

    private static final String TAG = "LOGIN";

    public static void loginWithBiometrics(String username, View view) {

        Response rsp = isSensorAvailable(view.getContext());
        if(!rsp.isSuccess()) {
            Toast.makeText(view.getContext(), rsp.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "something";
        KeystoreUtils.signMessage(message, view, username);
    }

    public static void loginUsingPassword(String username, String password, View view){
        Response response = UsersServiceAPI.verifyUserUsingPassword(username, password);
        Toast.makeText(view.getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
        if (response.isSuccess())
            view.getContext().startActivity(new Intent(view.getContext(), LoggedInView.class));
    }
}