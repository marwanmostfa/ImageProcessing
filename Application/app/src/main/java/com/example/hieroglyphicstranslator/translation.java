package com.example.hieroglyphicstranslator;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class translation extends AppCompatActivity{
    private static final String TAG="error";
    private Bitmap my_image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);
        //inal View rootView = inflater.inflate(R.layout.symbol_list, container, false);
        Intent intent = getIntent();
        Log.e(TAG, "trnaslating ya bro");
        FirebaseApp.initializeApp(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef=database.getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://hierotranslate.appspot.com");
        Log.e(TAG, "before listener");
        final ArrayList<symbol >symbols=new ArrayList<symbol>();

        /**storageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String url = ds.getValue(String.class);
                    Uri myUrl=Uri.parse(url);
                    if(myUrl !=null) {
                        try {
                            Bitmap image = getBitmapFromUri(myUrl);
                            symbols.add(new symbol(image, "1", "2", "3"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }


                //String value = dataSnapshot.getValue(String.class);
                //Log.d(TAG, Value is  + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to read value
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });**/
       /** for(int i=0;i<28;i++){

            String urlImage="gs://hierotranslate.appspot.com/img"+String.valueOf(i)+".jpg";
            ImageView tempimage = new ImageView(getApplicationContext());
            Glide.with(translation.this)
                    .load(urlImage)
                    .into(tempimage);
            tempimage.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) tempimage.getDrawable();
            Bitmap image = drawable.getBitmap();
            symbols.add(new symbol(image, "1", "2", "3"));
        }**/

        try {
            final File localFile = File.createTempFile("Images", "jpg");
            storageRef.child("img10").getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    my_image = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    if(my_image != null){
                        Log.e(TAG, "bitmap fl saliim");
                    }
                    symbols.add(new symbol(my_image, "1", "2", "3"));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "loading image failed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }



        symbolAdapter sAdapter =new symbolAdapter(getBaseContext(),symbols);
        ListView listView = (ListView) findViewById(R.id.list);

        listView.setAdapter(sAdapter);



        //return rootView;

    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}

