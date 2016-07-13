package it.unipg.studenti.ai.snails.utils;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

import java.util.UUID;

/**
 * Created by m.viscido on 13/07/2016.
 */
public class BlobDetected {
    public UUID ID;
    public KeyPoint keyPoint;
    public MatOfPoint contour;
    public Rect rect;
    public Mat binaryMat;
    public Mat origMat;
    public boolean large;
}
