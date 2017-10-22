package com.example.akshay.faceanalyser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.vision.contract.Description;

public class MainActivity extends AppCompatActivity {


    public EmotionServiceClient emotionServiceClient = new EmotionServiceRestClient("e8ced3a76fe946d3a862fa6068541884","https://westus.api.cognitive.microsoft.com/emotion/v1.0");


    private static int PICK_IMAGE=1;

    ByteArrayOutputStream outputStream;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;
    ByteArrayInputStream inputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
    }

    public void PickCLicked(View view) {
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

    public void AnalyzeClicked(View view) {
        AsyncTask<InputStream,String,List<RecognizeResult>> emotionTask= new AsyncTask<InputStream,String,List<RecognizeResult>>()
        {
            ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            @Override
            protected List<RecognizeResult> doInBackground(InputStream... params) {
                try{
                    publishProgress("Recognizing....");
                    List<RecognizeResult> result = emotionServiceClient.recognizeImage(params[0]);
                    return result;
                }
                catch (Exception ex)
                {
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                mDialog.show();
            }

            @Override
            protected void onPostExecute(List<RecognizeResult> recognizeResults) {
                mDialog.dismiss();
                for(RecognizeResult res : recognizeResults)
                {
                    String status = getEmo(res);
                    textView.setText(status);
                    imageView.setImageBitmap(ImageHelper.drawRectOnBitmap(bitmap,res.faceRectangle,status));
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mDialog.setMessage(values[0]);
            }
        };

        emotionTask.execute(inputStream);
    }
    private String getEmo(RecognizeResult res) {
        List<Double> list = new ArrayList<>();
        Scores scores = res.scores;

        list.add(scores.anger);
        list.add(scores.happiness);
        list.add(scores.contempt);
        list.add(scores.disgust);
        list.add(scores.fear);
        list.add(scores.neutral);
        list.add(scores.sadness);
        list.add(scores.surprise);

        Collections.sort(list);

        double maxNum = list.get(list.size() - 1);
        if(maxNum == scores.anger)
            return "Anger";
        else if(maxNum == scores.happiness)
            return "Happy";
        else if(maxNum == scores.contempt)
            return "Contemp";
        else if(maxNum == scores.disgust)
            return "Disgust";
        else if(maxNum == scores.fear)
            return "Fear";
        else if(maxNum == scores.neutral)
            return "Neutral";
        else if(maxNum == scores.sadness)
            return "Sadness";
        else if(maxNum == scores.surprise)
            return "Surprise";
        else
            return "Neutral";

    }

    public void NextClicked(View view) {
        Intent i = new Intent(this, DescriptionActivity.class);
        startActivity(i);
    }
}
