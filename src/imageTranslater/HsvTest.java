package imageTranslater;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import processing.core.PApplet;

public class HsvTest extends PApplet {

	public static void main(String args[]) {

		PApplet.main(new String[] { "imageTranslater.HsvTest" });
	}

	VideoCapture cap;
	ImageTranslater it;

	public void setup() {
		size(1280, 720);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		frameRate(60);
		it = new ImageTranslater(this);
		cap = new VideoCapture();
		cap.open(0);
	}

	public void draw() {
		Mat bgr = new Mat();
		cap.read(bgr);
		Mat hsv = new Mat();
		Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV);
		Imgproc.medianBlur(hsv, hsv, 3);
		Mat skin = skinDetect(hsv);
		Mat skinDist = distTransform(skin);

		 image(it.Mat2PI(skinDist),0,0,width,height);
		Mat bin = new Mat();
		Imgproc.threshold(skinDist, bin, 0, 255, Imgproc.THRESH_BINARY
				| Imgproc.THRESH_OTSU);

		 image(it.Mat2PI(bin), 0, 0, width, height);

		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat(skinDist.cols(), skinDist.rows(),
				CvType.CV_32SC1);
		Imgproc.findContours(skinDist, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_NONE);
		int index = -1;
		double area = 0;
		for (int i = 0, n = contours.size(); i < n; i++) {
			double tmp = Imgproc.contourArea(contours.get(i));
			if (area < tmp) {
				area = tmp;
				index = i;
			}
		}
		if (index != -1) {
			MatOfInt hull = new MatOfInt();
			MatOfPoint contour = contours.get(index);
			Imgproc.convexHull(contour, hull);

			Point data[] = contour.toArray();
			int v = -1;
			for (int i : hull.toArray()) {
				if (v == -1) {
					v = i;
				} else {
					Core.line(bgr, data[i], data[v], new Scalar(0, 255, 0));
					v = i;
				}
			}

			convexityDefects(bgr, contour,hull);
			Imgproc.drawContours(bgr, contours, index, new Scalar(255, 0, 0));
		}
		image(it.Mat2PI(bgr), 0, 0, width, height);
		fill(0);
		text(frameRate + "fps", 0, 10);
	}

	void convexityDefects(Mat img,MatOfPoint contour,MatOfInt hull) {
		Point data[] = contour.toArray();
		MatOfInt4 convexityDefects = new MatOfInt4();
		Imgproc.convexityDefects(contour, hull, convexityDefects);
		int cd[] = convexityDefects.toArray();
		if(cd==null)return;
		for (int i = 0; i < cd.length; i += 4) {
//			Core.line(img, data[cd[i]], data[cd[i+1]], new Scalar(255,255,255));
			Core.line(img, data[cd[i+1]], data[cd[i+2]], new Scalar(255,255,255));
			Core.line(img, data[cd[i]], data[cd[i+2]], new Scalar(255,255,255));


//			Core.circle(img, data[cd[i + 2]], 10, new Scalar(0, 0, 255));
		}
	}

	Mat skinDetect(Mat mat) {
		Mat mat1 = new Mat();
		Core.inRange(mat, new Scalar(0, 58, 89), new Scalar(25, 173, 229), mat1);
		return mat1;
	}

	Mat distTransform(Mat mat) {
		Mat mat1 = new Mat(mat.cols(), mat.rows(), CvType.CV_8UC1);
		Mat mat2 = new Mat();
		Imgproc.distanceTransform(mat, mat2, Imgproc.CV_DIST_L2, 3);
		Core.convertScaleAbs(mat2, mat1);
		Core.normalize(mat1, mat1, 0.0, 255.0, Core.NORM_MINMAX);
		return mat1;
	}
}
