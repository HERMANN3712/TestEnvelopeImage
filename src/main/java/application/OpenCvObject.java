package application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.Image;

public class OpenCvObject {
	
    private static int MAX_BINARY_VALUE = 255;

	private Mat matrix;

	public boolean checked = false;

	public OpenCvObject(String fileName) {
		this.matrix = Imgcodecs.imread(fileName);

		if (matrix.empty())
			return;
		this.checked = true;
		System.out.format("Size : %d x %d\n", matrix.height(), matrix.width());
		System.out.println("Image Loaded");
	}

	public boolean isLarge = false;

	public Image getImageSource() {
		return this.checked ? ConvertToImg(matrix) : null;
	}
	
	public Image getImageIntermediate(Integer thresholdValue ) {
		return this.checked ? ThreholdImg(matrix, thresholdValue) : null;
	}
	
	public Image getImageOut(Integer thresholdValue) {
		return this.checked ? EnvelopeImg(matrix, thresholdValue ) : null;
	}

	private Image ConvertToImg(Mat matrix) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", matrix, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}
	
	// Retouche de l'image avant traitment
	private Mat ThreholdMat(Mat matrix, Integer thresholdValue )
	{
		if(thresholdValue == null)
		{
			thresholdValue = 100;			
		}
        
        Mat srcGray = new Mat();
        Mat output = new Mat();
        
        // Convert the image to Gray
        Imgproc.cvtColor(matrix, srcGray, Imgproc.COLOR_BGR2GRAY);
        // Add blur (on floute l'image pour l’épaissir et pour mettre en évidence les contours)
        Imgproc.blur(srcGray, srcGray, new Size(5, 5));
        
        //  0: Binary > 1: Binary Inverted > 2: Truncate > 3: To Zero > 4: To Zero Inverted
        int thresholdType = 1;
        
        // Seuillage et transformation de l'image source en noir et blanc
        Imgproc.threshold(srcGray, output, thresholdValue, MAX_BINARY_VALUE, thresholdType);
        
        // Le filtre de Canny permet la détection des contours, il est non utilisé préférable au seuillage 
        //Imgproc.Canny(srcGray, output, thresholdValue, thresholdValue * 2);        
        
        return output;
	}
	
	private Image ThreholdImg(Mat matrix, Integer thresholdValue )
	{
		matrix = ThreholdMat(matrix, thresholdValue );		
		return getImage(matrix);
	}
	
	private Image getImage(Mat matrix)
	{
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", matrix, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}
	
	// Traitement de l'enveloppe convexe des contours
	private Image EnvelopeImg(Mat matrix, Integer thresholdValue )
	{
		
		matrix = ThreholdMat(matrix, thresholdValue);	
		Thread t = Thread.currentThread();
			
		
		Random rng = new Random(12345);
        
		Mat hierarchy = new Mat(); // non utilisé
		List<MatOfPoint> contours = new ArrayList<>();
        
        
        // Récupération des contours = tableaux de points
        Imgproc.findContours(matrix, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        
        List<MatOfPoint> hullList = new ArrayList<>();
        
        // création de la liste des contours et des enveloppes        
        for (MatOfPoint contour : contours) {
        	
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull, true);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }
        
        // Itération sur la liste des contours et des enveloppes
        Mat drawing = Mat.zeros(matrix.size(), CvType.CV_8UC3);
        Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
        for (int i = 0; i < contours.size(); i++) {
        	
        	// si le thread courant est interrompu, on sort de la méthode
        	if(t != null && t.isInterrupted()) { System.out.println("... stop process"); return null; }
        	
        	// affichage d'un contours
        	Imgproc.drawContours(drawing, contours, i, color);
        	
        	// affichage d'une enveloppe
        	Imgproc.drawContours(drawing, hullList, i, color );
        	
        	// !! Important le contours et l'enveloppe correspondante on la même couleur
        	color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));            
        }		
		
		return getImage(drawing);
	}
}
