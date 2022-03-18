package com.example.thesis_final.clientSide;

import static com.example.thesis_final.clientSide.KeystoreUtils.generateKeyPairAndStoreToKeystore;
import static com.example.thesis_final.clientSide.KeystoreUtils.getPubKeyFromKeystore;
import static com.example.thesis_final.clientSide.KeystoreUtils.isSensorAvailable;

import android.view.View;
import android.widget.Toast;

import com.example.thesis_final.serverSide.Response;
import com.example.thesis_final.serverSide.UsersService;

public class Register {

    public static void registerNewUserWithBiometrics(String username, String password, View view) {
        Response rsp = isSensorAvailable(view.getContext());
        if(!rsp.isSuccess()) {
            Toast.makeText(view.getContext(), rsp.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        generateKeyPairAndStoreToKeystore(password);
        String pubKey = getPubKeyFromKeystore();
        if (pubKey == null) {
            Toast.makeText(view.getContext(), "Couldn't retrieve the public key", Toast.LENGTH_SHORT).show();
            return;
        }

        Response response = UsersService.addNewUser(username, pubKey);
        Toast.makeText(view.getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public static void alterUserWithBiometrics(String username, String password, View view) {
        Response rsp = isSensorAvailable(view.getContext());
        if(!rsp.isSuccess()) {
            Toast.makeText(view.getContext(), rsp.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        generateKeyPairAndStoreToKeystore(password);
        String pubKey = getPubKeyFromKeystore();
        if (pubKey == null) {
            Toast.makeText(view.getContext(), "Couldn't retrieve the public key", Toast.LENGTH_SHORT).show();
            return;
        }

        Response response = UsersService.addBiometricsToUser(username, pubKey);
        Toast.makeText(view.getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
