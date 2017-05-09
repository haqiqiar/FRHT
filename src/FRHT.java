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
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

public class FRHT {
	static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	public static void printArr( byte[][] arr )
	{
		for ( int y=0; y<arr.length; ++y )
		{
			for ( int x=0; x<arr[y].length; ++x )
				System.out.print( arr[y][x] + ", " ); // Was arr[x][y] in prev code.
			System.out.println("");
		}

	}
	public static void dumper(Mat m, String name) throws IOException{
		File f = new File(name);
		f.createNewFile();
		PrintWriter out = new PrintWriter(f, "UTF-8");
		out.println(m.dump());
		out.close(); 
	}
	
	static class Point{
		Integer x;
		Integer y;
		Point(Integer x, Integer y){this.x=x;this.y=y;}
	}

	
	static class Cans{
		Integer x;
		Integer y;
		Double rc;
		Cans(Integer xc, Integer yc, Double rc2){this.x = xc; this.y = yc; this.rc = rc2;}
	}

	
	public static Cans getCentre(Point a, Point b, Point c){
		Double a1 = -1 * Double.valueOf((b.x - a.x)/(b.y - a.y));
		Double b1 = Double.valueOf((a.y+b.y)/2) - a1 *Double.valueOf((a.x+b.x)/2);
		
		Double a2 = -1 * Double.valueOf((c.x-b.x)/(c.y-b.y));
		Double b2 = Double.valueOf((b.y+c.y)/2) - a2 *Double.valueOf((b.x+c.x)/2);
		
		Integer xc = (int)((b2 - b1)/(a1 - a2));
		Integer yc = (int) ((a1*xc)+b1);
		
		Double rc = Math.sqrt(Math.pow((a.x-xc),2)+Math.pow((a.y-yc),2));
		
		return new Cans(xc, yc,rc);
	}


	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int w=60; 

		
		Mat grayscaleMat = Imgcodecs.imread("data/test.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);		


		//convert to binary
		Mat binaryMat = new Mat(grayscaleMat.size(),grayscaleMat.type());
		//dumper(binaryMat, "ehe3.csv");
		Imgproc.threshold(grayscaleMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

		//extract points
		ArrayList<Point> edge = new ArrayList<Point>(); 
		for(int i =0+w; i<binaryMat.rows()-w;i++){
			for(int j=0+w;j<binaryMat.cols()-w;j++){
				if(binaryMat.get(i,j)[0]==0){
					edge.add(new Point(j,i));
				}
			}
		}

		ArrayList<Cans> cans = new ArrayList<Cans>();
		//System.out.println(binaryMat.size());
		//System.out.println(binaryMat.get(5,1)[0]);
		
		for(int n = 0; n<400;n++){
			//picking random seed

			Random rand = new Random();
			int pos = rand.nextInt((edge.size()-1)-0+1)+0;
			//System.out.println(pos);
			Point selected = edge.get(pos);
			//Point selected = new Point(5,1);
//			//System.out.println(String.valueOf(selected.x)+" "+String.valueOf(selected.y));

			//windowing
			//Rect rect = new Rect(selected.x-w, selected.y-w,2*w,2*w);
			//Mat roi = new Mat(binaryMat, rect);

			//get candidate
			Map<Double, Point> counter = new HashMap<Double, Point>();
			
			//Cans ncans = new Cans(selected);
			for(int i =selected.y-w; i<(selected.y-w)+2*w;i++){
				for(int j=selected.x-w;j<(selected.x-w)+2*w;j++){
					if(binaryMat.get(i,j)[0]==0){
						Double d = Math.sqrt(Math.pow((selected.y-i),2)+Math.pow((selected.x-j),2));
						if(counter.get(d)==null){
							counter.put(d, new Point(j,i));
						}else{
							cans.add(getCentre(counter.get(d), selected, new Point(j,i)));
							//ncans..add(new Pair(counter.get(d), new Point(j,i)));
							counter.remove(d);
						}
					}
				}
			}
			
			
			//dumper(roi, "ehe2.csv");
			/*
			if(ncans.points.size()>=106){
				
				dumper(roi, "ehe2.csv");
			}*/
			//cans.add(ncans);
		}
		
		
		//System.out.println(cans.size());
		
		/*Sorting*/
		
		Collections.sort(cans, new Comparator<Cans>(){

			@Override
			public int compare(Cans arg0, Cans arg1) {
				// TODO Auto-generated method stub
				if(arg0.rc>arg1.rc){
					return -1;
				}
				
				else if (arg0.rc<arg1.rc){
					return 1;
				}
				return 0;
			}
			
		});
		
		for(Cans a : cans){
			System.out.println(a.x.toString()+" "+a.y.toString()+" "+a.rc.toString());
		}
		
		/*
		Cans candidate = cans.get(0);
		System.out.println("Candidate: "+"("+candidate.points.size()+")"+candidate.seed.x.toString()+","+candidate.seed.y.toString());
		
		
		Mat res = Mat.zeros(binaryMat.size(), CvType.CV_32FC1);
		Integer i=0;
		for (Pair a : candidate.points){
			res.put(a.a.y, a.a.x, Integer.valueOf(i.toString()+"25"));
			res.put(a.b.y, a.b.x, Integer.valueOf(i.toString()+"25"));
			System.out.println(a.a.x.toString()+","+a.a.y.toString()+";"+a.b.x.toString()+","+a.b.y.toString());
			i++;
		}
		
		res.put(candidate.seed.y,candidate.seed.x, 128);
		*/
		/*for(Cans can : cans){
			System.out.println("Candidate :");
			System.out.println(can.points.size());
			
			/*System.out.println("("+can.seed.x.toString()+","+can.seed.y.toString()+";"+")");
			for(Pair now : can.points){
				System.out.println(now.a.x.toString()+","+now.a.y.toString()+";"+now.b.x.toString()+","+now.b.y.toString());
			}(=
			
			
			}*/
		
		MatOfInt matInt=new MatOfInt();
		
		matInt.fromArray(Imgcodecs.CV_IMWRITE_PNG_COMPRESSION, 1);
		
		
		
		//Imgcodecs.imwrite("lala.jpg", res, matInt);
		

		//dumper(res, "ehe1.csv");
	}
	

}
