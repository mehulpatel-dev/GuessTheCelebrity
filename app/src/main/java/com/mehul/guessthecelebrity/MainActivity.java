package com.mehul.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURL= new ArrayList<String>();
    ArrayList<String> celebName = new ArrayList<String>();

    int chosenCeleb;
    int correctAnswerLocation = 0;
    int incorrectAnswerLocation = 0;
    String[] answers = new String[4];

    ImageView celebImageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public class DownloadTask extends AsyncTask <String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String htmlContent = null;

            try{
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                InputStream inStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    htmlContent += current;
                }

            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class ImageDownloader extends AsyncTask <String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {
            //creating url that is passed to this method (params)
            //setting try catch for the possibility of failure in case of malformed url
            try{
                URL url = new URL(strings[0]);

                //set up url connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //download image and convert to Bitmap image
                connection.connect();
                //downloads whole input stream in one go
                InputStream inStream = connection.getInputStream();
                //convert stream to Bitmap
                Bitmap myBitmap = BitmapFactory.decodeStream(inStream);

                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public void celebChosen(View view){
        if(view.getTag().toString().equals(Integer.toString(correctAnswerLocation))){
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebName.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }

        generateCeleb();
    }

    public void generateCeleb(){
        Random rnum = new Random();
        Bitmap celebImg = null;
        ImageDownloader imgTask = new ImageDownloader();

        chosenCeleb = rnum.nextInt(celebURL.size());

        try {
            celebImg = imgTask.execute(celebURL.get(chosenCeleb)).get();
            celebImageView.setImageBitmap(celebImg);
            correctAnswerLocation = rnum.nextInt(4);

            for(int i = 0; i < 4; i++){
                if(i == correctAnswerLocation){
                    answers[i] = celebName.get(chosenCeleb);
                }else {
                    incorrectAnswerLocation = rnum.nextInt(celebURL.size());

                    while (incorrectAnswerLocation == chosenCeleb){
                        incorrectAnswerLocation = rnum.nextInt(celebURL.size());

                    }

                    answers[i] = celebName.get(incorrectAnswerLocation);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        button0.setText(answers[0]);
        button1.setText(answers[1]);
        button2.setText(answers[2]);
        button3.setText(answers[3]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask htmlTask = new DownloadTask();

        celebImageView = (ImageView) findViewById(R.id.celebImg);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        try {
            String htmlString = htmlTask.execute("http://www.posh24.se/kandisar").get();
            String[] splitString = htmlString.split("<div class=\"col-xs-12 col-sm-6 col-md-4\">");

            //pattern to find the img src in the html code
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitString[0]);

            while(m.find()){
                celebURL.add(m.group(1));
            }

            //pattern to find the name/alt of the image
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitString[0]);

            while(m.find()){
                celebName.add(m.group(1));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        generateCeleb();
    }
}
