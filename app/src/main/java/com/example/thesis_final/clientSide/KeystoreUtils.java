package com.example.thesis_final.clientSide;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import static com.example.thesis_final.KeyPairGeneration.generateCertificate;
import static com.example.thesis_final.KeyPairGeneration.generateKeyPairFromPwd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.thesis_final.CurrentState;
import com.example.thesis_final.Response;
import com.example.thesis_final.serverSide.UsersServiceAPI;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class KeystoreUtils {

    private static final String TAG = "KeyStoreUtils";
    private static final String alias = "bio_auth_login";


    private static KeyStore loadKeystore(byte[] keystoreBytes)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks;
        ks = KeyStore.getInstance("AndroidKeystore");
        if (keystoreBytes != null) {
            ks.load(new ByteArrayInputStream(keystoreBytes), "android".toCharArray());
        } else {
            ks.load(null);
        }
        return ks;
    }

    public static String getPubKeyFromKeystore() {
        try {
            KeyStore keystore = loadKeystore(null);
            Key key = keystore.getKey(alias, "password".toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                return Base64.toBase64String(publicKey.getEncoded());
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public static boolean generateKeyPairAndStoreToKeystore(String password) {
        boolean successfull = true;

        try {
            KeyStore ks = loadKeystore(null);
            KeyPair keyPair = generateKeyPairFromPwd(password);
            X509Certificate certificate = generateCertificate(keyPair);
            ks.setKeyEntry(alias,
                    keyPair.getPrivate(),
                    null,
                    new X509Certificate[]{
                            certificate
                    });

            Log.e(TAG, Base64.toBase64String(keyPair.getPublic().getEncoded()));
            Log.e(TAG, Base64.toBase64String(keyPair.getPrivate().getEncoded()));
            CurrentState.setPubKeyLatest(Base64.toBase64String(keyPair.getPublic().getEncoded()));
            CurrentState.setPrivKeyLatest(Base64.toBase64String(keyPair.getPrivate().getEncoded()));

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | NoSuchProviderException | InvalidKeyException | SignatureException | OperatorCreationException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            successfull = false;
        }
        return successfull;
    }


    ////////     LOGIN USAGE    ////////////
    private static PrivateKey getPrivateKey() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore keyStore = loadKeystore(null);
        if (keyStore.containsAlias(alias)) {
            KeyStore.Entry entry = keyStore.getEntry(alias, null);
            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            return privateKey;
        }
        return null;
    }

    public static void signMessage(String message, View view, String username) {


        BiometricPrompt.AuthenticationCallback authCallBack = new BiometricPrompt.AuthenticationCallback() {
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



        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(getPrivateKey());
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setDeviceCredentialAllowed(false)
                    .setNegativeButtonText("Cancel?")
                    .setTitle("Signing")
                    .build();
            BiometricPrompt bp = new BiometricPrompt((FragmentActivity) view.getContext(), ContextCompat.getMainExecutor(view.getContext()), authCallBack);
            bp.authenticate(promptInfo, new BiometricPrompt.CryptoObject(signature));

        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableEntryException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    public static Response isSensorAvailable(Context context) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                BiometricManager biometricManager = BiometricManager.from(context);
                switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
                    case BiometricManager.BIOMETRIC_SUCCESS:
                        return new Response(true, "App can authenticate using biometrics.");
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        return new Response(false, "No biometric features available on this device.");
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        return new Response(false, "Biometric features are currently unavailable.");
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        // Prompts the user to create credentials that your app accepts.
                        return new Response(false, "No fingerprints detected. Enroll one.");
                }
            } else {
                return new Response(false, "Unsupported android version");
            }
        } catch (Exception e) {
            System.out.println("Error detecting biometrics availability: " + e.getMessage());
        }
        return new Response(false, "Error detecting biometrics availability");
    }


}
