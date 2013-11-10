import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
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
	private static final Dimension SCREEN_RESOLUTION = Toolkit.getDefaultToolkit().getScreenSize();
	private static BufferedImage TARGET_IMAGE;
	private static final int TARGET_OFFSET = 200;
	private static final int TARGET_SIZE = 64;
	//	private static final Stroke STROKE = new BasicStroke(4);
	private static final Point2D.Double[] CALIBRATION_POINTS = {
		new Point2D.Double(Main.TARGET_OFFSET, Main.TARGET_OFFSET),
		new Point2D.Double(Main.SCREEN_RESOLUTION.width - Main.TARGET_OFFSET, Main.TARGET_OFFSET),
		new Point2D.Double(SCREEN_RESOLUTION.width - Main.TARGET_OFFSET, Main.SCREEN_RESOLUTION.height - Main.TARGET_OFFSET),
		new Point2D.Double(Main.TARGET_OFFSET, Main.SCREEN_RESOLUTION.height - Main.TARGET_OFFSET)
	};
	private static final long RELEASE_DELAY = 50;

	static {
		try {
			Main.robot = new Robot();
		} catch(AWTException e) {
			e.printStackTrace();
		}
		try {
			Main.TARGET_IMAGE = ImageIO.read(new File("arrow.png"));
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
	private Point2D.Double[] referencePoints = new Point2D.Double[4];
	private boolean controllingMouse = false;
	private long lastIREvent = System.currentTimeMillis();
	private OverlayFrame overlay = new OverlayFrame();

	@SuppressWarnings("serial")
	public Main() {
		this.target = new JFrame() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(Main.TARGET_IMAGE, 0, 0, Main.TARGET_SIZE, Main.TARGET_SIZE, null);
				//				Graphics2D g2d = (Graphics2D) g;
				//				g2d.setStroke(STROKE);
				//				g2d.setColor(Color.RED);
				//				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				//				g2d.drawOval(4, 4, getWidth() - 8, getHeight() - 8);
				//				g2d.drawLine(4, getHeight() / 2, getWidth() - 8, getHeight() / 2);
				//				g2d.drawLine(getWidth() / 2, 4, getWidth() / 2, getHeight() - 8);
			}
		};
		this.target.setUndecorated(true);
		this.target.setAlwaysOnTop(true);
		this.target.setSize(Main.TARGET_SIZE, Main.TARGET_SIZE);
		//		target.add(new JLabel(targetIcon));
		this.target.setVisible(true);
		this.target.setLocation(Utils.pointDoubleToInt(Main.CALIBRATION_POINTS[this.count]));
		new Thread() {
			public void run() {
				try {
					while(true) {
						if(System.currentTimeMillis() - Main.this.lastIREvent > Main.RELEASE_DELAY) {
							Main.robot.mouseRelease(InputEvent.BUTTON1_MASK);
						}
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void onButtonsEvent(WiimoteButtonsEvent wbe) {
		System.out.println(wbe);
		if (wbe.isButtonAPressed()) {
			WiiUseApiManager.shutdown();
			System.exit(0);
		} else if(wbe.isButtonPlusJustPressed()) {
			this.overlay.setVisible(!this.overlay.isVisible());
		}
	}

	@Override
	public void onIrEvent(IREvent ire) {
		System.out.println("New IREvent: " + ire);
		IRSource[] points = ire.getIRPoints();
		if (points.length > 0)
		{
			IRSource point = points[0];
			System.out.println("Point " + this.count + ": " + point);
			if (this.calibrating) {
				if (this.count == 0 || new Point2D.Double(point.getRx(), point.getRy()).distance(this.referencePoints[this.count - 1]) > 100)
				{
					this.referencePoints[this.count] = new Point2D.Double(point.getRx(), point.getRy());
					if (++this.count >= 4) {
						this.calibrating = false;
						this.controllingMouse = true;
						this.target.setVisible(false);
					} else {
						this.target.setLocation(Utils.pointDoubleToInt(Main.CALIBRATION_POINTS[this.count]));
					}
				}
			} else if (this.controllingMouse) {
				this.lastIREvent = System.currentTimeMillis();
				Point screenCoord = Utils.pointDoubleToInt(Utils.map(new Point2D.Double(point.getRx(), point.getRy()), this.referencePoints, Main.CALIBRATION_POINTS));
				System.out.println(screenCoord);
				Main.robot.mouseMove(screenCoord.x, screenCoord.y);
				Main.robot.mousePress(InputEvent.BUTTON1_MASK);
			}
		}
	}
}