import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

//import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Utils {
	public static Point2D.Double intersect(Point2D.Double a, Point2D.Double b, Point2D.Double c, Point2D.Double d){
		Point2D.Double result=new Point2D.Double(0,0);
		double deterA = b.x*a.y - a.x*b.y;
		double deterB = d.x*c.y - c.x*d.y;
		if(a.x==b.x){
			result.x = a.x;
			result.y = (d.y-c.y)/(d.x-c.y)*result.x + deterB/(d.x-c.x);
		}
		else{
			if(c.x==d.x){
				result.x = c.x;
			}
			else{
				result.x = (deterB/(d.x-c.x) - deterA/(b.x-a.x))/((b.y-a.y)/(b.x-a.x) - (d.y-c.y)/(d.x-c.x));
			}
		result.y = (b.y-a.y)/(b.x-a.y)*result.x + deterA/(b.x-a.x);
		}
	return result;
}
		


	public static Point2D.Double map(Point2D.Double raw, Point2D.Double[]
			inputRef, Point2D.Double[] outputRef) {
			Point2D.Double a = inputRef[0];
			Point2D.Double b = inputRef[1];
			Point2D.Double c = inputRef[2];
			Point2D.Double d = inputRef[3];
			Point2D.Double p = raw;
			
			Point2D.Double x = intersect(a, b, c, d);
			Point2D.Double y = intersect(b, c, d, a);
			
			Point2D.Double e = intersect(p, x, d, a);
			Point2D.Double f = intersect(p, y, a, b);
			Point2D.Double g = intersect(p, x, b, c);
			Point2D.Double h = intersect(p, y, c, d);

			double relativeX = (p.x - e.x)/(g.x - e.x);
			double relativeY = (p.x - f.x)/(h.x - f.x);

			return new Point2D.Double(outputRef[0].x + (outputRef[1].x-outputRef[0].x)*relativeX, outputRef[0].y +
			(outputRef[3].y - outputRef[0].y)*relativeY);
	}
	
//	private static Vector2D perpendicular(Vector2D v) {
//		return new Vector2D(-v.getY(), v.getX());
//	}
	
//	private static Double length(Vector2D v) {
//		return Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY());
//	}
	
	public static Point pointDoubleToInt(Point2D.Double p) {
		return new Point((int) p.x, (int) p.y);
	}
}