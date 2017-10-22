package com.example.akshay.faceanalyser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DescriptionActivity extends AppCompatActivity {

    public VisionServiceClient visionServiceClient= new VisionServiceRestClient("0bea8746fa9040179804d782c93172aa","https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");


    private static int PICK_IMAGE=1;

    ByteArrayOutputStream outputStream;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;
    ByteArrayInputStream inputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        imageView = (ImageView) findViewById(R.id.imageView1);
        textView = (TextView) findViewById(R.id.textView1);
    }

    public void PickCLicked1(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // Get the URI of the selected file
                final Uri uri = data.getData();

                try{

                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,5,outputStream);
                    inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                    imageView.setImageBitmap(bitmap);
                    //use the bitmap as you like


                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void AnalyzeClicked1(View view) {
        AsyncTask<InputStream,String,String> visionTask= new AsyncTask<InputStream, String, String>() {
            @Override
            protected void onPostExecute(String s) {
                AnalysisResult result=new Gson().fromJson(s,AnalysisResult.class);
                TextView textView=(TextView)findViewById(R.id.textView);
                StringBuilder stringBuilder=new StringBuilder();
                for(Caption caption:result.description.captions){
                    stringBuilder.append(caption.text);
                }
                TextView textView1 = (TextView) findViewById(R.id.textView1);
                Log.i("message","Tester log message number 1");
                textView1.setText(stringBuilder);

                Log.i("message","Tester log message number 2");
            }

            @Override
            protected String doInBackground(InputStream... inputStreams) {
                try{
                    String[] features={"Description"};
                    String[] details={};

                    AnalysisResult result=visionServiceClient.analyzeImage(inputStreams[0],features,details);
                    String str=new Gson().toJson(result);
                    return str;
                } catch (Exception e) {
                    return null;
                }


            }


        };
        visionTask.execute(inputStream);

    }

    public void PreviousClicked(View view) {
        Intent i =new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
