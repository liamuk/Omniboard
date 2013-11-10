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
import java.util.Arrays;
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
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setUndecorated(true);
		setAlwaysOnTop(true);
		setSize(SCREEN_SIZE);
		setBackground(new Color(0, 0, 0, 0.01f));
		setVisible(false);
		
		//test
//		LinkedList<Point> l = new LinkedList<>();
//		l.addAll(Arrays.asList(new Point[] {new Point(0, 0), new Point(100, 100), new Point(100, 200)}));
//		lines.add(l);
//		
//		l = new LinkedList<>();
//		l.addAll(Arrays.asList(new Point[] {new Point(500, 250), new Point(130, 460), new Point(750, 200)}));
//		lines.add(l);
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setStroke(STROKE);
		g2d.setColor(Color.RED);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		for(List<Point> line : lines) {
			Iterator<Point> iterator = line.iterator();
			
			Point current = iterator.next();
			for(int i = 0; i < line.size() - 1; ++i) {
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
		
		lines.add(line);
		
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if(lines.size() > 0)
			lines.get(lines.size() - 1).add(me.getPoint());
		
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		lines.get(lines.size() - 1).add(me.getPoint());
		
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent me) {
	}
}