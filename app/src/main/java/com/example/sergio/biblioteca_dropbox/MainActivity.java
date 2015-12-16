package com.example.sergio.biblioteca_dropbox;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

public class MainActivity extends AppCompatActivity {
    /*****************************************************/
    /*        Estas son las keys de la app DropBox       */
    /*      para mayor seguridad se debería de ofuscar   */
    /*****************************************************/
    final static private String APP_KEY = "463frntsft9g0ro";
    final static private String APP_SECRET = "rfkesxcoouuw3br";

    private DropboxAPI<AndroidAuthSession> mDBApi;
    private boolean mLoggedIn;

    /* ESTAS STRING SON USADAS PARA GUARDAR EN SHAREDPREFERENCES*/
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // método inicializar
        inicializar();
        //iniciar sesión (se podría llamar a este método haciendo un boton de login, pero al ser algo trivial no se ha contemplado)
        linkToDropbox();

    }

    private void inicializar() {
       // AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }
    private void linkToDropbox() {
        if (mLoggedIn) {
            logOut();
        } else {
            AndroidAuthSession session = mDBApi.getSession();
            if (loadAuth(session)) {
                showToast("Already logged in");
            } else {
                mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
                setLoggedIn(mDBApi.getSession().isLinked());
            }
        }

    }

    private void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mDBApi.getSession();
        if(loadAuth(session)){

        }else {

            if (mDBApi.getSession().authenticationSuccessful()) {
                try {
                    // Required to complete auth, sets the access token on the session
                    mDBApi.getSession().finishAuthentication();
                    storeAuth(session);
                    setLoggedIn(true);
                    String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                    //Toast.makeText(this,"Login Successfull",Toast.LENGTH_SHORT).show();
                } catch (IllegalStateException e) {
                    showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                    Log.i("DbAuthLog", "Error authenticating", e);
                }
            }
        }
    }

    private void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
        if (loggedIn) {
            //mSubmit.setText("unlink with Dropbox");
            //showToast("Login Successfull");
            //mDisplay.setVisibility(View.VISIBLE);
        } else {
            //showToast("Logged Out!");
            //mSubmit.setText("Link with Dropbox");
            //mDisplay.setVisibility(View.GONE);
            //mImage.setImageDrawable(null);
        }
    }
    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private boolean loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return false;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
            return true;
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
            return true;
        }
    }
}
