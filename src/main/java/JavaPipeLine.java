import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import edu.wpi.cscore.CvSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.*;

import org.opencv.core.*;
import org.opencv.imgproc.*;

/**
* JavaGripPipeline class.
*
* <p>An OpenCV pipeline generated by GRIP.
*
* @author GRIP
*/
public class JavaPipeLine implements VisionPipeline {

	//Outputs
	private Mat blurOutput = new Mat();
	private Mat rgbThresholdOutput = new Mat();
	private ArrayList<Line> findLinesOutput = new ArrayList<Line>();
	private ArrayList<Line> filterLinesOutput = new ArrayList<Line>();
	private CameraServer inst = CameraServer.getInstance();
	private CvSource imageOut = inst.putVideo("processed", 160, 120);
	private double val = 0;

	double[] yes = {1,1};
	double[] no = {0,0};

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 */
	@Override	public void process(Mat source0) {
		System.out.print("Starting Process.");
		
		// Step Blur0:
		Mat blurInput = source0;
		BlurType blurType = BlurType.get("Median Filter");
		double blurRadius = 0.0;
		blur(blurInput, blurType, blurRadius, blurOutput);

		// Step RGB_Threshold0:
		Mat rgbThresholdInput = blurOutput;
		double[] rgbThresholdRed = {0.0, 150.56313993174058};
		double[] rgbThresholdGreen = {238.4892086330935, 255.0};
		double[] rgbThresholdBlue = {0.0, 255.0};
		SmartDashboard.putNumberArray("Red color", rgbThresholdGreen);
		rgbThreshold(rgbThresholdInput, rgbThresholdRed, rgbThresholdGreen, rgbThresholdBlue, rgbThresholdOutput);
		//imageOut.putFrame(rgbThresholdOutput);   
		
		NetworkTableInstance netWorkTable = NetworkTableInstance.getDefault();
		NetworkTable table = netWorkTable.getTable("Test");
		NetworkTableEntry testValue = table.getEntry("TestValue");
		testValue.setDouble(val++);

		double totalPoints = 0;
		double sumOfWhiteCol = 0;
		double sumOfWhiteRow = 0;

		for(int i = 0; i < rgbThresholdOutput.cols(); i++){
			for(int j = 0; j < rgbThresholdOutput.rows(); j++){
				// System.out.print(rgbThresholdOutput.get(j,i));
				if(rgbThresholdOutput.get(j,i)[0] == 255.0){
					sumOfWhiteCol += i;
					sumOfWhiteRow += j;
					totalPoints++;
					
				}
			}
		}

		int avgCol = (int)(Math.round(sumOfWhiteCol/totalPoints));
		int avgRow =  (int)(Math.round(sumOfWhiteRow/totalPoints));
		Mat imageout = blurOutput;
		Imgproc.circle(imageout,new Point(avgCol, avgRow),10, new Scalar(0,0,254),2);
		imageOut.putFrame(imageout);   
		System.out.println("Center Point: Row: " + avgRow + ", Col: " + avgCol);

		// Add center point to the network tables.
		NetworkTableEntry ValueMiddleX = table.getEntry("Middle X");
		NetworkTableEntry ValueMiddleY = table.getEntry("Middle Y");
		ValueMiddleX.setDouble(avgCol);
		ValueMiddleY.setDouble(avgRow);

		// Add lowest point of identified target to network tables.

		// Add width/farthest left/farthest right of identified target to network tables.

	}

	/**
	 * This method is a generated getter for the output of a Blur.
	 * @return Mat output from Blur.
	 */
	public Mat blurOutput() {
		return blurOutput;
	}

	/**
	 * This method is a generated getter for the output of a RGB_Threshold.
	 * @return Mat output from RGB_Threshold.
	 */
	public Mat rgbThresholdOutput() {
		imageOut.putFrame(rgbThresholdOutput());  
		return rgbThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a Find_Lines.
	 * @return ArrayList<Line> output from Find_Lines.
	 */
	public ArrayList<Line> findLinesOutput() {
		return findLinesOutput;
	}

	/**
	 * This method is a generated getter for the output of a Filter_Lines.
	 * @return ArrayList<Line> output from Filter_Lines.
	 */
	public ArrayList<Line> filterLinesOutput() {
		return filterLinesOutput;
	}


	/**
	 * An indication of which type of filter to use for a blur.
	 * Choices are BOX, GAUSSIAN, MEDIAN, and BILATERAL
	 */
	enum BlurType{
		BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"),
			BILATERAL("Bilateral Filter");

		private final String label;

		BlurType(String label) {
			this.label = label;
		}

		public static BlurType get(String type) {
			if (BILATERAL.label.equals(type)) {
				return BILATERAL;
			}
			else if (GAUSSIAN.label.equals(type)) {
			return GAUSSIAN;
			}
			else if (MEDIAN.label.equals(type)) {
				return MEDIAN;
			}
			else {
				return BOX;
			}
		}

		@Override
		public String toString() {
			return this.label;
		}
	}

	/**
	 * Softens an image using one of several filters.
	 * @param input The image on which to perform the blur.
	 * @param type The blurType to perform.
	 * @param doubleRadius The radius for the blur.
	 * @param output The image in which to store the output.
	 */
	private void blur(Mat input, BlurType type, double doubleRadius,
		Mat output) {
		int radius = (int)(doubleRadius + 0.5);
		int kernelSize;
		switch(type){
			case BOX:
				kernelSize = 2 * radius + 1;
				Imgproc.blur(input, output, new Size(kernelSize, kernelSize));
				break;
			case GAUSSIAN:
				kernelSize = 6 * radius + 1;
				Imgproc.GaussianBlur(input,output, new Size(kernelSize, kernelSize), radius);
				break;
			case MEDIAN:
				kernelSize = 2 * radius + 1;
				Imgproc.medianBlur(input, output, kernelSize);
				break;
			case BILATERAL:
				Imgproc.bilateralFilter(input, output, -1, radius, radius);
				break;
		}
	}

	/**
	 * Segment an image based on color ranges.
	 * @param input The image on which to perform the RGB threshold.
	 * @param red The min and max red.
	 * @param green The min and max green.
	 * @param blue The min and max blue.
	 * @param output The image in which to store the output.
	 */
	private void rgbThreshold(Mat input, double[] red, double[] green, double[] blue,
		Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2RGB);
		Core.inRange(out, new Scalar(red[0], green[0], blue[0]),
			new Scalar(red[1], green[1], blue[1]), out);
	}

	public static class Line {
		public final double x1, y1, x2, y2;
		public Line(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		public double lengthSquared() {
			return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
		}
		public double length() {
            System.out.println("Length ");
            double Lengthvar = Math.sqrt(lengthSquared());
            System.out.println(Lengthvar);
            return Math.sqrt(lengthSquared());
		}
		public double angle() {
			return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
		}
	}
	/**
	 * Finds all line segments in an image.
	 * @param input The image on which to perform the find lines.
	 * @param lineList The output where the lines are stored.
	 */
	private void findLines(Mat input, ArrayList<Line> lineList) {
		final Mat lines = new Mat();
		Imgproc.HoughLines(input, lines, 1, 1, 1);
		
		lineList.clear();
		System.out.println("lines:" + lines.cols());
		for(int i = 0; i < lines.cols(); i++)
		{
			double data[] = lines.get(0,i);
			double rho1 = data[0];
			double theta1 = data[1];
			double cosTheta = Math.cos(theta1);
			double sinTheta = Math.sin(theta1);
			double x0 = cosTheta * rho1;
			double y0 = sinTheta * rho1;
			lineList.add(new Line(x0 + 10000 * -sinTheta, y0 + 10000 * cosTheta, x0 - 10000 * -sinTheta, y0 - 10000*cosTheta));
		} 
		
		// if (!lines.empty()) {
		// 	for (int i = 0; i < lines.rows(); i++) {
		// 		lineList.add(new Line(lines.get(i, 0)[0], lines.get(i, 0)[1],
		// 			lines.get(i, 0)[2], lines.get(i, 0)[3]));
		// 	}
		// }
	}

	/**
	 * Filters out lines that do not meet certain criteria.
	 * @param inputs The lines that will be filtered.
	 * @param minLength The minimum length of a line to be kept.
	 * @param angle The minimum and maximum angle of a line to be kept.
	 * @param outputs The output lines after the filter.
	 */
	private void filterLines(List<Line> inputs,double minLength,double[] angle,
		List<Line> outputs) {
		outputs = inputs.stream()
				.filter(line -> line.lengthSquared() >= Math.pow(minLength,2))
				.filter(line -> (line.angle() >= angle[0] && line.angle() <= angle[1])
				|| (line.angle() + 180.0 >= angle[0] && line.angle() + 180.0 <= angle[1]))
				.collect(Collectors.toList());
	}




}

