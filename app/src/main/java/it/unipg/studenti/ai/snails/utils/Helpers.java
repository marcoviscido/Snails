package it.unipg.studenti.ai.snails.utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco on 18/06/2016.
 */
public class Helpers {
    public static Mat findLargestRectangle(Mat original_image) {
        Mat imgSource = original_image.clone();
        //Mat untouched = original_image.clone();

        //convert the image to black and white
        Imgproc.cvtColor(imgSource, imgSource, Imgproc.COLOR_BGR2GRAY);

        //convert the image to black and white does (8 bit)
        Imgproc.Canny(imgSource, imgSource, 50, 50);

        //apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(imgSource, imgSource, new Size(1, 1), 1);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imgSource, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f maxCurve = new MatOfPoint2f();
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        int param = 20;
        for (int idx = 0; idx < contours.size(); idx++) {
            if(param > 30){ param=1; break; }
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                //Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);
                //System.out.println("Parametro: "+param);
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize/param, true);
                if (approxCurve.total() == 4) {
                    maxCurve = approxCurve;
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    largest_contours.add(temp_contour);
                } else {
                    idx--;
                    param++;
                }
            }
        }

        //create the new image here using the largest detected square
        Mat new_image = new Mat(imgSource.size(), CvType.CV_8U); //we will create a new black blank image with the largest contour
        Imgproc.cvtColor(new_image, new_image, Imgproc.COLOR_BayerBG2RGB);
        Imgproc.drawContours(new_image, contours, maxAreaIdx, new Scalar(255, 255, 255), 1); //will draw the largest square/rectangle

        double temp_double[] = maxCurve.get(0, 0);
        if(temp_double!=null) {
            Point p1 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(new_image, new Point(p1.x, p1.y), 20, new Scalar(255, 0, 0), 5); //p1 is colored red
            //Imgproc.circle(new_image, new Point(p1.x, p1.y), 20, new Scalar(255, 0, 0), 5); //p1 is colored red
            Imgproc.circle(original_image, new Point(p1.x, p1.y), 20, new Scalar(255, 0, 0), 5); //p1 is colored red
            //String temp_string = "Point 1: (" + p1.x + ", " + p1.y + ")";
        }

        temp_double = maxCurve.get(1, 0);
        if(temp_double!=null) {
            Point p2 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(new_image, new Point(p2.x, p2.y), 20, new Scalar(0, 255, 0), 5); //p2 is colored green
            //Imgproc.circle(new_image, new Point(p2.x, p2.y), 20, new Scalar(0, 255, 0), 5); //p2 is colored green
            Imgproc.circle(original_image, new Point(p2.x, p2.y), 20, new Scalar(0, 255, 0), 5); //p2 is colored green
            //temp_string += "\nPoint 2: (" + p2.x + ", " + p2.y + ")";
        }

        temp_double = maxCurve.get(2, 0);
        if(temp_double!=null) {
            Point p3 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(new_image, new Point(p3.x, p3.y), 20, new Scalar(0, 0, 255), 5); //p3 is colored blue
            //Imgproc.circle(new_image, new Point(p3.x, p3.y), 20, new Scalar(0, 0, 255), 5); //p3 is colored blue
            Imgproc.circle(original_image, new Point(p3.x, p3.y), 20, new Scalar(0, 0, 255), 5); //p3 is colored blue
            //temp_string += "\nPoint 3: (" + p3.x + ", " + p3.y + ")";
        }

        temp_double = maxCurve.get(3, 0);
        if(temp_double!=null) {
            Point p4 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(new_image, new Point(p4.x, p4.y), 20, new Scalar(0, 255, 255), 5); //p1 is colored violet
            //Imgproc.circle(new_image, new Point(p4.x, p4.y), 20, new Scalar(0, 255, 255), 5); //p1 is colored violet
            Imgproc.circle(original_image, new Point(p4.x, p4.y), 20, new Scalar(0, 255, 255), 5); //p1 is colored violet
            //temp_string += "\nPoint 4: (" + p4.x + ", " + p4.y + ")";
        }

        //TextView temp_text = (TextView)findViewById(R.id.temp_text);
        //temp_text.setText(temp_string);
        //return new_image;
        return original_image;
    }
}
