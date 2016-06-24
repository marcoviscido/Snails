package it.unipg.studenti.ai.snails;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import it.unipg.studenti.ai.snails.utils.BlobDetection;
import it.unipg.studenti.ai.snails.utils.Helpers;

public class Step3 extends AppCompatActivity {
    SubsamplingScaleImageView imgView;
    Mat imgToProcess;
    String filename;
    ProgressDialog progress;
    int numberOfBlob;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step3);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");

        Button btnAvanti = (Button)findViewById(R.id.button5);
        imgView = (SubsamplingScaleImageView)findViewById(R.id.imageViewStep3);

        Bundle bd = getIntent().getExtras();
        if(bd != null)
        {
            filename = (String) bd.get("filename");
            Bitmap bitmap = null;
            try {
                File f = new File(getFilesDir()+"/"+filename);
                if (f.exists()) {
                    bitmap = BitmapFactory.decodeFile(getFilesDir()+"/"+filename);
                    imgToProcess=new Mat();
                    blobDetection asyncTask = new blobDetection();
                    asyncTask.execute(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


       /* btnAvanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Step2.this, Step3.class);
                intent.putExtra("filename", filename );
                startActivity(intent);
            }
        });*/


    }



    private class blobDetection extends AsyncTask<Bitmap, Void, Bitmap> {
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
            //progressBar.setVisibility(ProgressBar.VISIBLE);
            progress.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmaps) {
            // Some long-running task like downloading an image.
            Utils.bitmapToMat(bitmaps[0], imgToProcess);
            ArrayList result = new ArrayList();
            result = Helpers.findBlob(imgToProcess);
            imgToProcess = (Mat)result.get(0);
            numberOfBlob = (int)result.get(1);
            Bitmap bmpOut = Bitmap.createBitmap(imgToProcess.cols(), imgToProcess.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgToProcess, bmpOut);
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
            imgView.setImage(ImageSource.bitmap(result));
            try {
                FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
                result.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Hide the progress bar
            progress.dismiss();
        }
    }
}



