import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
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
	 
	
	 
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int w=90; 
		
		class Pair<T>{
			 T x;
			 T y;
			 Pair(T x, T y){this.x=x;this.y=y;}
		 }
		
		class Cans<T>{
			 T x;
			 T y;
			 T z;
			 Cans(T x, T y, T z){this.x=x;this.y=y;this.z=z;}
		 }

		Mat grayscaleMat = Imgcodecs.imread("data/test.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);		
		
	
        //convert to binary
        Mat binaryMat = new Mat(grayscaleMat.size(),grayscaleMat.type());
        Imgproc.threshold(grayscaleMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        ArrayList<Pair<Integer>> edge = new ArrayList<Pair<Integer>>(); 
        for(int i =0+w; i<binaryMat.rows()-w;i++){
        	for(int j=0+w;j<binaryMat.cols()-w;j++){
        		if(binaryMat.get(i,j)[0]==0){
        			edge.add(new Pair<Integer>(j,i));
        			
        		}
        	}
        }
        
        Random rand = new Random();
        
        int pos = rand.nextInt(edge.size()-0+1)+0;
        System.out.println(pos);
        Pair<Integer> selected = edge.get(pos);
        System.out.println(String.valueOf(selected.x)+" "+String.valueOf(selected.y));
        
        
        Rect rect = new Rect(selected.x-w, selected.y-w,2*w,2*w);
        Mat roi = new Mat(binaryMat, rect);
        
        Map<Double, Pair<Integer>> counter = new HashMap<Double, Pair<Integer>>();
        
        
        ArrayList<Cans<Pair<Integer>>> cans = new ArrayList<Cans<Pair<Integer>>>();
        for(int i =0; i<roi.rows();i++){
        	for(int j=0;j<roi.cols();j++){
        		if(roi.get(i,j)[0]==0){
        			Double d = Math.sqrt(Math.pow((selected.y-i),2)+Math.pow((selected.x-j),2));
        			if(counter.get(d)==null){
        				counter.put(d, new Pair<Integer>(j,i));
        			}else{
        
        				cans.add(new Cans<Pair<Integer>>(selected, counter.get(d), new Pair<Integer>(j,i)));
        				counter.remove(d);
        
        			}
        
        		}
        	}
        }
        
       System.out.println("Candidate :");
       for(Cans<Pair<Integer>> can : cans){
    	   System.out.println("("+can.x.x.toString()+","+can.x.y.toString()+";"
    			   		+can.y.x.toString()+","+can.y.y.toString()+";"
    			   		+can.z.x.toString()+","+can.z.y.toString()+")"
    			   		+String.valueOf(Math.sqrt(Math.pow(can.x.x-can.y.x,2)+Math.pow(can.x.y-can.y.y,2)))+"|"
    			   		+String.valueOf(Math.sqrt(Math.pow(can.x.x-can.z.x,2)+Math.pow(can.x.y-can.z.y,2))));
       }
        
        dumper(binaryMat, "ehe1.csv");
	}

}
