import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import wiiusej.WiiUseApiManager;
import wiiusej.Wiimote;
import wiiusej.values.IRSource;
import wiiusej.wiiusejevents.physicalevents.IREvent;
import wiiusej.wiiusejevents.physicalevents.WiimoteButtonsEvent;

public class Main extends WiiMoteAdapter {
	private static Robot robot;
	private static BufferedImage TARGET_IMAGE;
	private static final Dimension SCREEN_RESOLUTION = Toolkit.getDefaultToolkit().getScreenSize();
	private static final int TARGET_OFFSET = 200;

	static {
		try {
			robot = new Robot();
		} catch(AWTException e) {
			e.printStackTrace();
		}

		try {
			TARGET_IMAGE = ImageIO.read(new File("arrow.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Main app = new Main();
		Wiimote[] wiimotes = WiiUseApiManager.getWiimotes(1, true);
		if (wiimotes.length > 0) {
			Wiimote wiimote = wiimotes[0];
			wiimote.activateIRTRacking();
			wiimote.addWiiMoteEventListeners(app);
		} else {
			System.err.println("Failed to detect wiimote.");
		}
	}

	private boolean calibrating = true;
	private int count = 0;
	private JFrame target;
	private static final int TARGET_SIZE = 64;
//	private static final Stroke STROKE = new BasicStroke(4);
	private Point2D.Double[] calibrationPoints = {
		new Point2D.Double(TARGET_OFFSET, TARGET_OFFSET),
		new Point2D.Double(SCREEN_RESOLUTION.width - TARGET_OFFSET, TARGET_OFFSET),
		new Point2D.Double(SCREEN_RESOLUTION.width - TARGET_OFFSET, SCREEN_RESOLUTION.height - TARGET_OFFSET),
		new Point2D.Double(TARGET_OFFSET, SCREEN_RESOLUTION.height - TARGET_OFFSET)
	};

	private Point2D.Double[] referencePoints = new Point2D.Double[4];

	private boolean controllingMouse = false;

	private long lastIREvent = System.currentTimeMillis();
	private static final long RELEASE_DELAY = 500;

	@SuppressWarnings("serial")
	public Main() {
		target = new JFrame() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(TARGET_IMAGE, 0, 0, TARGET_SIZE, TARGET_SIZE, null);
//				Graphics2D g2d = (Graphics2D) g;
//				g2d.setStroke(STROKE);
//				g2d.setColor(Color.RED);
//				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				
//				g2d.drawOval(4, 4, getWidth() - 8, getHeight() - 8);
//				g2d.drawLine(4, getHeight() / 2, getWidth() - 8, getHeight() / 2);
//				g2d.drawLine(getWidth() / 2, 4, getWidth() / 2, getHeight() - 8);
			}
		};
		
		target.setUndecorated(true);
		target.setAlwaysOnTop(true);
		target.setSize(TARGET_SIZE, TARGET_SIZE);

//		target.add(new JLabel(targetIcon));
		target.setVisible(true);

		target.setLocation(Utils.pointDoubleToInt(calibrationPoints[count]));

		new Thread() {
			public void run() {
				while(true) {
					if(System.currentTimeMillis() - lastIREvent > RELEASE_DELAY)
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
					
					while (true) {
						if (System.currentTimeMillis() - lastIREvent > RELEASE_DELAY)
							robot.mouseRelease(InputEvent.BUTTON1_MASK);
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

	public void onButtonsEvent(WiimoteButtonsEvent wbe) {
		System.out.println(wbe);
		if(wbe.isButtonAPressed()) {
			WiiUseApiManager.shutdown();
			System.exit(0);
		}
	}

	@Override
	public void onIrEvent(IREvent ire) {
		System.out.println("New IREvent: " + ire);

		IRSource point = ire.getIRPoints()[0];
		System.out.println("Point " + count + ": " + point);

		if (calibrating) {
			if (count == 0 || new Point2D.Double(point.getRx(), point.getRy()).distance(referencePoints[count - 1]) > 100)
				referencePoints[count] = new Point2D.Double(point.getRx(), point.getRy());
			else
				return;

			count++;

			if (count >= 4) {
				calibrating = false;
				controllingMouse = true;
				target.setVisible(false);
			} else {
				target.setLocation(Utils.pointDoubleToInt(calibrationPoints[count]));
			}
		} else if (controllingMouse) {
			lastIREvent = System.currentTimeMillis();

			Point screenCoord = Utils.pointDoubleToInt(Utils.map(new Point2D.Double(point.getRx(), point.getRy()), referencePoints, calibrationPoints));
			System.out.println(screenCoord);
			robot.mouseMove(screenCoord.x, screenCoord.y);
			robot.mousePress(InputEvent.BUTTON1_MASK);
		}
	}
}