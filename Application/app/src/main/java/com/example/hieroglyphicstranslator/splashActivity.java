package com.example.hieroglyphicstranslator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class splashActivity extends AppCompatActivity {
    private static final int CAMERA_PIC_REQUEST=1;
    private static final int GALLERY_PIC_REQUEST=2;

    private static final int PERMISSIONS_REQUEST_CAMERA=101;
    private static final int PERMISSIONS_REQUEST_GAllERY=102;

    private static final String TAG="error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button takePhoto =(Button) findViewById(R.id.takePhotoButton);
        Button uploadPhoto =(Button) findViewById(R.id.uploadPhotoButton);
        ImageView imageview = (ImageView) findViewById(R.id.my_image);
        imageview.setImageDrawable(getResources().getDrawable(R.drawable.place_holder));

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted

                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(splashActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSIONS_REQUEST_CAMERA);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                }
            }
        });
        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(splashActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_GAllERY);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GALLERY_PIC_REQUEST);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"camera request permission granted start capturing!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(),"camera request permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSIONS_REQUEST_GAllERY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(),"gallery request permission granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(),"gallery request permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST) {
            if(data != null) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                ImageView imageview = (ImageView) findViewById(R.id.my_image);
                networkingAsync task=new networkingAsync();
                task.execute(image);
                imageview.setImageBitmap(image);
            }
        }
        else if (requestCode == GALLERY_PIC_REQUEST) {
            if(data !=null) {
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage =  data.getData();
                    //String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    ImageView imageview = (ImageView) findViewById(R.id.my_image);
                    try {
                        imageview.setImageBitmap(getBitmapFromUri(selectedImage));
                        //myIntent.putExtra("BitmapImage",getBitmapFromUri(selectedImage));
                        networkingAsync task=new networkingAsync();
                        task.execute(getBitmapFromUri(selectedImage));
                        Log.e(TAG,"aSync finished");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Intent myIntent = new Intent(splashActivity.this, translation.class);
                                //splashActivity.this.startActivity(myIntent);
                                TextView translateText=(TextView) findViewById(R.id.translation_edit_text);
                                
                                translateText.setText("https://console.firebase.google.com/project/hierotranslate/storage/hierotranslate.appspot.com/files~2F");
                            }
                        }, 10000);

                        Log.e(TAG,"rohna hnak");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

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