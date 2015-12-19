package com.example.sergio.biblioteca_dropbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class MainActivity extends AppCompatActivity {
    /*****************************************************/
    /*        Estas son las keys de la app DropBox       */
    /*      para mayor seguridad se debería de ofuscar   */
    /*****************************************************/
    final static private String APP_KEY = "463frntsft9g0ro";
    final static private String APP_SECRET = "rfkesxcoouuw3br";

    private DropboxAPI<AndroidAuthSession> mDBApi;
    private boolean mLoggedIn = false;
    private boolean isVistaLista = false;
    private ArrayList<Ebook> ebooksList = null;
    private GridView gv;
    private CustomGridViewAdapter customGridAdapter;
    private Book eBookTemp;
    /* ESTAS STRING SON USADAS PARA GUARDAR EN SHAREDPREFERENCES*/
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Instancia de vistas
        gv = (GridView) findViewById(R.id.gridView1);
        //gv.setGravity(Gravity.CENTER);
        //Bitmap epubIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.epub);

        // método inicializar
        inicializar();
        //iniciar sesión (se podría llamar a este método haciendo un boton de login, pero al ser algo trivial no se ha contemplado)

        linkToDropbox();
        // sincronizar los archivos epub tras haber conectado
        new Sincronizador().execute("");

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long id) {

                String path = ebooksList.get(position).getPath();
                String fileName = ebooksList.get(position).getFileName();
                //showToast(path);
                downloadDropboxFile(path);

                return;
            }

        });
    }
    private boolean downloadDropboxFile(String filePath) {
        new Copy().execute(filePath);
        return true;
    }



    class Copy extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... params) {
            try {
                showToast("Cargando datos... Espera por favor");
                DropboxAPI.DropboxInputStream fd = mDBApi.getFileStream(params[0], null);
                eBookTemp = (new EpubReader()).readEpub(fd);


            } catch (DropboxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //showToast(eBookTemp.getTitulo+" Titulo");


            //Bitmap coverImage = BitmapFactory.decodeStream(eBookTemp.getCoverImage().getInputStream());
            //ImageView iv;
            //iv.setImageBitmap(coverImage);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.vista) {
            if (gv.getNumColumns() == 2) {
                setVistaLista();
            } else {
                setVistaGrid();
            }
            return true;
        } else if (id == R.id.ordenar_fecha) {
            ordenarListaFecha();
        }

        return super.onOptionsItemSelected(item);
    }

    private void ordenarListaFecha() {
        // Se podría implementar un compareTo en la clase Ebook
        Ebook aux = new Ebook("", "", "");
        for (int i = 0; i < ebooksList.size() - 1; i++) {
            for (int j = i + 1; j < ebooksList.size(); j++) {
                if (ebooksList.get(i).getModified().compareToIgnoreCase(ebooksList.get(j).getModified()) > 0) {
                    aux = ebooksList.get(i);
                    ebooksList.set(i, ebooksList.get(j));
                    ebooksList.set(j, aux);
                }
            }

        }
        customGridAdapter.notifyDataSetChanged();
    }

    private void setVistaGrid() {
        this.isVistaLista = false;
        gv.setNumColumns(2);
        customGridAdapter = new CustomGridViewAdapter(MainActivity.this, R.layout.row_view, ebooksList);
        gv.setAdapter(customGridAdapter);
        customGridAdapter.notifyDataSetChanged();
        gv.setGravity(Gravity.CENTER);
    }

    private void setVistaLista() {
        this.isVistaLista = true;
        gv.setNumColumns(1);
        customGridAdapter = new CustomGridViewAdapter(MainActivity.this, R.layout.row_view_list, ebooksList);
        gv.setAdapter(customGridAdapter);
        customGridAdapter.notifyDataSetChanged();
        gv.setGravity(Gravity.LEFT);
    }

    class Sincronizador extends AsyncTask<String, Void, String> {

        private Exception exception;

        @Override
        protected String doInBackground(String... params) {

            try {

                DropboxAPI.Entry files = mDBApi.metadata("/MyEBooks", 0, null, true, null);
                List<DropboxAPI.Entry> CFolder = files.contents;
                ebooksList = new ArrayList<Ebook>();
                // FILTRAMOS LA LISTA
                List<DropboxAPI.Entry> CFolder_filtered = new ArrayList<DropboxAPI.Entry>();
                for (int i = 0; i < CFolder.size(); i++) {
                    String fileName = CFolder.get(i).fileName();

                    if (fileName.endsWith(".epub")) {
                        CFolder_filtered.add(CFolder.get(i));
                        Ebook ebook = new Ebook(CFolder.get(i).fileName(), CFolder.get(i).clientMtime, CFolder.get(i).path);

                        ebooksList.add(ebook);

                    }
                }
                if (CFolder_filtered != null) {
                    for (DropboxAPI.Entry entry : CFolder_filtered) {
                        Log.i("DbExampleLog", "Filename: " + entry.fileName());
                    }
                } else {
                    Log.i("Error Filtering", "List is null");
                }

            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return null;
        }


        protected void onPostExecute(String string) {

            if (isVistaLista) {
                setVistaLista();

            } else {
                setVistaGrid();

            }

        }


    }


    private void inicializar() {
        // AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    private void linkToDropbox() {
        // Descomentar esto si quieres implementar un boton de logout

        if (mLoggedIn) {
            //logOut();
        } else {
            AndroidAuthSession session = mDBApi.getSession();
            if (loadAuth(session)) {
                showToast("Already logged in");
                setLoggedIn(mDBApi.getSession().isLinked());
            } else {
                mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
                setLoggedIn(mDBApi.getSession().isLinked());
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("mLoggedIn", mLoggedIn);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLoggedIn = savedInstanceState.getBoolean("mLoggedIn");
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
        if (loadAuth(session)) {

        } else {

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
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0)
            return false;

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
