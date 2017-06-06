import java.util.List;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfRect; import org.opencv.core.Point;import org.opencv.objdetect.CascadeClassifier;

public class FRHT {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws IOException {
		
		/*
		 * successful one
		int nrandom = 200;
		int window = 60;
		double mratio = 0.22985;
		*/
		
		int nrandom = 200;
		int window = 100;
		double mratio = 0.22;
		
		
		
		Mat dst = new Mat();
		for(int i = 390;i<391;i++){
			System.out.println(i);
			Mat matSrc = Imgcodecs.imread("data/test.jpg", Imgcodecs.CV_LOAD_IMAGE_COLOR);
			
			Imgproc.cvtColor(matSrc, dst, Imgproc.COLOR_BGR2GRAY);
			
			Mat edged = edgeDetector(dst);
			
			String res = doFRHT(edged, nrandom,  window, mratio);
			
			if(res !=""){
				String[] det = res.split(";");
				Scalar warna = new Scalar(255,255,255);
				matSrc.copyTo(dst);
				Imgproc.circle(dst, new org.opencv.core.Point(Double.valueOf(det[0]),Double.valueOf(det[1])), Double.valueOf(det[2]).intValue(), warna);
				
				Mat finale = new Mat();
				List<Mat> alist = Arrays.asList(matSrc, dst);
				Core.hconcat(alist, finale);
				Imgcodecs.imwrite("saved7Circled.jpg", dst);
				Imgcodecs.imwrite("data/input/saved8Result"+i+".jpg", finale);
			}
		}
	}
	
	static class Point {
		Integer x;
		Integer y;

		Point(Integer x, Integer y) {
			this.x = x;
			this.y = y;
		}
	}

	static class Cans {
		Integer x;
		Integer y;
		Double rc;

		Cans(Integer xc, Integer yc, Double rc2) {
			this.x = xc;
			this.y = yc;
			this.rc = rc2;
		}
	}
	
	public static Mat edgeDetector(Mat src) {
		Mat dst = new Mat();
		Imgproc.blur(src, dst, new Size(3, 3));
		Imgcodecs.imwrite("saved5Blurred.jpg", src);
		
		double thres = 19.4;
		Imgproc.Canny(dst, dst, thres, thres*3);
		Core.bitwise_not(dst, dst);
		Imgcodecs.imwrite("saved6Edge.jpg", dst);
		
		return dst;
	}
	
	public static String findCenter(Point a, Point b, Point c){
		
		//http://www.ambrsoft.com/TrigoCalc/Circle3D.htm
		//base on Integer
		
		Integer A = a.x*(b.y - c.y) 
				- a.y*(b.x-c.x) 
				+ b.x*c.y 
				- c.x*b.y;
		
		Integer B = (a.x*a.x + a.y*a.y) * (c.y-b.y)
				+ (b.x*b.x + b.y*b.y) * (a.y-c.y)
				+ (c.x*c.x + c.y*c.y) * (b.y-a.y);
		
		Integer C = (a.x*a.x+a.y*a.y) * (b.x-c.x)
				+(b.x*b.x+b.y*b.y) * (c.x-a.x)
				+(c.x*c.x+c.y*c.y) * (a.x-b.x);
		
		Integer D = (a.x*a.x+a.y*a.y)*(c.x*b.y-b.x*c.y)
				+(b.x*b.x+b.y*b.y)*(a.x*c.y-c.x*a.y)
				+(c.x*c.x+c.y*c.y)*(b.x*a.y-a.x*b.y);
		
		//System.out.println(A + " "+ B+" "+C + " "+ D);
		
		if(A == 0){
			return "";
		}
		
		Integer x =  Math.round(-1*B/(2*A));
		Integer y =  Math.round(-1*C/(2*A));
		Double r = Math.sqrt((B*B + C*C - 4*A*D)/(4*A*A));
		
		if(r < 60){
			return "";
		}
		
		String key = x.toString() + ";" + y.toString() + ";" + r.toString();
		
		
		//System.out.println(x + " "+ y+" "+ r);
		
		return key;
	}


	public static void preprocess(Mat src) {

		// Strel
		Imgcodecs.imwrite("saved1Gray.jpg", src);
		int morph_size = 3;
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(2 * morph_size + 1, 2 * morph_size + 1), new org.opencv.core.Point(3, 3));

		// TopHat
		int operation = 5;
		
		Mat dst = new Mat();
		
		Imgproc.morphologyEx(src, dst, operation, element);
		Core.bitwise_not(dst, dst);
		Imgcodecs.imwrite("saved2TopHat.jpg", dst);

		// Adjusting
		Imgproc.equalizeHist( dst, dst );
		Imgcodecs.imwrite("saved3Adjust.jpg", dst);
	
	}
	
	
	public static Mat segmentasi(Mat src){
		int morph_size = 3;
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(2 * morph_size + 1, 2 * morph_size + 1), new org.opencv.core.Point(3, 3));
		
		Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
				new Size(3,3));

		Mat dst = new Mat();
		Core.bitwise_not(src, src);
		Imgproc.morphologyEx(src, dst, 3, element);
		Imgcodecs.imwrite("saved5Closed.jpg", dst);	
		
		boolean done;
		
		Mat skel = new Mat(dst.size(), CvType.CV_8UC1, new Scalar(0));
		
		Mat eroded = new Mat();
		Mat temp = new Mat();
		do{
			Imgproc.erode(dst, eroded, element1);
			Imgproc.dilate(eroded, temp, element1);
			Core.subtract(dst, temp, temp);
			Core.bitwise_or(skel, temp, skel);
			eroded.copyTo(dst);
			done = (Core.countNonZero(dst) == 0);
		}
		while(!done);
		
		Imgcodecs.imwrite("saved6Opened.jpg", dst);
				
		//Imgproc.morphologyEx(dst, dst, 2, element1);
		//Imgcodecs.imwrite("saved6Opened.jpg", dst);
		
		
		return dst;
	}
	
	
	public static String doFRHT(Mat src, int nrandom, int w, double mratio) {
	
		Mat dst = new Mat();
		src.copyTo(dst);
		
		// extract points
		ArrayList<Point> edge = new ArrayList<Point>();
		for (int i = 0 + w; i < src.rows() - w; i++) {
			for (int j = 0 + w; j < src.cols() - w; j++) {
				if (src.get(i, j)[0] == 0) {
					edge.add(new Point(j, i));
				}
			}
		}
		
		if(edge.size()==0){
			return "";
		}
		
		for (int n = 0; n < nrandom; n++) {
			
			//pick random seed
			Random rand = new Random();
			int pos = rand.nextInt((edge.size() - 1) - 0 + 1) + 0;
			Point selected = edge.get(pos);
		
			Map<Double, Point> counter = new HashMap<Double, Point>();

			Map<String, Integer> cans = new HashMap<String, Integer>();
			
			String high = "";
			int nhigh = 0;

			for (int i = selected.y - w; i < (selected.y - w) + 2 * w; i++) {
				for (int j = selected.x - w; j < (selected.x - w) + 2 * w; j++) {
					if (src.get(i, j)[0] == 0) {
						Double d = Math.sqrt(Math.pow((selected.y - i), 2) + Math.pow((selected.x - j), 2));
						if (counter.get(d) == null) {
							counter.put(d, new Point(j, i));
						} else {
							String key = findCenter(counter.get(d), new Point(j,i), selected);
							if(key=="") continue;
							if (cans.get(key) == null) {
								cans.put(key, 1);
							} else {
								cans.put(key, cans.get(key) + 1);
							}								
							if (nhigh <= cans.get(key)) {
								nhigh = cans.get(key);
								high = key;
							}				
							counter.remove(d);
						}
					}
				}
			}
			
			if(high=="") continue;
			String[] det = high.split(";");
			//System.out.println(high);
			int count = 0;
			for (int i = selected.y - w; i < (selected.y - w) + 2 * w; i++) {
				for (int j = selected.x - w; j < (selected.x - w) + 2 * w; j++) {
					if (src.get(i, j)[0] == 0) {
						
						Double circ =  Math.sqrt(
								Math.pow((i - Integer.valueOf(det[1])), 2) + Math.pow((j - Integer.valueOf(det[0])), 2));
						Double diff = circ - Double.valueOf(det[2]);
						if ((diff <= 1) && (diff >= -1)) {
							count++;
						}
					}
				}
			}

			Double ratio = (double) count / (2 * Math.PI * Double.valueOf(det[2]));

			if(ratio>mratio){
				System.out.println(count + " " + ratio);
				return high;
			}
		}		
		return "";

	}

	public static void printArr(byte[][] arr) {
		for (int y = 0; y < arr.length; ++y) {
			for (int x = 0; x < arr[y].length; ++x)
				System.out.print(arr[y][x] + ", "); // Was arr[x][y] in prev
			// code.
			System.out.println();
		}
	}

	public static void dumper(Mat m, String name) throws IOException {
		File f = new File(name);
		f.createNewFile();
		PrintWriter out = new PrintWriter(f, "UTF-8");
		out.println(m.dump());
		out.close();
	}
}
