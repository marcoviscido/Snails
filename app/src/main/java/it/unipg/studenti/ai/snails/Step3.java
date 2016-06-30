package it.unipg.studenti.ai.snails;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;

import it.unipg.studenti.ai.snails.utils.Helpers;

public class Step3 extends AppCompatActivity {
    AsyncTask asyncTask;
    SubsamplingScaleImageView imgViewElab;
    SubsamplingScaleImageView imgViewOrig;
    Mat imgToProcess;
    Bitmap bmpImgOrig;
    String filename;
    ProgressDialog progress;
    int numberOfBlob;
    TextView textView;
    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step3);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");

        imgViewElab = (SubsamplingScaleImageView)findViewById(R.id.imageViewStep3);
        imgViewOrig = (SubsamplingScaleImageView)findViewById(R.id.imageViewStepOrig);
        textView = (TextView)findViewById(R.id.textView);

        Button btnRefresh = (Button)findViewById(R.id.buttonRefresh);
        btnRefresh.setVisibility(View.INVISIBLE);

        aSwitch = (Switch)findViewById(R.id.switch1);
        aSwitch.setVisibility(View.INVISIBLE);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    imgViewElab.setVisibility(View.INVISIBLE);
                    imgViewOrig.setVisibility(View.VISIBLE);
                } else {
                    imgViewElab.setVisibility(View.VISIBLE);
                    imgViewOrig.setVisibility(View.INVISIBLE);
                }
            }
        });

        Bundle bd = getIntent().getExtras();
        if(bd != null)
        {
            filename = (String) bd.get("filename");
            filename = getFilesDir()+"/"+ filename;

            Bitmap bitmap;
            try {
                File f = new File(filename);
                if (f.exists()) {
                    bitmap = BitmapFactory.decodeFile(filename);
                    imgToProcess=new Mat();
                    asyncTask = new blobDetection().execute(bitmap);
                    btnRefresh.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap bitmap;
                    try {
                        File f = new File(filename);
                        if (f.exists()) {
                            bitmap = BitmapFactory.decodeFile(filename);
                            //imgToProcess=new Mat();
                            asyncTask = new blobDetection().execute(bitmap);
                            //btnRefresh.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
            ArrayList result;
            result = Helpers.findBlob(imgToProcess);
            Mat imgProcessed = (Mat)result.get(0);
            //imgToProcess = (Mat)result.get(0);
            numberOfBlob = (int)result.get(1);
            Mat imgOrig = (Mat)result.get(2);
            Bitmap bmpOut = Bitmap.createBitmap(imgProcessed.cols(), imgProcessed.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgProcessed, bmpOut);
            bmpImgOrig = Bitmap.createBitmap(imgOrig.cols(), imgOrig.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgOrig, bmpImgOrig);
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
            Toast.makeText(Step3.this, "done!", Toast.LENGTH_SHORT).show();
            textView.setText("Blob trovati: "+numberOfBlob);
            imgViewOrig.setImage(ImageSource.bitmap(bmpImgOrig));
            imgViewElab.setImage(ImageSource.bitmap(result));
            aSwitch.setVisibility(View.VISIBLE);
            /*try {
                FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
                result.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            // Hide the progress bar
            progress.dismiss();
        }
    }
}



