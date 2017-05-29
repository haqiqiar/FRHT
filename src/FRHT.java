import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
		/*Point c = new Point(4,1);
		Point a = new Point(-3,7);
		Point b = new Point(5,-2);
		
		String res = findCenter(a,b,c);
		*/
		//System.out.println(res);
		//System.out.println(la.x+" "+la.y);
		Mat grayscaleMat = Imgcodecs.imread("data/test.jpg", Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
		
		
		//Mat binaryMat = new Mat();
		//Imgproc.threshold(grayscaleMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
		FRHT(grayscaleMat);
		
		
		//preprocess(grayscaleMat);
		
		//Imgproc.cvtColor(grayscaleMat, grayscaleMat, Imgproc.COLOR_BGR2GRAY);
		//Imgproc.equalizeHist(grayscaleMat, grayscaleMat);
		//edgeDetector(grayscaleMat);
		
		//int morph_size = 1;
		//Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
		//		new Size(2 * morph_size + 1, 2 * morph_size + 1), new org.opencv.core.Point(1, 1));

		/*Mat dst = new Mat();		Imgproc.morphologyEx(binaryMat, dst, 3, element);
		Imgcodecs.imwrite("savedClosed.jpg", dst);
				
		Imgproc.morphologyEx(dst, dst, 2, element);
		Imgcodecs.imwrite("savedOpened.jpg", dst);
		*/
		//FRHT(grayscaleMat);
		
		
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
		
		Integer x =  Math.round(-1*B/(2*A));
		Integer y =  Math.round(-1*C/(2*A));
		Integer r = (int) Math.round(Math.sqrt((B*B + C*C - 4*A*D)/(4*A*A)));
		
		String key = x.toString() + ";" + y.toString() + ";" + r.toString();
		
		//System.out.pritln(A + " "+ B+" "+C + " "+ D);
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
		
		
		
		//Imgcodecs.imwrite("saved31Adjust.jpg", dst);
		//Imgproc.morphologyEx(dst, dst, 3, element);
		//Imgcodecs.imwrite("savedClosed.jpg", dst);
		
		//Imgproc.morphologyEx(dst, dst, 2, element);
		//Imgcodecs.imwrite("savedOpened.jpg", dst);
		
		
		edgeDetector(dst);
	}
	
	
	public static void segmentasi(Mat src){
		int morph_size = 3;
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(2 * morph_size + 1, 2 * morph_size + 1), new org.opencv.core.Point(3, 3));
		
		Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(2 * 1 + 1, 2 * 1 + 1), new org.opencv.core.Point(1, 1));

		Mat dst = new Mat();
		Imgproc.morphologyEx(src, dst, 3, element);
		Imgcodecs.imwrite("saved5Closed.jpg", dst);
				
		//Imgproc.morphologyEx(dst, dst, 2, element1);
		//Imgcodecs.imwrite("saved6Opened.jpg", dst);
		
		FRHT(dst);
		
	}
	

	public static void edgeDetector(Mat src) {
		
		
		Imgproc.blur(src, src, new Size(3, 3));
		Imgcodecs.imwrite("saved4Blurred.jpg", src);
		//Scalar val = Core.mean(src);
		//double thres = val.val[0];
		//System.out.println(thres);
		
		Mat dst = new Mat();
		double thres = 19.4;
		Imgproc.Canny(src, dst, thres, thres*3);
		Core.bitwise_not(dst, dst);
		//Imgcodecs.imwrite("befsavedEdge.jpg", src);
		Imgcodecs.imwrite("saved4Edge.jpg", dst);
		
		
		FRHT(dst);
		//segmentasi(dst);
	}

	public static void FRHT(Mat src) {
		// convert to binary

		int w = 100;
		//Mat grayscaleMat = src;
		//Mat binaryMat = new Mat(grayscaleMat.size(), grayscaleMat.type());
		Mat binaryMat = src;
		Mat dst = new Mat();
		src.copyTo(dst);
		try {
			dumper(src, "init.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Imgproc.threshold(grayscaleMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

		// extract points
		ArrayList<Point> edge = new ArrayList<Point>();
		for (int i = 0 + w; i < binaryMat.rows() - w; i++) {
			for (int j = 0 + w; j < binaryMat.cols() - w; j++) {
				if (binaryMat.get(i, j)[0] == 0) {
					edge.add(new Point(j, i));
				}
			}
		}

		Map<String, Integer> mcans = new HashMap<String, Integer>();
		for (int n = 0; n < 200; n++) {

			Random rand = new Random();
			int pos = rand.nextInt((edge.size() - 1) - 0 + 1) + 0;
	
			Point selected = edge.get(pos);
		
			Map<Double, Point> counter = new HashMap<Double, Point>();

			Map<String, Integer> cans = new HashMap<String, Integer>();
			String high = "";
			int nhigh = 0;

			for (int i = selected.y - w; i < (selected.y - w) + 2 * w; i++) {
				for (int j = selected.x - w; j < (selected.x - w) + 2 * w; j++) {
					if (binaryMat.get(i, j)[0] == 0) {
						Double d = Math.sqrt(Math.pow((selected.y - i), 2) + Math.pow((selected.x - j), 2));
						if (counter.get(d) == null) {
							counter.put(d, new Point(j, i));
						} else {
							String key = findCenter(counter.get(d), new Point(j,i), selected);
								
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

			
			String[] det = high.split(";");
			
			System.out.println(high);
			int count = 0;
			for (int i = selected.y - w; i < (selected.y - w) + 2 * w; i++) {
				for (int j = selected.x - w; j < (selected.x - w) + 2 * w; j++) {
					if (binaryMat.get(i, j)[0] == 0) {
						Integer circ = (int) Math.sqrt(
								Math.pow((i - Double.valueOf(det[1])), 2) + Math.pow((j - Double.valueOf(det[0])), 2));
						Double diff = circ - Double.valueOf(det[2]);
						if ((diff <= 1) && (diff >= 0)) {
							count++;
						}
					}
				}
			}

			
			Scalar warna = new Scalar(128,128,0);
			
			Imgproc.circle(dst, new org.opencv.core.Point(Double.valueOf(det[0]),Double.valueOf(det[1])), 1, warna);
			Imgcodecs.imwrite("saved6Circled.jpg", dst);

			Double ratio = (double) count / (2 * Math.PI * Double.valueOf(det[2]));
			//System.out.println(count + " " + ratio);
			//Mat dst = src;
			/*if(ratio > 0.8){
				
				
				//Imgproc.circle(dst, new org.opencv.core.Point(Double.valueOf(det[0]),Double.valueOf(det[1])), Integer.valueOf(det[2]), warna);
				Imgproc.circle(dst, new org.opencv.core.Point(Double.valueOf(det[0]),Double.valueOf(det[1])), 1, warna);
				
				Imgcodecs.imwrite("saved6Circled.jpg", dst);
				System.out.println("saved");
				//break; 
			}*/
			
			

		}

		// Point la =new Point(1,1);
		// cans.put("lala", 1);
		// cans.put("lala", 2);
		for (Map.Entry<String, Integer> entry : mcans.entrySet()) {
			if (entry.getValue() > 4) {
				System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			}
		}

		// System.out.println(cans.size());

		/* Sorting */

		/*
		 * Collections.sort(cans, new Comparator<Cans>(){
		 * 
		 * @Override public int compare(Cans arg0, Cans arg1) { // TODO
		 * Auto-generated method stub if(arg0.rc>arg1.rc){ return -1; }
		 * 
		 * else if (arg0.rc<arg1.rc){ return 1; } return 0; }
		 * 
		 * });
		 * 
		 * for(Cans a : cans){
		 * System.out.println(a.x.toString()+" "+a.y.toString()+" "+a.rc.
		 * toString()); }
		 */
		/*
		 * Cans candidate = cans.get(0);
		 * System.out.println("Candidate: "+"("+candidate.points.size()+")"+
		 * candidate.seed.x.toString()+","+candidate.seed.y.toString());
		 * 
		 * 
		 * Mat res = Mat.zeros(binaryMat.size(), CvType.CV_32FC1); Integer i=0;
		 * for (Pair a : candidate.points){ res.put(a.a.y, a.a.x,
		 * Integer.valueOf(i.toString()+"25")); res.put(a.b.y, a.b.x,
		 * Integer.valueOf(i.toString()+"25"));
		 * System.out.println(a.a.x.toString()+","+a.a.y.toString()+";"+a.b.x.
		 * toString()+","+a.b.y.toString()); i++; }
		 * 
		 * res.put(candidate.seed.y,candidate.seed.x, 128);
		 */
		/*
		 * for(Cans can : cans){ System.out.println("Candidate :");
		 * System.out.println(can.points.size());
		 * 
		 * /*System.out.println("("+can.seed.x.toString()+","+can.seed.y.
		 * toString()+";"+")"); for(Pair now : can.points){
		 * System.out.println(now.a.x.toString()+","+now.a.y.toString()+";"+now.
		 * b.x.toString()+","+now.b.y.toString()); }(=
		 * 
		 * 
		 * }
		 */

		MatOfInt matInt = new MatOfInt();

		matInt.fromArray(Imgcodecs.CV_IMWRITE_PNG_COMPRESSION, 1);

		// Imgcodecs.imwrite("lala.jpg", res, matInt);

		// dumper(res, "ehe1.csv");
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
