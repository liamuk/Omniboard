import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import wiiusej.WiiUseApiManager;
import wiiusej.Wiimote;
import wiiusej.values.IRSource;
import wiiusej.wiiusejevents.physicalevents.IREvent;

public class Main extends WiiMoteAdapter {
	private static Robot robot;
	private static Icon targetIcon;
	private static final Dimension SCREEN_RESOLUTION = Toolkit
			.getDefaultToolkit().getScreenSize();
	private static final int CALIBRATION_TARGET_OFFSET = 20;

	static {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		targetIcon = new ImageIcon("target.png");
	}

	public static void main(String[] args) {
		Main app = new Main();

		Wiimote wiimote = WiiUseApiManager.getWiimotes(1, true)[0];
		wiimote.activateIRTRacking();
		wiimote.addWiiMoteEventListeners(app);

		// Wiimote[] wiimotes = WiiUseApiManager.getWiimotes(1, true);
		// WiiuseJGuiTest gui = null;
		// if (wiimotes.length > 0) {
		// gui = new WiiuseJGuiTest(wiimotes[0]);
		// } else {
		// gui = new WiiuseJGuiTest();
		// }
		// gui.setDefaultCloseOperation(3);
		// gui.setVisible(true);
	}

	private boolean calibrating = true;
	private int count = 0;
	private JFrame target;
	private Point2D.Double[] calibrationPoints = {
			// new Point2D.Double(CALIBRATION_TARGET_OFFSET,
			// CALIBRATION_TARGET_OFFSET),
			// new Point2D.Double(SCREEN_RESOLUTION.width -
			// CALIBRATION_TARGET_OFFSET, CALIBRATION_TARGET_OFFSET),
			// new Point2D.Double(SCREEN_RESOLUTION.width -
			// CALIBRATION_TARGET_OFFSET, SCREEN_RESOLUTION.height -
			// CALIBRATION_TARGET_OFFSET),
			// new Point2D.Double(CALIBRATION_TARGET_OFFSET,
			// SCREEN_RESOLUTION.height - CALIBRATION_TARGET_OFFSET)
			new Point2D.Double(50, 50), new Point2D.Double(500, 50),
			new Point2D.Double(500, 500), new Point2D.Double(50, 500) };

	private Point2D.Double[] referencePoints = new Point2D.Double[4];

	private boolean controllingMouse = false;

	private long lastIREvent = System.currentTimeMillis();
	private static final long RELEASE_DELAY = 500;

	public Main() {
		target = new JFrame();
		target.setUndecorated(true);
		// target.setAlwaysOnTop(true);
		target.setSize(64, 64);

		target.add(new JLabel(targetIcon));
		target.setVisible(true);

		target.setLocation(Utils.pointDoubleToInt(calibrationPoints[count]));

		new Thread() {
			public void run() {
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
		}.start();
	}

	@Override
	public void onIrEvent(IREvent ire) {
		System.out.println("New IREvent: " + ire);

		IRSource point = ire.getIRPoints()[0];
		System.out.println("Point " + count + ": " + point);

		if (calibrating) {
			if (count == 0
					|| new Point2D.Double(point.getRx(), point.getRy())
							.distance(referencePoints[count - 1]) > 100)
				referencePoints[count] = new Point2D.Double(point.getRx(),
						point.getRy());
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

			Point screenCoord = Utils.pointDoubleToInt(Utils.map(
					new Point2D.Double(point.getRx(), point.getRy()),
					referencePoints, calibrationPoints));
			System.out.println(screenCoord);
			robot.mouseMove(screenCoord.x, screenCoord.y);
			robot.mousePress(InputEvent.BUTTON1_MASK);
		}

		// @Override
		// public void onButtonsEvent(WiimoteButtonsEvent wbe) {
		// if(wbe.isButtonHomeHeld()) {
		// WiiUseApiManager.getInstance().shutdown();
		// System.exit(0);
		// }
		// }
	}
}