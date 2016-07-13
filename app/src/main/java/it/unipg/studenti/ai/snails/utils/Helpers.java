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
import org.opencv.core.RotatedRect;
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
import java.util.UUID;

public class Helpers {

    public static Mat debugMat;

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

    private static MatOfPoint2f myfindContours(Mat inputImg, int mode, int method, List<MatOfPoint> matOfPointList){
        if(matOfPointList==null) matOfPointList = new ArrayList<>();
        Mat imgToFindContours = new Mat();
        inputImg.copyTo(imgToFindContours);
        List<MatOfPoint> contours = new ArrayList<>();
        //Imgproc.findContours(imgToFindContours, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );
        Imgproc.findContours(imgToFindContours, contours, new Mat(), mode, method, new Point(0, 0) );
        //List<Rect> boundRect = new ArrayList<>(contours.size());
        //List<MatOfPoint> matOfPointList = new ArrayList<>();
        MatOfPoint2f matOfPoint2fContoursPoly = new MatOfPoint2f();
        for( int i = 0; i < contours.size(); i++ )
        {
            MatOfPoint2f matOfPoint2fContours = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(matOfPoint2fContours, matOfPoint2fContoursPoly , 1., true );
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint2fContoursPoly.convertTo(matOfPoint, CvType.CV_32S);
            //Rect rect = Imgproc.boundingRect( matOfPoint );
            //boundRect.add(i, rect);
            matOfPointList.add(matOfPoint);
        }
        return matOfPoint2fContoursPoly;
    }

    private static int SplitInSubmats(Mat input_image, Mat original_image, List<BlobDetected> blobDetectedList ) {
        /// preset delle strutture dati di input/output
        List<KeyPoint> inKpsList = new ArrayList<>();
        for (BlobDetected bd : blobDetectedList) {
            inKpsList.add(bd.keyPoint);
        }

        /// ricerca dei contorni
        List<MatOfPoint> contours = new ArrayList<>();
        Mat imgToFindContours = new Mat();
        input_image.copyTo(imgToFindContours);
        Imgproc.findContours(imgToFindContours, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<Rect> boundRect = new ArrayList<>(contours.size());
        List<MatOfPoint> approxRectList = new ArrayList<>();
        MatOfPoint2f matOfPoint2fContoursPoly = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f matOfPoint2fContours = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(matOfPoint2fContours, matOfPoint2fContoursPoly, 3., true);
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint2fContoursPoly.convertTo(matOfPoint, CvType.CV_32S);
            Rect rect = Imgproc.boundingRect(matOfPoint);
            boundRect.add(i, rect);
            approxRectList.add(i, matOfPoint);
        }

        /// matching tra rect e kp
        for (KeyPoint kp : inKpsList) {
            double minDist = 10000000;
            Rect minDistRect = null;
            MatOfPoint minContour = null;
            for (Rect r : boundRect) {
                if (r != null) {
                    if (r.area() < (input_image.size().area() / 10 * 9)) {
                        if (kp.pt.inside(r)) {
                            Point center = new Point((r.x + r.width) / 2, (r.y + r.height) / 2);
                            double dist = DistBtwnPoints(center, kp.pt);
                            if (dist < minDist) {
                                minDist = dist;
                                minDistRect = r;
                                minContour = approxRectList.get(boundRect.indexOf(r));
                            }
                        }
                    } else {
                        boundRect.set(boundRect.indexOf(r), null);
                    }
                }
            }
            if (minDistRect != null) {
                blobDetectedList.get(inKpsList.indexOf(kp)).rect = minDistRect;
                blobDetectedList.get(inKpsList.indexOf(kp)).contour = minContour;
                boundRect.set(boundRect.indexOf(minDistRect), null);
            } else {
                blobDetectedList.get(inKpsList.indexOf(kp)).rect = new Rect(kp.pt, new Size(50,50));
                blobDetectedList.get(inKpsList.indexOf(kp)).contour = null;
            }
        }



        /// creo le mat per ogni rect
        int maxColsNumber = -1;
        for (BlobDetected bd : blobDetectedList) {
            if(bd.rect.area() < (input_image.size().area() / 10 * 9)) {
                Mat m = input_image.submat(bd.rect);
                Mat mm = new Mat();
                m.copyTo(mm);
                Core.copyMakeBorder(mm, mm, 15, 15, 15, 15, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
                bd.binaryMat = new Mat();
                mm.copyTo(bd.binaryMat);


                Mat n = original_image.submat(new Rect(bd.rect.x - 15, bd.rect.y - 15, bd.rect.width + 30, bd.rect.height + 30));
                Mat nn = new Mat();
                Imgproc.cvtColor(n, nn, Imgproc.COLOR_BGR2GRAY, 1);
                Imgproc.putText(nn, "" + (blobDetectedList.indexOf(bd) + 1), new Point(0, 20), Core.FONT_HERSHEY_COMPLEX, 0.7, new Scalar(255, 255, 255), 2);
                bd.origMat = new Mat();
                nn.copyTo(bd.origMat);

                /*if(outRectKpPairs.indexOf(pair)+1 == 38) {
                    debugMat = new Mat();
                    mask.copyTo(debugMat);
                }*/

                if (mm.cols() > maxColsNumber) {
                    maxColsNumber = mm.cols();
                }
            }
        }
        return maxColsNumber;
    }

    private static Mat[] mVConcat(int maxMatCols, List<BlobDetected> blobDetectedList) {
        Mat[] ret = new Mat[2];
        ret[0] = new Mat();
        ret[1] = new Mat();

        List<List<Mat>> temp = new ArrayList<>();
        temp.add(new ArrayList<Mat>());
        temp.add(new ArrayList<Mat>());

        for (BlobDetected bd : blobDetectedList) {
            Mat[] m = new Mat[2];
            m[0] = bd.binaryMat;
            m[1] = bd.origMat;

            for (int i = 0; i < 2; i++) {
                int mLastCol = m[i].cols();
                if (mLastCol <= maxMatCols) {
                    Mat col = new Mat(m[i].rows(), maxMatCols - mLastCol, m[i].type());
                    col.setTo(new Scalar(255));
                    List<Mat> cols = new ArrayList<>();
                    cols.add(m[i]);
                    cols.add(col);
                    Mat newMc = new Mat();
                    Core.hconcat(cols, newMc);
                    Mat row = new Mat(40, newMc.cols(), newMc.type());
                    row.setTo(new Scalar(255));
                    List<Mat> rows = new ArrayList<>();
                    Mat newMr = new Mat();
                    rows.add(newMc);
                    rows.add(row);
                    Core.vconcat(rows, newMr);
                    temp.get(i).add(newMr);
                }
            }
        }

        Core.vconcat(temp.get(0),ret[0] );
        Core.vconcat(temp.get(1),ret[1] );

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
        List<KeyPoint> kpsSnailsList = new ArrayList<>(kpsSnails.toList());
        Features2d.drawKeypoints(origImgProc, kpsSnails, origImgProc, new Scalar(0,0,255),Features2d.DRAW_RICH_KEYPOINTS);
        Features2d.drawKeypoints(inImgProc, kpsSnails, inImgProc, new Scalar(0,0,255),Features2d.DRAW_RICH_KEYPOINTS);

        MatOfKeyPoint kpsLargeSnails = SimpleBlobDetector(input_image, appPath, "/largeSnails.params.xml");
        List<KeyPoint> kpsLargeSnailsList = new ArrayList<>(kpsLargeSnails.toList());
        Features2d.drawKeypoints(origImgProc, kpsLargeSnails, origImgProc, new Scalar(255,0,0),Features2d.DRAW_RICH_KEYPOINTS);
        Features2d.drawKeypoints(inImgProc, kpsLargeSnails, inImgProc, new Scalar(255,0,0),Features2d.DRAW_RICH_KEYPOINTS);

        List<BlobDetected> blobsList = new ArrayList<>();
        for (KeyPoint kp: kpsSnailsList) {
            BlobDetected bd = new BlobDetected();
            bd.ID = UUID.randomUUID();
            bd.keyPoint = kp;
            bd.large = false;
            blobsList.add(bd);
        }
        for (KeyPoint kp: kpsLargeSnailsList) {
            BlobDetected bd = new BlobDetected();
            bd.ID = UUID.randomUUID();
            bd.keyPoint = kp;
            bd.large = true;
            blobsList.add(bd);
        }

        int maxMatCols = SplitInSubmats(input_image, origImgProc, blobsList);
        Mat[] splittedMat = mVConcat(maxMatCols, blobsList);

        Mat submats = splittedMat[0];
        Mat origSubmats = splittedMat[1];

        Mat outImgProc = new Mat();
        input_image.copyTo(outImgProc);

        //for (Mat blobMat:blobMatsList) {
/*        for (Pair p : rectKpPairs) {
            if(largeKps.contains(p.second)){
                Mat blobMat = submatList.get(rectKpPairs.indexOf(p));

            List<KeyPoint> kpList = new ArrayList<>(SimpleBlobDetector(blobMat, appPath, "/blobs.params.xml").toList());
            Log.i("blobMat_"+(blobMatsList.indexOf(blobMat)+1), "numbers of keypoints is "+kpList.size());
            if(kpList.size() > 0){
                //Imgproc.rectangle(blobMat, new Point(0,0), new Point(blobMat.rows(), blobMat.cols()),new Scalar(100,100,100) );
                Mat element1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 2 + 1, 2 * 2 + 1), new Point(1, 1));
                int TotalNumberOfPixels = blobMat.rows() * blobMat.cols();
                int ZeroPixels = TotalNumberOfPixels - Core.countNonZero(blobMat);
                //int iterations = (100*ZeroPixels/50000) -7 ;
                int iterations = 1;
                Log.i("blobMat_"+(blobMatsList.indexOf(blobMat)+1), "numbers of black pixels is "+ZeroPixels);
                Log.i("blobMat_"+(blobMatsList.indexOf(blobMat)+1), "numbers of dilate iterations is "+iterations);
                MatOfPoint2f snailCnt2f = myfindContours(blobMat, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, null);
                MatOfPoint2f snailCnt2fDO;
                int zoom = 0;
                do {
                    Imgproc.dilate(blobMat, blobMat, element1, new Point(-1,-1), iterations);
                    snailCnt2fDO = myfindContours(blobMat, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, null);
                    zoom++; // provare a scalare l'immagine per aumentare la sua area
                } while (snailCnt2f.rows() <= snailCnt2fDO.rows());

                Mat modMat = outImgProc.submat(rectKpPairs.get(blobMatsList.indexOf(blobMat)).first);
                Mat blobMatMod = new Mat();
                blobMat.copyTo(blobMatMod);
                Imgproc.resize(blobMatMod, blobMatMod, modMat.size());
                blobMatMod.copyTo(modMat);
            }
        }
        }*/
        /*Core.vconcat(submatList, submats);
        Core.vconcat(origSubmatList, origSubmats);
        Core.vconcat(blobMatsList, blobMats);*/

        List<Mat> outSubmatsList = new ArrayList<>();
        Mat margine = new Mat(origSubmats.rows(), 50, origSubmats.type());
        margine.setTo(new Scalar(255));
        outSubmatsList.add(origSubmats);
        outSubmatsList.add(margine);
        outSubmatsList.add(submats);
        outSubmatsList.add(margine);

        Mat outSubmats = new Mat();
        Core.hconcat(outSubmatsList, outSubmats);

        Mat filteredLargeBlobsMat = filterLargeBlobsMat(input_image, blobsList);
        Mat element1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 2 + 1, 2 * 2 + 1), new Point(1, 1));
        int iterations = 10;
        Imgproc.dilate(filteredLargeBlobsMat, filteredLargeBlobsMat, element1, new Point(-1,-1), iterations);
        debugMat = new Mat();
        filteredLargeBlobsMat.copyTo(debugMat);

        MatOfKeyPoint kpsFilteredLargeSnails = SimpleBlobDetector(filteredLargeBlobsMat, appPath, "/lastSnails.params.xml");
        Features2d.drawKeypoints(outImgProc, kpsSnails, outImgProc, new Scalar(0,0,255),Features2d.DRAW_RICH_KEYPOINTS);
        Features2d.drawKeypoints(outImgProc, kpsFilteredLargeSnails, outImgProc, new Scalar(0,255,0),Features2d.DRAW_RICH_KEYPOINTS);
        Mat textRes = new Mat(100, outImgProc.cols(), outImgProc.type(), new Scalar(255,255,255));
        Imgproc.putText(textRes, "Total snails: " + kpsSnailsList.size() + " + " + kpsFilteredLargeSnails.toList().size() , new Point(20,80),Core.FONT_HERSHEY_SIMPLEX,3,new Scalar(0,0,0),5);

        List<Mat> endOutMat = new ArrayList<>();
        endOutMat.add(outImgProc);
        endOutMat.add(textRes);
        Core.vconcat(endOutMat, outImgProc);


        //ret.add(imgProcessed);
        ret.add(inImgProc);
        //ret.addAll(submats);
        ret.add(outSubmats);
        ret.add(outImgProc);

        if(debugMat==null){
            debugMat = new Mat(1,1,CvType.CV_8UC1,new Scalar(255,255,255));
        }
        ret.add(debugMat);

        Mat[] r = new Mat[ret.size()];
        ret.toArray(r);
        return r;
    }

    private static Mat filterLargeBlobsMat(Mat input_image, List<BlobDetected> blobsList){
        Mat ret = new Mat();
        input_image.copyTo(ret);

        for (BlobDetected bd:blobsList) {
            if(!bd.large){
                MatOfPoint2f approxCntr = new MatOfPoint2f(bd.contour.toArray());
                RotatedRect ellipse = Imgproc.fitEllipse(approxCntr);

                Mat m = ret.submat(ellipse.boundingRect());
                m.setTo(new Scalar(255,255,255));
            }
        }
        return ret;
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
