package com.example.hieroglyphicstranslator;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class networkingAsync extends AsyncTask<Bitmap,Void, Void> {
    private static final String TAG="error";
    private static final String HOST="192.168.1.9";

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {

        Socket socket = null;
        try {

            socket = new Socket(HOST, 8888);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmapimg = bitmaps[0];
            bitmapimg = Bitmap.createScaledBitmap(bitmapimg, 1150, 1600, true);
            bitmapimg.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            byte[] byteArray2 = Base64.encode(byteArray, Base64.DEFAULT);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.write(byteArray2);
            dos.flush();
            dos.close();
            socket.close();

            //----------------------------------------------------------------------------------------------
            Socket socket1 = new Socket(HOST,8888);
            InputStream in = socket1.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buff =new byte[1024 * 1024];
            while (true)
            {
                int n = in.read(buff);

                if(n<0)
                {
                    break;
                }
                bos.write(buff,0,n);


            }

            byte [] b = bos.toByteArray();
            bos.close();
            byte [] bb = Base64.decode(b,Base64.DEFAULT);
            String inString=new String(bb);
            Log.e(TAG,"smth "+inString);
            //JSONObject baseJsonResponse = new JSONObject(inString);
            in.close();
            socket1.close();


        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG,"host fl async class");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"io fl async class");}

        return null;
    }


}
