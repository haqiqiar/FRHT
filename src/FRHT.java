import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
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
		Mat grayscaleMat = Imgcodecs.imread("test.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);		
		
	
        //convert to binary
        Mat binaryMat = new Mat(grayscaleMat.size(),grayscaleMat.type());
        Imgproc.threshold(grayscaleMat, binaryMat, 1, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Mat binary = binaryMat.clone();
        
        dumper(binary, "ehe1.csv");
	}

}
