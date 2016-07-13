package it.unipg.studenti.ai.snails.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.Pair;

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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Helpers {

    public static boolean initParams(Context ctx){
        AssetManager am = ctx.getAssets();
        File destPath = ctx.getFilesDir();
        try {
            String[] files = am.list("params");
            for (String file:files) {
                File dstFile = new File(destPath, file);
                if(!dstFile.exists()){
                    InputStream in = am.open("params/" + file);
                    dstFile.createNewFile();
                    OutputStream out = new FileOutputStream(dstFile);
                    byte[] buffer = new byte[1024];
                    int read;
                    while((read = in.read(buffer)) != -1){
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.flush();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

        Imgproc.threshold(dstCh.get(2), dstCh.get(2), 255, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.morphologyEx(dstCh.get(2), dstCh.get(2), Imgproc.MORPH_OPEN, element, new Point(0,0), 2);
        Imgproc.dilate(dstCh.get(2), dstCh.get(2), element, new Point(-1,-1), 3);

        return dstCh.get(2);
    }

    private static MatOfKeyPoint SimpleBlobDetector(Mat src, String paramsPath, String params){
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        if(params!=null) {
            File f = new File(paramsPath, params);
            //if(!f.exists()) fd.write(f.getAbsolutePath());
            fd.read(f.getAbsolutePath());
            Log.i("SimpleBlobDetector", " params:" + f.getAbsolutePath().toString());
        }
        fd.detect(src, keyPoints);
        return keyPoints;
    }

    private static double DistBtwnPoints(Point p, Point q){
        double X_Diff = p.x - q.x;
        double Y_Diff = p.y - q.y;
        return Math.sqrt((X_Diff * X_Diff) + (Y_Diff * Y_Diff));
    }

    private static MatOfPoint2f myfindContours(Mat inputImg){
        Mat imgToFindContours = new Mat();
        inputImg.copyTo(imgToFindContours);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imgToFindContours, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );
        //List<Rect> boundRect = new ArrayList<>(contours.size());
        //List<MatOfPoint> matOfPointList = new ArrayList<>();
        MatOfPoint2f matOfPoint2fContoursPoly = new MatOfPoint2f();
        for( int i = 0; i < contours.size(); i++ )
        {
            MatOfPoint2f matOfPoint2fContours = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(matOfPoint2fContours, matOfPoint2fContoursPoly , 3., true );
            //MatOfPoint matOfPoint = new MatOfPoint();
            //matOfPoint2fContoursPoly.convertTo(matOfPoint, CvType.CV_32S);
            //Rect rect = Imgproc.boundingRect( matOfPoint );
            //boundRect.add(i, rect);
            //matOfPointList.add(matOfPoint);
        }
        return matOfPoint2fContoursPoly;
    }

    private static List<List<Mat>> SplitInSubmats(Mat input_image, Mat original_image, List<KeyPoint> blobDetectedKpList, List<Pair<Rect, KeyPoint>> outRectKpPairs ){
        List<MatOfPoint> contours = new ArrayList<>();
        Mat imgToFindContours = new Mat();
        input_image.copyTo(imgToFindContours);
        Imgproc.findContours(imgToFindContours, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );

        List<Rect> boundRect = new ArrayList<>(contours.size());
        //List<MatOfPoint> matOfPointList = new ArrayList<>();
        MatOfPoint2f matOfPoint2fContoursPoly = new MatOfPoint2f();
        for( int i = 0; i < contours.size(); i++ )
        {
            MatOfPoint2f matOfPoint2fContours = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(matOfPoint2fContours, matOfPoint2fContoursPoly , 3., true );
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint2fContoursPoly.convertTo(matOfPoint, CvType.CV_32S);
            Rect rect = Imgproc.boundingRect( matOfPoint );
            boundRect.add(i, rect);
            //matOfPointList.add(matOfPoint);
        }

        //outRectKpPairs = new ArrayList<>();
        for (KeyPoint kp : blobDetectedKpList) {
            double minDist = 10000000;
            Rect minDistRect = null;
            for (Rect r: boundRect) {
                if(r!=null) {
                    if (r.area() < (input_image.size().area() / 10 * 9)) {
                        if(kp.pt.inside(r)) {
                            Point center = new Point((r.x + r.width) / 2, (r.y + r.height) / 2);
                            double dist = DistBtwnPoints(center, kp.pt);
                            if (dist < minDist) {
                                minDist = dist;
                                minDistRect = r;
                            }
                        }
                    } else {
                        boundRect.set(boundRect.indexOf(r), null);
                    }
                }
            }
            if(minDistRect!=null) {
                outRectKpPairs.add(new Pair<Rect, KeyPoint>(minDistRect, kp));
                boundRect.set(boundRect.indexOf(minDistRect), null);
            }
        }

        List<List<Mat>> ret = new ArrayList<>();
        ret.add(new ArrayList<Mat>());
        ret.add(new ArrayList<Mat>());
        ret.add(new ArrayList<Mat>());

        int maxColsNumber = 0;
        for (Pair pair : outRectKpPairs) {
            Rect rect = (Rect) pair.first;
            KeyPoint kp = (KeyPoint) pair.second;
            if (rect.area() < (input_image.size().area() / 10 * 9)) {
                Mat m = input_image.submat(rect);
                Mat mm = new Mat();
                m.copyTo(mm);
                Core.copyMakeBorder(mm, mm, 15, 15, 15, 15, Core.BORDER_CONSTANT, new Scalar(255,255,255));
                //ret.get(0).add(mm);
                //submatList.add(m);

                MatOfPoint2f snailCnt2f = myfindContours(mm);
                MatOfPoint mopSnailCnt = new MatOfPoint();
                snailCnt2f.convertTo(mopSnailCnt, CvType.CV_32S);
                List<MatOfPoint> snailCnt = new ArrayList<>();
                snailCnt.add(mopSnailCnt);
                int idx = -1;
                int tmp = -1;
                for (MatOfPoint c : snailCnt) {
                    if(c.rows() < tmp) {
                        tmp = c.rows();
                        idx = snailCnt.indexOf(c);
                    }
                }
                Mat mask = new Mat(mm.size(), CvType.CV_8UC1, new Scalar(255,255,255));
                Mat l = new Mat(mm.size(), CvType.CV_8UC1);
                Imgproc.drawContours(mask, snailCnt, idx, new Scalar(0,0,0),1);
                Imgproc.floodFill(mask, new Mat(), new Point(0,0) , new Scalar(0,0,0) );
                Core.bitwise_not(mask, mask);
                Core.bitwise_or(mm, mask, l);
                ret.get(0).add(mm);

                Mat n = original_image.submat(new Rect(rect.x - 15, rect.y - 15, rect.width + 30, rect.height + 30));
                Mat o = new Mat();
                Imgproc.cvtColor(n, o, Imgproc.COLOR_BGR2GRAY, 1);
                Imgproc.putText(o, "" + (outRectKpPairs.indexOf(pair)+1) , new Point(0,20), Core.FONT_HERSHEY_COMPLEX,0.7,new Scalar(255,255,255),2);
                ret.get(1).add(o);
                ret.get(2).add(l);
                //origSubmatList.add(o);

                if (mm.cols() > maxColsNumber) {
                    maxColsNumber = mm.cols();
                }
            }
        }

        for (Mat m: ret.get(0)) {
            int mLastCol = m.cols();
            if(mLastCol <= maxColsNumber){
                Mat col = new Mat(m.rows(), maxColsNumber-mLastCol, m.type());
                col.setTo(new Scalar(255));
                List<Mat> cols = new ArrayList<>();
                cols.add(m);
                cols.add(col);
                Mat newMc = new Mat();
                Core.hconcat(cols, newMc);
                Mat row = new Mat(40,newMc.cols(), newMc.type());
                row.setTo(new Scalar(255));
                List<Mat> rows = new ArrayList<>();
                Mat newMr = new Mat();
                rows.add(newMc);
                rows.add(row);
                Core.vconcat(rows, newMr);
                ret.get(0).set(ret.get(0).indexOf(m), newMr);
                //submatList.set(submatList.indexOf(m), newMr);
            }
        }

        for (Mat m: ret.get(1)) {
            int mLastCol = m.cols();
            if(mLastCol <= maxColsNumber){
                Mat col = new Mat(m.rows(), maxColsNumber-mLastCol, m.type());
                col.setTo(new Scalar(255));
                List<Mat> cols = new ArrayList<>();
                cols.add(m);
                cols.add(col);
                Mat newMc = new Mat();
                Core.hconcat(cols, newMc);
                Mat row = new Mat(40,newMc.cols(), newMc.type());
                row.setTo(new Scalar(255));
                List<Mat> rows = new ArrayList<>();
                Mat newMr = new Mat();
                rows.add(newMc);
                rows.add(row);
                Core.vconcat(rows, newMr);
                ret.get(1).set(ret.get(1).indexOf(m), newMr);
                //origSubmatList.set(origSubmatList.indexOf(m), newMr);
            }
        }

        for (Mat m: ret.get(2)) {
            int mLastCol = m.cols();
            Log.i("ret.get2_"+(ret.get(2).indexOf(m)+1), "numbers of cols is "+ mLastCol + " and maxColsNumber is "+ maxColsNumber );
            if(mLastCol <= maxColsNumber){
                Mat col = new Mat(m.rows(), maxColsNumber-mLastCol, m.type());
                col.setTo(new Scalar(255));
                List<Mat> cols = new ArrayList<>();
                cols.add(m);
                cols.add(col);
                Mat newMc = new Mat();
                Core.hconcat(cols, newMc);
                Mat row = new Mat(40,newMc.cols(), newMc.type());
                row.setTo(new Scalar(255));
                List<Mat> rows = new ArrayList<>();
                Mat newMr = new Mat();
                rows.add(newMc);
                rows.add(row);
                Core.vconcat(rows, newMr);
                ret.get(2).set(ret.get(2).indexOf(m), newMr);
                Log.i("ret.get2_"+(ret.get(2).indexOf(m)+1), "numbers of black pixels is "+ ((m.rows() * m.cols()) - Core.countNonZero(m)) );
                //origSubmatList.set(origSubmatList.indexOf(m), newMr);
            }
        }
        return ret;
    }

    public static Mat[] SnailsDetect(Mat input_image, Mat original_image, String appPath) {
        ArrayList<Mat> ret = new ArrayList<>();

        Mat origImgProc = new Mat();
        Imgproc.cvtColor(original_image, origImgProc, Imgproc.COLOR_BGRA2BGR);

        Imgproc.floodFill(input_image, new Mat(), new Point(2,2), new Scalar(255,255,255));
        Mat inImgProc = new Mat(input_image.size(), input_image.type());
        //Imgproc.cvtColor(original_image, origImgProc, Imgproc.COLOR_BGRA2BGR);
        input_image.copyTo(inImgProc);

        MatOfKeyPoint kpsSnails = SimpleBlobDetector(input_image, appPath, "/snails.params.xml");
        Features2d.drawKeypoints(origImgProc, kpsSnails, origImgProc, new Scalar(0,0,255),Features2d.DRAW_RICH_KEYPOINTS);
        Features2d.drawKeypoints(inImgProc, kpsSnails, inImgProc, new Scalar(0,0,255),Features2d.DRAW_RICH_KEYPOINTS);

        MatOfKeyPoint kpsLargeSnails = SimpleBlobDetector(input_image, appPath, "/largeSnails.params.xml");
        Features2d.drawKeypoints(origImgProc, kpsLargeSnails, origImgProc, new Scalar(255,0,0),Features2d.DRAW_RICH_KEYPOINTS);
        Features2d.drawKeypoints(inImgProc, kpsLargeSnails, inImgProc, new Scalar(255,0,0),Features2d.DRAW_RICH_KEYPOINTS);

        List<KeyPoint> allKps = new ArrayList<>();
        allKps.addAll(kpsSnails.toList());
        allKps.addAll(kpsLargeSnails.toList());

        List<Pair<Rect, KeyPoint>> rectKpPairs = new ArrayList<>();
        List<List<Mat>> splittedSubmats = SplitInSubmats(input_image, origImgProc, allKps, rectKpPairs);
        Mat submats = new Mat();
        Mat origSubmats = new Mat();
        Mat blobMats = new Mat();
        List<Mat> submatList = splittedSubmats.get(0);
        List<Mat> origSubmatList = splittedSubmats.get(1);
        List<Mat> blobMatsList = splittedSubmats.get(2);

        Mat outImgProc = new Mat();
        input_image.copyTo(outImgProc);

        for (Mat blobMat:blobMatsList) {
            List<KeyPoint> kpList = new ArrayList<>(SimpleBlobDetector(blobMat, appPath, "/blobs.params.xml").toList());
            Log.i("blobMat_"+(blobMatsList.indexOf(blobMat)+1), "numbers of keypoints is "+kpList.size());
            if(kpList.size() > 0){
                //Imgproc.rectangle(blobMat, new Point(0,0), new Point(blobMat.rows(), blobMat.cols()),new Scalar(100,100,100) );
                Mat element1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 2 + 1, 2 * 2 + 1), new Point(1, 1));
                int TotalNumberOfPixels = blobMat.rows() * blobMat.cols();
                int ZeroPixels = TotalNumberOfPixels - Core.countNonZero(blobMat);
                int iterations = (100*ZeroPixels/50000) -7 ;
                Log.i("blobMat_"+(blobMatsList.indexOf(blobMat)+1), "numbers of black pixels is "+ZeroPixels);
                Log.i("blobMat_"+(blobMatsList.indexOf(blobMat)+1), "numbers of dilate iterations is "+iterations);
                MatOfPoint2f snailCnt2f = myfindContours(blobMat);
                MatOfPoint2f snailCnt2fDO;
                int zoom = 0;
                do {
                    Imgproc.dilate(blobMat, blobMat, element1, new Point(-1,-1), iterations);
                    snailCnt2fDO = myfindContours(blobMat);
                    zoom++; // provare a scalare l'immagine per aumentare la sua area
                } while (snailCnt2f.rows() <= snailCnt2fDO.rows());

                Mat modMat = outImgProc.submat(rectKpPairs.get(blobMatsList.indexOf(blobMat)).first);
                Mat blobMatMod = new Mat();
                blobMat.copyTo(blobMatMod);
                Imgproc.resize(blobMatMod, blobMatMod, modMat.size());
                blobMatMod.copyTo(modMat);
            }
        }
        Core.vconcat(submatList, submats);
        Core.vconcat(origSubmatList, origSubmats);
        Core.vconcat(blobMatsList, blobMats);

        List<Mat> outSubmatsList = new ArrayList<>();
        Mat margine = new Mat(origSubmats.rows(), 50, origSubmats.type());
        margine.setTo(new Scalar(255));
        outSubmatsList.add(origSubmats);
        outSubmatsList.add(margine);
        outSubmatsList.add(submats);
        outSubmatsList.add(margine);
        outSubmatsList.add(blobMats);

        Mat outSubmats = new Mat();
        Core.hconcat(outSubmatsList, outSubmats);

        MatOfKeyPoint kpsAllSnails = SimpleBlobDetector(outImgProc, appPath, "/lastSnails.params.xml");
        Features2d.drawKeypoints(outImgProc, kpsAllSnails, outImgProc, new Scalar(0,255,0),Features2d.DRAW_RICH_KEYPOINTS);
        Mat textRes = new Mat(100, outImgProc.cols(), outImgProc.type(), new Scalar(255,255,255));
        Imgproc.putText(textRes, "Total snails: " + kpsAllSnails.rows(), new Point(20,60),Core.FONT_HERSHEY_SIMPLEX,3,new Scalar(0,0,0),5);

        List<Mat> endOutMat = new ArrayList<>();
        endOutMat.add(outImgProc);
        endOutMat.add(textRes);
        Core.vconcat(endOutMat, outImgProc);


        //ret.add(imgProcessed);
        ret.add(inImgProc);
        //ret.addAll(submats);
        ret.add(outSubmats);
        ret.add(outImgProc);

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
