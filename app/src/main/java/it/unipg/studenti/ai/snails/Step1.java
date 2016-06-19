package it.unipg.studenti.ai.snails;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileOutputStream;

import it.unipg.studenti.ai.snails.utils.Helpers;

public class Step1 extends AppCompatActivity {
    SubsamplingScaleImageView imgView1;
    Mat imgToProcess;
    String imgUrl;
    String filename;
    //ProgressBar progressBar;
    ProgressDialog progress;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imgToProcess=new Mat();
                    RectangleDetection asyncTask = new RectangleDetection();
                    asyncTask.execute(imgUrl);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");

        //progressBar = (ProgressBar)findViewById(R.id.progressBar);
        Button btnAvanti = (Button)findViewById(R.id.button4);
        imgView1 = (SubsamplingScaleImageView)findViewById(R.id.imageView);

        Bundle bd = getIntent().getExtras();
        if(bd != null)
        {
            imgUrl = (String) bd.get("bmp");
            filename = (String) bd.get("filename");
        }

        btnAvanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Step1.this, Step2.class);
                intent.putExtra("filename", filename );
                startActivity(intent);
            }
        });


    }

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    private class RectangleDetection extends AsyncTask<String, Void, Bitmap> {
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
            //progressBar.setVisibility(ProgressBar.VISIBLE);
            progress.show();
        }

        protected Bitmap doInBackground(String... strings) {
            // Some long-running task like downloading an image.
            Bitmap bmp = BitmapFactory.decodeFile(strings[0]);
            Utils.bitmapToMat(bmp, imgToProcess);
            Mat imgProcessed = Helpers.findLargestRectangle(imgToProcess);
                Bitmap bmpOut = Bitmap.createBitmap(imgProcessed.cols(), imgProcessed.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(imgProcessed, bmpOut);
                return bmpOut;
        }

        protected void onProgressUpdate() {
            // Executes whenever publishProgress is called from doInBackground
            // Used to update the progress indicator
            //progressBar.setProgress(values[0]);
        }

        protected void onPostExecute(Bitmap result) {
            // This method is executed in the UIThread
            // with access to the result of the long running task
            //progressBar.setVisibility(ProgressBar.INVISIBLE);
            imgView1.setImage(ImageSource.bitmap(result));
            try {
                FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
                result.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                System.out.println("Salvato in: "+getFilesDir()+"/"+filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Hide the progress bar
            progress.dismiss();
        }
    }
}



