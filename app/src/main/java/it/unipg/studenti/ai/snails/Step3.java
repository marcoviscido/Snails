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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
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

        Button btnAvanti = (Button)findViewById(R.id.button5);
        imgViewElab = (SubsamplingScaleImageView)findViewById(R.id.imageViewStep3);
        imgViewOrig = (SubsamplingScaleImageView)findViewById(R.id.imageViewStepOrig);

        textView = (TextView)findViewById(R.id.textView);
        aSwitch = (Switch)findViewById(R.id.switch1);
        aSwitch.setVisibility(View.INVISIBLE);

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

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    imgViewElab.setVisibility(View.INVISIBLE);
                    imgViewOrig.setVisibility(View.VISIBLE);
                } else {
                    imgViewElab.setVisibility(View.VISIBLE);
                    imgViewOrig.setVisibility(View.INVISIBLE);
                }
            }
        });


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
            imgToProcess = (Mat)result.get(0);
            numberOfBlob = (int)result.get(1);
            Mat imgOrig = (Mat)result.get(2);
            Bitmap bmpOut = Bitmap.createBitmap(imgToProcess.cols(), imgToProcess.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgToProcess, bmpOut);
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
            textView.setText("Blob trovati: "+numberOfBlob);
            imgViewOrig.setImage(ImageSource.bitmap(bmpImgOrig));
            imgViewElab.setImage(ImageSource.bitmap(result));
            aSwitch.setVisibility(View.VISIBLE);
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



