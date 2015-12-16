package com.example.sergio.biblioteca_dropbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class MainActivity extends AppCompatActivity {
    /*****************************************************/
    /*        Estas son las keys de la app DropBox       */
    /*      para mayor seguridad se debería de ofuscar   */
    /*****************************************************/
    final static private String APP_KEY = "463frntsft9g0ro";
    final static private String APP_SECRET = "rfkesxcoouuw3br";

    private DropboxAPI<AndroidAuthSession> mDBApi;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // método inicializar
        inicializar();
        //iniciar sesión (se podría llamar a este método haciendo un boton de login?)
        linkToDropbox();

    }

    private void inicializar() {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }
    private void linkToDropbox() {
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                // PORHACER: guardar el accessToken para evitar relogueo
                Toast.makeText(this,"Login Successfull",Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }
}
