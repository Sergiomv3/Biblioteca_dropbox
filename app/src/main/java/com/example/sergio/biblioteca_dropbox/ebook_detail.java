package com.example.sergio.biblioteca_dropbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class Ebook_detail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_detail);
        String titulo = getIntent().getExtras().getString("titulo");
        Intent intent = getIntent();
        Bitmap bitmap = null;//here context can be anything like getActivity() for fragment, this or MainActivity.this
        try {
            bitmap = BitmapFactory.decodeStream(Ebook_detail.this
                    .openFileInput("myImage"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ImageView iv = (ImageView)findViewById(R.id.imageView);
        TextView tv = (TextView)findViewById(R.id.textView);
        iv.setImageBitmap(bitmap);
        tv.setText(titulo);
    }
}
