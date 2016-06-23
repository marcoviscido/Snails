package it.unipg.studenti.ai.snails.utils;

import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.FeatureDetector;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * Created by Marco on 18/06/2016.
 */
public class Helpers {
    public static Mat findLargestRectangle(Mat original_image) {
        Mat imgProcess = new Mat();
        Point pone = new Point();
        Point ptwo = new Point();
        Point pthree = new Point();
        Point pfour = new Point();


       //preprocessing image


        //METODO PER ELIMINARE LO SFONDO FALLIMENTARE
        /*Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2HSV);
        java.util.List<Mat> hsvPlanes = new LinkedList<Mat>();
        Core.split(imgProcess, hsvPlanes);
        java.util.List<Mat> matList = new LinkedList<Mat>();
        matList.add(imgProcess);
        Mat hist_hue = new Mat();
        MatOfInt histSize = new MatOfInt(180);
        Imgproc.calcHist(matList, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));
        int average = 0;
        for (int h = 0; h < 180; h++) average += (hist_hue.get(h, 0)[0] * h);
        average = average / (int)imgProcess.size().height / (int)imgProcess.size().width;
        Imgproc.threshold(hsvPlanes.get(0), imgProcess, average, 179.0, Imgproc.THRESH_BINARY);
        Imgproc.blur(imgProcess, imgProcess, new Size(5, 5));
        Imgproc.dilate(imgProcess, imgProcess, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(imgProcess, imgProcess, new Mat(), new Point(-1, -1), 3);

        Mat foreground = new Mat(original_image.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        original_image.copyTo(foreground, imgProcess);*/






        Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(1, 1), 1);
        //CANNY
        Imgproc.Canny(imgProcess, imgProcess, 50 ,50);
        //OTSU
        //Imgproc.threshold(imgProcess, imgProcess, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);


        //CERCA I CONTORNI

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imgProcess, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


        double maxArea = -1;
        int maxAreaIdx = -1;
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f maxCurve = new MatOfPoint2f();
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();



        for (int idx = 0; idx < contours.size(); idx++) {

            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);

                if (approxCurve.total() == 4) {
                    maxCurve = approxCurve;
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    largest_contours.add(temp_contour);
                }

            }
        }

        //create the new image here using the largest detected square
        Mat new_image = new Mat(imgProcess.size(), CvType.CV_8U); //we will create a new black blank image with the largest contour
        Imgproc.cvtColor(new_image, new_image, Imgproc.COLOR_BayerBG2RGB);
        //Imgproc.drawContours(original_image, largest_contours, -1, new Scalar(0, 255, 0), 3); //will draw the largest square/rectangle
        //Imgproc.cvtColor(imgProcess, imgProcess, Imgproc.COLOR_BayerBG2RGB);
        //Imgproc.drawContours(original_image, largest_contours, -1, new Scalar(0, 255, 0), 3);


        double temp_double[] = maxCurve.get(0, 0);
        if(temp_double!=null) {
            Point p1 = new Point(temp_double[0], temp_double[1]);
            pone = p1;
            //Core.circle(new_image, new Point(p1.x, p1.y), 20, new Scalar(255, 0, 0), 5); //p1 is colored red
            Imgproc.circle(new_image, new Point(p1.x, p1.y), 20, new Scalar(255, 0, 0), 5); //p1 is colored red
            //Imgproc.circle(original_image, new Point(p1.x, p1.y), 20, new Scalar(255, 0, 0), 5); //p1 is colored red
            //String temp_string = "Point 1: (" + p1.x + ", " + p1.y + ")";
        }

        temp_double = maxCurve.get(1, 0);
        if(temp_double!=null) {
            Point p2 = new Point(temp_double[0], temp_double[1]);
            ptwo=p2;
            //Core.circle(new_image, new Point(p2.x, p2.y), 20, new Scalar(0, 255, 0), 5); //p2 is colored green
            Imgproc.circle(new_image, new Point(p2.x, p2.y), 20, new Scalar(0, 255, 0), 5); //p2 is colored green
            //Imgproc.circle(original_image, new Point(p2.x, p2.y), 20, new Scalar(0, 255, 0), 5); //p2 is colored green
            //temp_string += "\nPoint 2: (" + p2.x + ", " + p2.y + ")";
        }

        temp_double = maxCurve.get(2, 0);
        if(temp_double!=null) {
            Point p3 = new Point(temp_double[0], temp_double[1]);
            pthree=p3;
            //Core.circle(new_image, new Point(p3.x, p3.y), 20, new Scalar(0, 0, 255), 5); //p3 is colored blue
            Imgproc.circle(new_image, new Point(p3.x, p3.y), 20, new Scalar(0, 0, 255), 5); //p3 is colored blue
            //Imgproc.circle(original_image, new Point(p3.x, p3.y), 20, new Scalar(0, 0, 255), 5); //p3 is colored blue
            //temp_string += "\nPoint 3: (" + p3.x + ", " + p3.y + ")";
        }

        temp_double = maxCurve.get(3, 0);
        if(temp_double!=null) {
            Point p4 = new Point(temp_double[0], temp_double[1]);
            pfour=p4;
            //Core.circle(new_image, new Point(p4.x, p4.y), 20, new Scalar(0, 255, 255), 5); //p1 is colored violet
            Imgproc.circle(new_image, new Point(p4.x, p4.y), 20, new Scalar(0, 255, 255), 5); //p1 is colored violet
            //Imgproc.circle(original_image, new Point(p4.x, p4.y), 20, new Scalar(0, 255, 255), 5); //p1 is colored violet
            //temp_string += "\nPoint 4: (" + p4.x + ", " + p4.y + ")";
        }

        //TextView temp_text = (TextView)findViewById(R.id.temp_text);
        //temp_text.setText(temp_string);
        //return new_image;
        //Rect rectCrop = new Rect((int)pone.x, (int)pone.y ,(int)(pthree.x-pone.x), (int)(pthree.y-pone.y));
        //Mat image_output= original_image.submat(rectCrop);

        MatOfPoint mat = new MatOfPoint();
        Rect boundingRect = new Rect();
        for(int i=0; i<largest_contours.size(); i++) {
            boundingRect = Imgproc.boundingRect(largest_contours.get(i));
        }
        Mat image_output= original_image.submat(boundingRect);

        return image_output;

    }

    public static ArrayList findBlob(Mat original_image) {
        Mat imgProcess = new Mat();
        Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(1, 1), 1);
        Imgproc.threshold(imgProcess, imgProcess, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Bitmap bmpOut = Bitmap.createBitmap(imgProcess.cols(), imgProcess.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgProcess, bmpOut);
        BlobDetection b = new BlobDetection(bmpOut);
        bmpOut = b.getBlob(bmpOut);
        Utils.bitmapToMat(bmpOut, imgProcess);

        ArrayList result = new ArrayList();
        result.add(imgProcess);
        result.add(b.getNumber());
        return result;
    }
}
