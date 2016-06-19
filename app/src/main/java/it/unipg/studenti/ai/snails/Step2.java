package it.unipg.studenti.ai.snails;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;

public class Step2 extends AppCompatActivity {
    SubsamplingScaleImageView imgView1;
    Mat imgToProcess;
    String filename;
    ProgressDialog progress;

/*    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imgToProcess=new Mat();
                    GreyScaleStep asyncTask = new GreyScaleStep();
                    asyncTask.execute(imgUrl);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");

        Button btnAvanti = (Button)findViewById(R.id.button5);
        imgView1 = (SubsamplingScaleImageView)findViewById(R.id.imageViewStep2);

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
                    GreyScaleStep asyncTask = new GreyScaleStep();
                    asyncTask.execute(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        btnAvanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Step2.this, Step3.class);
                intent.putExtra("filename", filename );
                startActivity(intent);*/
            }
        });


    }

/*    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }*/

    private class GreyScaleStep extends AsyncTask<Bitmap, Void, Bitmap> {
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
            //progressBar.setVisibility(ProgressBar.VISIBLE);
            progress.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmaps) {
            // Some long-running task like downloading an image.
            Utils.bitmapToMat(bitmaps[0], imgToProcess);
            Imgproc.cvtColor(imgToProcess, imgToProcess, Imgproc.COLOR_RGB2Luv);
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
            imgView1.setImage(ImageSource.bitmap(result));
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



