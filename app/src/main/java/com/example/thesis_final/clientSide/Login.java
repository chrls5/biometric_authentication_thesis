package com.example.thesis_final.clientSide;


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

        String message = "something";

        BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "Authentication error");

            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.e(TAG, "Authentication succeeded");
                try {
                    BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
                    Signature cryptoSignature = cryptoObject.getSignature();
                    cryptoSignature.update(message.getBytes(StandardCharsets.UTF_8));
                    final byte[] signed = cryptoSignature.sign();

                    //send signed data to server for verification
                    Response response = UsersServiceAPI.verifyUserUsingBiometrics(username, signed);

                    Toast.makeText(view.getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();

                    if (response.isSuccess())
                        ((Activity) view.getContext()).startActivity(new Intent((Activity) view.getContext(), LoggedInView.class));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.e(TAG, "Authentication failed");

            }
        };

        KeystoreUtils.signMessage(message, view, authCallback);
    }

    public static void loginUsingPassword(String username, String password, View view){
        Response response = UsersServiceAPI.verifyUserUsingPassword(username, password);
        Toast.makeText(view.getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
        if (response.isSuccess())
            view.getContext().startActivity(new Intent(view.getContext(), LoggedInView.class));

    }
}
