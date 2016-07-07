package it.unipg.studenti.ai.snails.utils;

import android.provider.ContactsContract;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Helpers {
    //static String paramsPath = "/mnt/shared/android_shared/";
    static String paramsPath = "/mnt/sdcard/download/";

    static Point p1 = new Point();
    static Point p2 = new Point();
    static Point p3 = new Point();
    static Point p4 = new Point();

    public static boolean isContourSquare(MatOfPoint thisContour) {

        Rect ret = null;

        MatOfPoint2f thisContour2f = new MatOfPoint2f();
        MatOfPoint approxContour = new MatOfPoint();
        MatOfPoint2f approxContour2f = new MatOfPoint2f();

        thisContour.convertTo(thisContour2f, CvType.CV_32FC2);

        Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);

        approxContour2f.convertTo(approxContour, CvType.CV_32S);

        if (approxContour.size().height == 4) {
            ret = Imgproc.boundingRect(approxContour);
        }

        return (ret != null);
    }

    public static List<MatOfPoint> getSquareContours(List<MatOfPoint> contours) {

        List<MatOfPoint> squares = null;

        for (MatOfPoint c : contours) {

            if (Helpers.isContourSquare(c))
            {
                if (squares == null)
                    squares = new ArrayList<MatOfPoint>();
                squares.add(c);
            }
        }

        return squares;
    }

    public static Mat findLargestRectangle(Mat original_image) {
        Mat imgProcess = new Mat();


        Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2HSV);
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(0,0), 7, 7, 0);

        java.util.List<Mat> hsvPlanes = new LinkedList<Mat>();
        Core.split(imgProcess, hsvPlanes);

        //Imgproc.adaptiveThreshold(hsvPlanes.get(0), hsvPlanes.get(0), 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 5, 0);
        //Imgproc.adaptiveThreshold(hsvPlanes.get(1), hsvPlanes.get(1), 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 5, 0);
        Imgproc.adaptiveThreshold(hsvPlanes.get(2), hsvPlanes.get(2), 235, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 0);
        //Core.merge(hsvPlanes, imgProcess);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(hsvPlanes.get(2), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );

        //List<MatOfPoint> squareContours = getSquareContours(contours);

        double maxArea = -1;
        MatOfPoint temp_contour;// = squareContours.get(0); //the largest is at the index 0 for starting point
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
                    largest_contours.add(temp_contour);
                }
            }
        }



        double temp_double[] = maxCurve.get(0, 0);
        if(temp_double!=null) {
            p1 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p1.x, p1.y), 5, new Scalar(255, 0, 0), 5); //p1 is colored red
        }

        temp_double = maxCurve.get(1, 0);
        if(temp_double!=null) {
            p2 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p2.x, p2.y), 5, new Scalar(0, 255, 0), 5); //p2 is colored green
        }

        temp_double = maxCurve.get(2, 0);
        if(temp_double!=null) {
            p3 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p3.x, p3.y), 5, new Scalar(0, 0, 255), 5); //p3 is colored blue
        }

        temp_double = maxCurve.get(3, 0);
        if(temp_double!=null) {
            p4 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p4.x, p4.y), 5, new Scalar(0, 255, 255), 5); //p1 is colored violet
        }

        System.out.println("PUNTI" + p1+p2);
        return original_image;

    }

    public static Mat cropArea(Mat original_image) {


        //TAGLIARE IL RETTANGOLO
        Mat mask = new Mat(original_image.rows(), original_image.cols(), CvType.CV_8UC1);
        for(int i=0; i<mask.cols(); i++)
            for(int j=0; j<mask.rows(); j++)
                mask.put(j, i, 0);

        // Create Polygon from vertices
        MatOfPoint2f ROI_Poly = new MatOfPoint2f();
        MatOfPoint2f Vertices = new MatOfPoint2f(p1,p2,p3,p4);
        Imgproc.approxPolyDP(new MatOfPoint2f(p1,p2,p3,p4), ROI_Poly, 1.0, true);

        // Fill polygon white
        MatOfPoint RP = new MatOfPoint();
        ROI_Poly.convertTo(RP, CvType.CV_32S);
        Imgproc.fillConvexPoly(mask, RP, new Scalar(255, 255, 255), 8, 0);

        // Create new image for result storage
        Mat imageDest = new Mat(original_image.rows(), original_image.cols(), CvType.CV_8UC1);

        // Cut out ROI and store it in imageDest
        original_image.copyTo(imageDest, mask);

        return imageDest;
    }

    public static Mat blackToGray(Mat src, int value){
        Mat ret = new Mat(src.rows(), src.cols(), CvType.CV_8UC1);
        for(int j = 0; j < ret.cols(); j++){
            for(int i = 0; i < ret.rows(); i++){
                ret.put(i,j,255);
                if(src.get(i, j)[0]==0){
                    ret.put(i,j,value);
                }
            }
        }
        return ret;
    }

    public static Mat findBlob(Mat original_image) {

        Mat imgProcess = new Mat();
        Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(11, 11), 33);

        Mat chls[] = new Mat[4];
        int val[] = new int[]{ 191, 127, 63 };
        chls[0] = new Mat(imgProcess.rows(), imgProcess.cols(), CvType.CV_8UC1);
        for (int c = 0; c < 4; c++)
        {
            if(c==0){
                Imgproc.threshold(imgProcess, chls[0], 255, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            } else {
                chls[c] = blackToGray(chls[c-1], val[c-1]);
            }
        }

        imgProcess = chls[7];

        /*int the_size = 1;
        Mat element;

        MatOfKeyPoint keyPointsL = new MatOfKeyPoint();
        FeatureDetector fdL = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        fdL.read(paramsPath + "sbd.params.L.xml");
        fdL.detect(imgProcess, keyPointsL);
        List<KeyPoint> listKeyPointsL = keyPointsL.toList();
        for (KeyPoint k : listKeyPointsL) {
            int radius = (int) (Math.sqrt(k.size / Math.PI)*k.size/10);
            Rect r = new Rect(new Point(k.pt.x-radius, k.pt.y-radius), new Size(2*radius, 2*radius));
            Mat m = new Mat(imgProcess, r);
            the_size = 1;
            element = Imgproc.getStructuringElement( Imgproc.CV_SHAPE_ELLIPSE, new Size(2*the_size + 1, 2*the_size+1 ), new Point( the_size, the_size ) );
            for(int v = 0; v < ((int)k.size/5.8); v++) {
                Imgproc.dilate(m, m, element);
                //Imgproc.GaussianBlur(m, m, new Size(7, 7), 50);
            }
        }
        for (KeyPoint k:listKeyPointsL) {
            int radius = (int) (Math.sqrt(k.size / Math.PI)*k.size/10);
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), 1, new Scalar(0, 255, 0), 1); //p1 is colored violet
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 255, 0), 1);

            //Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            //Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 0, 0), 1); //p1 is colored violet
        }
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(3, 3), 15);*/
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /*MatOfKeyPoint keyPointsM = new MatOfKeyPoint();
        FeatureDetector fdM = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        fdM.read(paramsPath + "sbd.params.M.xml");
        fdM.detect(imgProcess, keyPointsM);
        List<KeyPoint> listKeyPointsM = keyPointsM.toList();
        for (KeyPoint k : listKeyPointsM) {
            int radius = (int) (Math.sqrt(k.size / Math.PI)*k.size/8);
            Rect r = new Rect(new Point(k.pt.x-radius, k.pt.y-radius), new Size(2*radius, 2*radius));
            Mat m = new Mat(imgProcess, r);
            the_size = 1;
            element = Imgproc.getStructuringElement( Imgproc.CV_SHAPE_ELLIPSE, new Size(2*the_size + 1, 2*the_size+1 ), new Point( the_size, the_size ) );
            for(int v = 0; v < ((int)k.size/9); v++) {
                Imgproc.dilate(m, m, element);
            }
        }
        for (KeyPoint k:listKeyPointsM) {
            int radius = (int) (Math.sqrt(k.size / Math.PI)*k.size/8);
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), 1, new Scalar(0, 255, 0), 1); //p1 is colored violet
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 255, 0), 1);

            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 0, 0), 1); //p1 is colored violet
        }*/

        /*MatOfKeyPoint keyPointsUnder6000 = new MatOfKeyPoint();
        FeatureDetector fd1 = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        fd1.read(paramsPath + "sbd.params.U6K.xml");
        fd1.detect(imgProcess, keyPointsUnder6000);
        List<KeyPoint> listPointsUnder6000 = keyPointsUnder6000.toList();
        for (KeyPoint k : listPointsUnder6000) {
            int radius = (int)Math.sqrt(k.size / Math.PI)*smallRadiusFactor;
            Rect r = new Rect(new Point(k.pt.x-(radius/2), k.pt.y-(radius/2)), new Size(radius, radius));
            Mat m = new Mat(imgProcess, r);
            the_size = 1;
            element = Imgproc.getStructuringElement( Imgproc.CV_SHAPE_ELLIPSE, new Size(2*the_size + 1, 2*the_size+1 ), new Point( the_size, the_size ) );
            for(int v = 0; v < (radius/13); v++) {
                Imgproc.dilate(m, m, element);
            }
        }
        for (KeyPoint k:listPointsUnder6000) {
            int radius = (int)Math.sqrt(k.size / Math.PI);
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), 1, new Scalar(0, 255, 0), 1); //p1 is colored violet
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), radius*10, new Scalar(0, 255, 0), 1);

            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), radius*10, new Scalar(0, 0, 0), 1); //p1 is colored violet
        }*/



       /* MatOfKeyPoint keyPointsOver6000 = new MatOfKeyPoint();
        FeatureDetector fd2 = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        fd2.read(paramsPath + "sbd.params.O6K.xml");
        fd2.detect(imgProcess, keyPointsOver6000);
        List<KeyPoint> listPointsOver6000 = keyPointsOver6000.toList();
        for (KeyPoint k : listPointsOver6000) {
            int radius = (int)Math.sqrt(k.size / Math.PI)*bigRadiusFactor;
            Rect r = new Rect(new Point(k.pt.x-(radius/2), k.pt.y-(radius/2)), new Size(radius, radius));
            Mat m = new Mat(imgProcess, r);
            the_size = 1;
            element = Imgproc.getStructuringElement( Imgproc.CV_SHAPE_ELLIPSE, new Size(2*the_size + 1, 2*the_size+1 ), new Point( the_size, the_size ) );
            for(int v = 0; v < (radius/13); v++) {
                Imgproc.dilate(m, m, element);
            }
        }
*/
        /*MatOfKeyPoint keyPointsUltimi = new MatOfKeyPoint();
        fdL.detect(imgProcess, keyPointsUltimi);
        List<KeyPoint> listkeyPointsUltimi = keyPointsUltimi.toList();*/
        //listkeyPointsUltimi.removeAll(listPointsUnder6000);

        /*for (KeyPoint k:listPointsUnder6000) {
            int radius = (int)Math.sqrt(k.size / Math.PI);
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), 1, new Scalar(0, 255, 0), 1); //p1 is colored violet
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), radius*10, new Scalar(0, 255, 0), 1);

            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), radius*10, new Scalar(0, 0, 0), 1); //p1 is colored violet
        }
        for (KeyPoint k:listPointsOver6000) {
            int radius = (int)Math.sqrt(k.size / Math.PI)*12;
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), radius, new Scalar(255, 255, 255), 5);

            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 0, 0), 1); //p1 is colored violet
        }
*/

        //ArrayList result = new ArrayList();
        //result.add(imgProcess);
        //result.add(0); //keyPointsUltimi.rows());
        //result.add(original_image);
        return imgProcess;
    }

    public static Mat[] findROI(Mat src) {
        Mat maskedImage = new Mat(src.size(), src.type());
        Mat binaryImg = new Mat(src.size(), src.type());

        Imgproc.cvtColor(src, binaryImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(binaryImg, binaryImg, new Size(27, 27), 10000);

        int the_size = 2;
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * the_size + 1, 2 * the_size + 1), new Point(the_size, the_size));
        for (int v = 0; v < 5; v++) {
            Imgproc.dilate(binaryImg, binaryImg, element);
        }

        Imgproc.threshold(binaryImg, binaryImg, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        for (int v = 0; v < 6; v++) {
            Imgproc.erode(binaryImg, binaryImg, element);
        }

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binaryImg, contours,new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );

        int idx = -1;
        int tmp = -1;
        for (MatOfPoint c : contours) {
            if(c.rows() > tmp) {
                tmp = c.rows();
                idx = contours.indexOf(c);
            }
        }

        Imgproc.drawContours(src, contours, idx, new Scalar(255,255,255),3  );

        Mat mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(255,255,255));
        Core.bitwise_not(mask,mask);

        Imgproc.drawContours(mask, contours, idx, new Scalar(255,255,255),3  );
        Imgproc.floodFill(mask, new Mat(), new Point(mask.size().width/2, mask.size().height/2) , new Scalar(255,255,255) );

        src.copyTo(maskedImage,mask); // creates masked Image and copies it to maskedImage
        Mat[] ret = new Mat[2];
        ret[0] = maskedImage;
        ret[1] = mask;
        //return maskedImage;
        return ret;
    }

    public static Mat SnailsPreDetect(Mat src) {
        List<Mat> dstCh = new ArrayList<>();
        int size = src.rows()/4;
        if(size%2 == 0) size--;
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 1 + 1, 2 * 1 + 1), new Point(1, 1));
        Core.split(src, dstCh);

        Imgproc.adaptiveThreshold(dstCh.get(2), dstCh.get(2), 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, size, 0);
        Imgproc.morphologyEx(dstCh.get(2), dstCh.get(2), Imgproc.MORPH_OPEN, element, new Point(0,0), 2);
        Imgproc.dilate(dstCh.get(2), dstCh.get(2), element, new Point(-1,-1), 10);

        return dstCh.get(2);
    }

    public static MatOfKeyPoint SimpleBlobDetector(Mat src, String params){
        File f = new File(paramsPath, params);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        if(!f.exists()) fd.write(f.getAbsolutePath());
        fd.read(f.getAbsolutePath());
        fd.detect(src, keyPoints);
        return keyPoints;
    }

    public static Mat[] SnailsDetect(Mat imgProcess, Mat original_image) {
        ArrayList<Mat> ret = new ArrayList<>();
        Mat imgProcessed = new Mat(imgProcess.size(), imgProcess.type());

        Imgproc.floodFill(imgProcess, new Mat(), new Point(2,2), new Scalar(255,255,255));

        imgProcess.copyTo(imgProcessed);

        MatOfKeyPoint kpsLARGE = SimpleBlobDetector(imgProcessed, "sbd.params.L.xml");
        MatOfKeyPoint kpsMEDIUM = SimpleBlobDetector(imgProcessed, "sbd.params.M.xml");
        MatOfKeyPoint kpsSMALL = SimpleBlobDetector(imgProcessed, "sbd.params.S.xml");

        //Features2d.drawKeypoints(imgProcessed, kpsLARGE, imgProcessed, new Scalar(255,0,0),Features2d.DRAW_RICH_KEYPOINTS);
        //Features2d.drawKeypoints(imgProcessed, kpsMEDIUM, imgProcessed, new Scalar(0,255,0),Features2d.DRAW_RICH_KEYPOINTS);
        //Features2d.drawKeypoints(imgProcessed, kpsSMALL, imgProcessed, new Scalar(0,0,255),Features2d.DRAW_RICH_KEYPOINTS);

        KeyPoint[] kpsLargeArray = kpsLARGE.toArray();

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imgProcessed, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );

        /// Approximate contours to polygons + get bounding rects and circles
        List<Rect> boundRect = new ArrayList<>(contours.size());
        List<Point> center = new ArrayList<>();
        List<float[]> radius = new ArrayList<>();

        List<MatOfPoint> matOfPointList = new ArrayList<>();
        MatOfPoint2f matOfPoint2fContoursPoly = new MatOfPoint2f();

        for( int i = 0; i < contours.size(); i++ )
        {
            MatOfPoint2f matOfPoint2fContours = new MatOfPoint2f(contours.get(i).toArray());

            Imgproc.approxPolyDP(matOfPoint2fContours, matOfPoint2fContoursPoly , 3., true );

            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint2fContoursPoly.convertTo(matOfPoint, CvType.CV_32S);
            boundRect.add(i, Imgproc.boundingRect( matOfPoint ) );

            //Imgproc.minEnclosingCircle( matOfPoint2fContoursPoly, center.get(i), radius.get(i) );

            matOfPointList.add(matOfPoint);
        }

        /// Draw polygonal contour + bonding rects + circles
        /*Mat drawing = Mat.zeros(imgProcess.size(), CvType.CV_8UC3);
        for( int i = 0; i< contours.size(); i++ )
        {
            Scalar color = new  Scalar( 200,156,53 );
            Imgproc.drawContours(imgProcess, matOfPointList, i, color, 1);
            Imgproc.rectangle(imgProcess, boundRect.get(i).tl(), boundRect.get(i).br(), color, 2, 8, 0 );
        }*/


        /*List<Rect> secBoundRect = new ArrayList<>(boundRect);
        for (Rect r:secBoundRect) {

        }*/

        int count = 0;
        List<Mat> submats = new ArrayList<>();
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 1 + 1, 2 * 1 + 1), new Point(1, 1));

        for(KeyPoint kp:kpsLargeArray){
            for (Rect rect:boundRect) {
                if(kp.pt.inside(rect) && (rect.area()< (original_image.size().area()/10*9))){
                    Scalar color = new  Scalar( 200,156,53 );
                    //Imgproc.drawContours(imgProcess, matOfPointList, boundRect.indexOf(rect), color, 15);
                    //Imgproc.rectangle(imgProcess, rect.tl(), rect.br(), color, 2, 8, 0 );
                    Mat m = imgProcess.submat(rect);
                    int iterations = (int) kp.size/10;
                    Imgproc.dilate(m, m, element, new Point(-1,-1), iterations);
                    submats.add(m);
                    count++;
                    //break;
                }
            }
        }

        //ret.add(imgProcessed);
        ret.add(imgProcess);
        ret.addAll(submats);

        /*MatOfPoint matOfPoint = new MatOfPoint();
        KeyPoint[] keyPoints = kpsLARGE.toArray();

        matOfPoint.setTo(kpsLARGE);
        for (KeyPoint kp: keyPoints) {


        }

        Point center = new Point();
        float[] radius = new float[1];
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        kpsLARGE.convertTo(matOfPoint2f, matOfPoint2f.type());*/
        //Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);

        /*MatOfKeyPoint keyPointsM = new MatOfKeyPoint();
        FeatureDetector fdM = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        fdM.read(paramsPath + "sbd.params.M.xml");
        fdM.detect(imgProcess, keyPointsM);
        List<KeyPoint> listKeyPointsM = keyPointsM.toList();
        for (KeyPoint k : listKeyPointsM) {
            int radius = (int) (Math.sqrt(k.size / Math.PI)*k.size/8);
            Rect r = new Rect(new Point(k.pt.x-radius, k.pt.y-radius), new Size(2*radius, 2*radius));
            Mat m = new Mat(imgProcess, r);
            Mat element = Imgproc.getStructuringElement( Imgproc.CV_SHAPE_ELLIPSE, new Size(2*1 + 1, 2*1+1 ), new Point( 1, 1 ) );
            for(int v = 0; v < ((int)k.size/9); v++) {
                Imgproc.dilate(m, m, element, new Point(-1,-1), 1);
            }
        }
        for (KeyPoint k:listKeyPointsM) {
            int radius = (int) (Math.sqrt(k.size / Math.PI)*k.size/8);
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), 1, new Scalar(0, 255, 0), 1); //p1 is colored violet
            Imgproc.circle(original_image, new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 255, 0), 1);

            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), 1, new Scalar(255, 255, 255), 1); //p1 is colored violet
            Imgproc.circle(imgProcess    , new Point(k.pt.x, k.pt.y), radius, new Scalar(0, 0, 0), 1); //p1 is colored violet
        }*/

        Mat[] r = new Mat[ret.size()];
        ret.toArray(r);
        return r;
    }

    public static Mat FunnyElab(Mat src){
        Mat ret = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 1 + 1, 2 * 1 + 1), new Point(1, 1));
        List<Mat> dstCh = new ArrayList<>();
        Core.split(src, dstCh);

        Imgproc.adaptiveThreshold(dstCh.get(0), dstCh.get(0), 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 521, 0);
        Imgproc.morphologyEx(dstCh.get(0), dstCh.get(0),Imgproc.MORPH_GRADIENT, element );

        Imgproc.adaptiveThreshold(dstCh.get(1), dstCh.get(1), 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 3, 0);
        Imgproc.GaussianBlur(dstCh.get(1), dstCh.get(1),new Size(3,3), 33);
        Imgproc.threshold(dstCh.get(1), dstCh.get(1), 130, 255, Imgproc.THRESH_BINARY);

        Imgproc.adaptiveThreshold(dstCh.get(2), dstCh.get(2), 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 521, 0);
        Imgproc.morphologyEx(dstCh.get(2), dstCh.get(2), Imgproc.MORPH_OPEN, element, new Point(0,0), 2);
        Imgproc.dilate(dstCh.get(2), dstCh.get(2), element, new Point(-1,-1), 10);

        Core.merge(dstCh, ret);

//        Imgproc.cvtColor(ret, ret, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.threshold(ret, ret, 0, 255, Imgproc.THRESH_BINARY_INV);
//        Imgproc.morphologyEx(ret, ret,Imgproc.MORPH_CLOSE, element );
//        Imgproc.morphologyEx(ret, ret,Imgproc.MORPH_CLOSE, element );

        return ret;
    }

}
