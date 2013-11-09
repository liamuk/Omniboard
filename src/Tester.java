import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;


public class Tester {
	public static void main(String[] args) {
		Point2D.Double raw = new Point2D.Double(30, 30);
		Point2D.Double[] inputRef = {new Point2D.Double(0, 0), new Point2D.Double(100, 0), new Point2D.Double(90, 90), new Point2D.Double(0, 100)};
		//Point2D.Double outputRef = new Point2D.Double(0, 0);
		Point2D.Double[] outputRef = {new Point2D.Double(0, 0), new Point2D.Double(100, 0), new Point2D.Double(100, 100), new Point2D.Double(0, 100)};
		int[] outputRes = {100, 100};
		
		System.out.println(Utils.map(raw, inputRef, outputRef));
	}
}
