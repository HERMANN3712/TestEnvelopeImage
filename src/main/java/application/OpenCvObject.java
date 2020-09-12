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
import static org.opencv.imgproc.Imgproc.*;

import javafx.scene.image.Image;

public class OpenCvObject {

	private static int MAX_BINARY_VALUE = 255;

	private Mat matrix;

	public boolean isValid = false;

	private boolean onlyEnvelop = true;

	public boolean isOnlyEnvelop() {
		return onlyEnvelop;
	}

	private boolean isFilter = false;

	public void setOnlyEnvelop(boolean onlyEnvelop) {
		this.onlyEnvelop = onlyEnvelop;
	}

	public OpenCvObject(String fileName, boolean filter) {
		this.isFilter = filter;
		Ininitizate(fileName);
	}

	public OpenCvObject(String fileName) {
		Ininitizate(fileName);
	}

	public void Ininitizate(String fileName) {
		this.matrix = Imgcodecs.imread(fileName);

		if (matrix.empty())
			return;
		this.isValid = true;
		System.out.println("Image Loaded");
		System.out.format("Size : %d x %d\n", matrix.height(), matrix.width());

		if (!isFilter)
			return;

		// Image which one of sides exceeds 800px is resized
		if (matrix.height() > 800 || matrix.width() > 800) {
			Size scaleSize = new Size();

			// Larger side equals 800px and sides keep ratio
			if (matrix.height() > matrix.width()) {
				scaleSize = new Size((int) ((double) matrix.width() / matrix.height() * 800), 800);
			} else {
				scaleSize = new Size(800, (int) ((double) matrix.height() / matrix.width() * 800));
			}

			Imgproc.resize(matrix, matrix, scaleSize, 0, 0, INTER_AREA);

			System.out.println("Image reesized");
			System.out.format("Size : %d x %d\n", matrix.height(), matrix.width());
		}
	}

	public Image getImageSource() {
		return this.isValid ? ConvertToImg(matrix) : null;
	}

	public Image getImageIntermediate(Integer thresholdValue) {
		return this.isValid ? ThreholdImg(matrix, thresholdValue) : null;
	}

	public Image getImageOut(Integer thresholdValue) {
		return this.isValid ? EnvelopeImg(matrix, thresholdValue) : null;
	}

	private Image ConvertToImg(Mat matrix) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", matrix, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}

	// Image retouching before processing
	private Mat ThreholdMat(Mat matrix, Integer thresholdValue) {
		if (thresholdValue == null) {
			thresholdValue = 100;
		}

		Mat srcGray = new Mat();
		Mat output = new Mat();

		// Convert the image to Gray
		Imgproc.cvtColor(matrix, srcGray, Imgproc.COLOR_BGR2GRAY);
		// Add blur (blur image to thicken it and to highlight contour)
		Imgproc.blur(srcGray, srcGray, new Size(5, 5));

		// 0: Binary > 1: Binary Inverted > 2: Truncate > 3: To Zero > 4: To Zero
		// Inverted
		int thresholdType = 1;

		// Threshold and transformation of the source image in black and white
		Imgproc.threshold(srcGray, output, thresholdValue, MAX_BINARY_VALUE, thresholdType);

		// Canny filter allows the detection of edges but it is not used
		// because preferable to thresholding
		// Imgproc.Canny(srcGray, output, thresholdValue, thresholdValue * 2);

		return output;
	}

	private Image ThreholdImg(Mat matrix, Integer thresholdValue) {
		matrix = ThreholdMat(matrix, thresholdValue);
		return getImage(matrix);
	}

	private Image getImage(Mat matrix) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", matrix, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}

	// Treatment of convex hull and contours
	private Image EnvelopeImg(Mat matrix, Integer thresholdValue) {

		matrix = ThreholdMat(matrix, thresholdValue);
		Thread t = Thread.currentThread();

		Random rng = new Random(12345);

		Mat hierarchy = new Mat(); // not used
		List<MatOfPoint> contours = new ArrayList<>();

		List<MatOfPoint> hullList = new ArrayList<>();
		// Contour recovery = arrays of points
		Imgproc.findContours(matrix, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
		contours.removeIf(x -> Imgproc.contourArea(x) < 1000);

		// Creation of the list of contours and envelopes
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

		// Iterate over the list of contours and envelopes
		Mat drawing = Mat.zeros(matrix.size(), CvType.CV_8UC3);
		Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
		for (int i = 0; i < contours.size(); i++) {

			// If current thread is interrupted, exit out method
			if (t != null && t.isInterrupted()) {
				System.out.println("... stop process");
				return null;
			}

			if (!onlyEnvelop)
				Imgproc.drawContours(drawing, contours, i, color);// Draw a contour

			// Draw an envelope
			Imgproc.drawContours(drawing, hullList, i, color);

			// Important !! Contour and corresponding envelope have same color
			color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
		}

		return getImage(drawing);
	}
}
