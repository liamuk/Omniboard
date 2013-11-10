import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class OverlayFrame extends JFrame implements MouseListener, MouseMotionListener {
	private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	private static final Stroke STROKE = new BasicStroke(4);
	private List<LinkedList<Point>> lines = new LinkedList<LinkedList<Point>>();

	public OverlayFrame() {
		super();
		this.setDefaultCloseOperation(OverlayFrame.EXIT_ON_CLOSE);
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.setSize(OverlayFrame.SCREEN_SIZE);
		this.setBackground(new Color(0, 0, 0, 0.01f));
		this.setVisible(false);
//		LinkedList<Point> l = new LinkedList<>();
//		l.addAll(Arrays.asList(new Point[] {new Point(0, 0), new Point(100, 100), new Point(100, 200)}));
//		lines.add(l);
//		l = new LinkedList<>();
//		l.addAll(Arrays.asList(new Point[] {new Point(500, 250), new Point(130, 460), new Point(750, 200)}));
//		lines.add(l);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void clear() {
		lines = new LinkedList<LinkedList<Point>>();
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(OverlayFrame.STROKE);
		g2d.setColor(Color.RED);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (List<Point> line : this.lines) {
			Iterator<Point> iterator = line.iterator();
			Point current = iterator.next();
			for (int i = line.size(); i > 1; --i) {
				Point next = iterator.next();
				g2d.drawLine(current.x, current.y, next.x, next.y);
				current = next;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
		LinkedList<Point> line = new LinkedList<>();
		line.add(me.getPoint());
		this.lines.add(line);
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if(this.lines.size() > 0) {
			this.lines.get(this.lines.size() - 1).add(me.getPoint());
			this.repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if (this.lines.size() > 0) {
			this.lines.get(this.lines.size() - 1).add(me.getPoint());
			this.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {
	}
}