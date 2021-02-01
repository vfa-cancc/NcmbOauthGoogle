package com.nifcloud.ncmboauthgoogle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.SignInButton;
import com.nifcloud.mbaas.core.NCMB;
import com.nifcloud.mbaas.core.NCMBException;
import com.nifcloud.mbaas.core.NCMBGoogleParameters;
import com.nifcloud.mbaas.core.NCMBUser;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static final String AUTH_SCOPE = "oauth2:profile email";
    private static final int REQUEST_SIGN_IN = 10000;
    private static final String TAG = "NCMBLOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //**************** APIキーの設定とSDKの初期化 **********************
        NCMB.initialize(this.getApplicationContext(), "0730e01abce99ac3d5400690cb658a25f79e8f0bac8895dd67283e9b98077d1e",
                "d4175a28a524d55c47057f6f77b47c0c654842521b94488442867c82deb83dac");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.GET_ACCOUNTS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.GET_ACCOUNTS},
                        1);
            }
        }

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.GET_ACCOUNTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = AccountManager.newChooseAccountIntent(null
                            , null
                            , new String[] {"com.google"}
                            , null
                            , null
                            , null
                            , null);
                    startActivityForResult(intent, REQUEST_SIGN_IN);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "ACCESS_GET_ACCOUNTS is granted!");
                } else {
                    Log.d(TAG, "ACCESS_GET_ACCOUNTS is denied!");
                }
            }
        }
    }

    private void getGoogleToken() {

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String> () {
            @Override
            protected String doInBackground(String... accounts) {

                String scopes = AUTH_SCOPE;
                String token = null;
                String id = null;
                try {
                    // Get google account
                    AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
                    Account[] getAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
                    if (getAccounts.length>0) {
                        String accountName = getAccounts[0].name;

                        id = GoogleAuthUtil.getAccountId(getApplicationContext(), accountName);
                        token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
                        Log.d(TAG, "id: " + id);
                        Log.d(TAG, "token: " + token);

                        NCMBGoogleParameters parameters = new NCMBGoogleParameters(
                                id,
                                token
                        );

                        NCMBUser.loginWith(parameters);

                        Log.d(TAG, "Login success!");
                    }


                } catch (UserRecoverableAuthException e) {
                    startActivityForResult(e.getIntent(), REQUEST_SIGN_IN);
                } catch (IOException | GoogleAuthException | ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NCMBException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                return token;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        };

        task.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN && resultCode == RESULT_OK) {
            getGoogleToken();
        }
    }
}