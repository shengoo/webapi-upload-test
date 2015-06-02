package com.sheng00.demo.uploadtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends Activity {

    ImageView image;

    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;
    protected static final int REQ_CODE_PICK_IMAGE = 3;

    String uploadUrl = "http://10.35.24.186/WebApiServer/api/Values/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView)findViewById(R.id.imageView);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("pick image")
                .setItems(R.array.change_avatar_action_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                selectPicture();
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void selectPicture(){
//        Intent intentFromGallery = new Intent();
//        intentFromGallery.setType("image/*"); // 设置文件类型
//        intentFromGallery
//                .setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intentFromGallery,
//                IMAGE_REQUEST_CODE);

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        photoPickerIntent.putExtra("aspectX", 1);
        photoPickerIntent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        photoPickerIntent.putExtra("outputX", 320);
        photoPickerIntent.putExtra("outputY", 320);
        photoPickerIntent.putExtra("return-data", true);
        photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
    }

    private void takePicture(){
        Intent intentFromCapture = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
//        if (Tools.hasSdcard()) {
//
//            intentFromCapture.putExtra(
//                    MediaStore.EXTRA_OUTPUT,
//                    Uri.fromFile(new File(Environment
//                            .getExternalStorageDirectory(),
//                            IMAGE_FILE_NAME)));
//        }

        startActivityForResult(intentFromCapture,
                CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_REQUEST_CODE:
                startPhotoZoom(data.getData());
                break;
//            case CAMERA_REQUEST_CODE:
//                if (Tools.hasSdcard()) {
//                    File tempFile = new File(
//                            Environment.getExternalStorageDirectory()
//                                    + IMAGE_FILE_NAME);
//                    startPhotoZoom(Uri.fromFile(tempFile));
//                } else {
//                    Toast.makeText(MainActivity.this, "未找到存储卡，无法存储照片！",
//                            Toast.LENGTH_LONG).show();
//                }
//
//                break;
            case RESULT_REQUEST_CODE:
                if (data != null) {
                    getImageToView(data);
                }
                break;
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (data!=null) {
                        Bundle extras = data.getExtras();
                        Bitmap selectedBitmap = extras.getParcelable("data");
                        String avatarFileName = "test.jpg";
                        FileOutputStream outputStream;
                        try {
                            outputStream = openFileOutput(avatarFileName, Context.MODE_PRIVATE);
                            selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();
                            uploadBitmap(selectedBitmap);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        image.setImageBitmap(selectedBitmap);
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadBitmap(Bitmap bitmap){
        new uploadBitmap().execute(bitmap);
    }

    private class uploadBitmap extends AsyncTask<Bitmap,Void,Void> {

        @Override
        protected Void doInBackground(Bitmap... params) {
            if(params.length>0){

//                try {
//                    HttpURLConnection connection = (HttpURLConnection ) new URL(uploadUrl).openConnection();
//// set some connection properties
//                    OutputStream output = connection.getOutputStream();
//                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, CHARSET), true);
//// set some headers with writer
//                    InputStream file = new ByteArrayInputStream(myEntity.getBinaryFile());
//                    System.out.println("Size: " + file.available());
//                    byte[] buffer = new byte[4096];
//                    int length;
//                    while ((length = file.read(buffer)) > 0) {
//                        output.write(buffer, 0, length);
//                    }
//                    output.flush();
//                    writer.append(CRLF).flush();
//                    writer.append("--" + boundary + "--").append(CRLF).flush();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

//                Bitmap bitmap = params[0];
//                String attachmentName = "bitmap";
//                String attachmentFileName = "bitmap.bmp";
//                String crlf = "\r\n";
//                String twoHyphens = "--";
//                String boundary =  "*****";
//                HttpURLConnection httpUrlConnection = null;
//                URL url = null;
//                try {
//                    url = new URL(uploadUrl);
//                    httpUrlConnection = (HttpURLConnection) url.openConnection();
//                    httpUrlConnection.setUseCaches(false);
//                    httpUrlConnection.setDoOutput(true);
//
//                    httpUrlConnection.setRequestMethod("POST");
//                    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
//                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
//                    httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                    DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
//
//                    request.writeBytes(twoHyphens + boundary + crlf);
//                    request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
//                    request.writeBytes(crlf);
//                    byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
//                    for (int i = 0; i < bitmap.getWidth(); ++i) {
//                        for (int j = 0; j < bitmap.getHeight(); ++j) {
//                            //we're interested only in the MSB of the first byte,
//                            //since the other 3 bytes are identical for B&W images
//                            pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
//                        }
//                    }
//
//                    request.write(pixels);
//                    request.writeBytes(crlf);
//                    request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
//                    request.flush();
//                    request.close();
//                    httpUrlConnection.disconnect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                Bitmap bitmap = params[0];
                InputStream is;

                ByteArrayOutputStream bao = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);

                byte [] ba = bao.toByteArray();

                String ba1= Base64.encodeToString(ba, 0);
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

                nameValuePairs.add(new BasicNameValuePair("file",ba1));
                nameValuePairs.add(new BasicNameValuePair("text","test upload"));

                try{

                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httppost = new HttpPost("http://10.35.24.186/WebApiServer/api/Values/upload");

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);

                    HttpEntity entity = response.getEntity();
                    System.out.print(entity.toString());
                    is = entity.getContent();
                    Log.e("log_tag", "Upload success ");

                }catch(Exception e){
                    e.printStackTrace();
                    Log.e("log_tag", "Error in http connection " + e.toString());

                }


            }
            return null;
        }
    }



    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            image.setImageDrawable(drawable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
