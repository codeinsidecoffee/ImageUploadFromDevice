package com.mrlonewolfer.imageuploadfromdevice;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnCamera,btnGallery,btnUpload;
    ImageView imageView;
    Context context;
    public static final int REQ_CAMERA=100;
    public static final int REQ_GALLERY=2;
    final static int MY_PERMISSION_REQUEST=1;
    private Bitmap bitmap;
    private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        imageView=findViewById(R.id.imageView);
        btnCamera=findViewById(R.id.camBtn);
        btnGallery=findViewById(R.id.galleryBtn);
        btnUpload=findViewById(R.id.uploadBtn);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        checkAppPermission();


    }

    private void checkAppPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }

        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.camBtn){
            Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(intent, REQ_CAMERA);
            }
        }
        if(v.getId()==R.id.galleryBtn){
            Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,REQ_GALLERY);
        }
        if(v.getId()==R.id.uploadBtn){
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);

            byte[] byteArrayImage=baos.toByteArray();
            encodedImage= Base64.encodeToString(byteArrayImage,Base64.DEFAULT);

            new UploadAsyncTask().execute();
        }

    }


    public class UploadAsyncTask extends AsyncTask<String,Void,String>{
        String url="http://192.168.1.103/imageupload/imageupload.php";
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd=ProgressDialog.show(context,"","");
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client=new OkHttpClient();
            Request.Builder builder=new Request.Builder().url(url);

            FormBody.Builder formBodyBuilder=new FormBody.Builder();
            formBodyBuilder.add("image",encodedImage);
            builder.post(formBodyBuilder.build());

            Request request=builder.build();

            try {
                Response response=client.newCall(request).execute();

                return response.body().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ_CAMERA && resultCode == RESULT_OK){
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(imageBitmap);
//                Uri filepath = data.getData();
//                Log.d("filepath", "onActivityResult: "+filepath);
//                imageView.setImageURI(filepath);
            }
        }
        if(requestCode==REQ_GALLERY){
            Uri filepath=data.getData();
            imageView.setImageURI(filepath);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                // Log.d(TAG, String.valueOf(bitmap));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
