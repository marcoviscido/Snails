package it.unipg.studenti.ai.snails;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.unipg.studenti.ai.snails.utils.Helpers;

public class ElabActivity extends AppCompatActivity {
    String imageAbsPath;
    Bitmap origBmpImage;
    Mat origMatImage;
    //String filename;
    //ProgressBar progressBar;
    ProgressDialog progress;

    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    TextView textView7;
    TextView textView8;
    TextView textView9;

    SubsamplingScaleImageView imgView1;
    SubsamplingScaleImageView imgView2;
    SubsamplingScaleImageView imgView3;
    SubsamplingScaleImageView imgView4;
    SubsamplingScaleImageView imgView5;
    SubsamplingScaleImageView imgView6;
    SubsamplingScaleImageView imgView7;
    SubsamplingScaleImageView imgView8;
    SubsamplingScaleImageView imgView9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elab);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);
        textView6 = (TextView)findViewById(R.id.textView6);
        textView7 = (TextView)findViewById(R.id.textView7);
        textView8 = (TextView)findViewById(R.id.textView8);
        textView9 = (TextView)findViewById(R.id.textView9);

        imgView1 = (SubsamplingScaleImageView)findViewById(R.id.imgView1);
        imgView2 = (SubsamplingScaleImageView)findViewById(R.id.imgView2);
        imgView3 = (SubsamplingScaleImageView)findViewById(R.id.imgView3);
        imgView4 = (SubsamplingScaleImageView)findViewById(R.id.imgView4);
        imgView5 = (SubsamplingScaleImageView)findViewById(R.id.imgView5);
        imgView6 = (SubsamplingScaleImageView)findViewById(R.id.imgView6);
        imgView7 = (SubsamplingScaleImageView)findViewById(R.id.imgView7);
        imgView8 = (SubsamplingScaleImageView)findViewById(R.id.imgView8);
        imgView9 = (SubsamplingScaleImageView)findViewById(R.id.imgView9);

        Bundle bd = getIntent().getExtras();
        if(bd != null)
        {
            imageAbsPath = (String) bd.get("imageAbsPath");
            //filename = (String) bd.get("filename");
            origBmpImage = BitmapFactory.decodeFile(imageAbsPath);
        }
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

    private class LongOperation extends AsyncTask<Mat, Void, ArrayList<Bitmap>> {
        protected void onPreExecute() {
            progress.show();
        }

        protected ArrayList<Bitmap> doInBackground(Mat... mats) {
            ArrayList<Bitmap> ret = new ArrayList();
            Mat[] imgProcessed = Helpers.findROI(mats[0]);

            ret.add(Bitmap.createBitmap(imgProcessed[1].cols(), imgProcessed[1].rows(), Bitmap.Config.ARGB_8888));
            Utils.matToBitmap(imgProcessed[1], ret.get(0));

            ret.add(Bitmap.createBitmap(imgProcessed[0].cols(), imgProcessed[0].rows(), Bitmap.Config.ARGB_8888));
            Utils.matToBitmap(imgProcessed[0], ret.get(1));

            Mat snailsPreDetect = Helpers.SnailsPreDetect(imgProcessed[0]);
            ret.add(Bitmap.createBitmap(snailsPreDetect.cols(), snailsPreDetect.rows(), Bitmap.Config.ARGB_8888));
            Utils.matToBitmap(snailsPreDetect, ret.get(2));

            Mat snailsDetect = Helpers.SnailsDetect(snailsPreDetect, mats[0]);
            ret.add(Bitmap.createBitmap(snailsDetect.cols(), snailsDetect.rows(), Bitmap.Config.ARGB_8888));
            Utils.matToBitmap(snailsDetect, ret.get(3));


            /*Mat funny = Helpers.FunnyElab(imgProcessed[0]);
            ret.add(Bitmap.createBitmap(funny.cols(), funny.rows(), Bitmap.Config.ARGB_8888));
            Utils.matToBitmap(funny, ret.get(7));*/

            return ret;
        }

        protected void onProgressUpdate() {
            // Executes whenever publishProgress is called from doInBackground
            // Used to update the progress indicator
            //progressBar.setProgress(values[0]);
        }

        protected void onPostExecute(ArrayList<Bitmap> result) {
            textView1.setText("Original image:");
            imgView1.setImage(ImageSource.bitmap(origBmpImage));
            textView2.setText("Pre ROI detection (2Gray + GaussianBlur + Dilate + Threshold):");
            imgView2.setImage(ImageSource.bitmap(result.get(0)));
            textView3.setText("ROI detection:");
            imgView3.setImage(ImageSource.bitmap(result.get(1)));
            textView4.setText("Snails pre-detection:");
            imgView4.setImage(ImageSource.bitmap(result.get(2)));
            textView5.setText("Snails detection:");
            imgView5.setImage(ImageSource.bitmap(result.get(3)));
            /*textView6.setText("Pre Snails detection 3:");
            imgView6.setImage(ImageSource.bitmap(result.get(4)));
            textView7.setText("Merged channels:");
            imgView7.setImage(ImageSource.bitmap(result.get(5)));
            textView8.setText("Second Elaboration:");
            imgView8.setImage(ImageSource.bitmap(result.get(6)));
            textView9.setText("Funny image:");
            imgView9.setImage(ImageSource.bitmap(result.get(7)));*/
            try {
                //FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
                File f = new File("/mnt/shared/android_shared/", UUID.randomUUID().toString()+".jpg");
                FileOutputStream fos = new FileOutputStream(f);
                result.get(6).compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                //System.out.println("Salvato in: "+getFilesDir()+"/"+filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
            progress.dismiss();
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    origMatImage=new Mat();
                    Utils.bitmapToMat(origBmpImage, origMatImage);
                    LongOperation asyncTask = new LongOperation();
                    asyncTask.execute(origMatImage);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
