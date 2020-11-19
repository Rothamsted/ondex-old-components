package net.sourceforge.ondex.util.metadata.elements;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;

/**
 * @author weilej
 *
 */
public class ErrorDialog extends JDialog implements ActionListener {

	//####FIELDS####
	
	private JPanel topPanel, bottomPanel;
	
	private JScrollPane centerPanel;
	
	private JButton moreLess;
	
	private BufferedImage errorImg;
	
	private Throwable throwable;
	
	private Thread thread;
	
	private boolean running;
	
	private Dimension minDim, maxDim;

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 3398440747424344511L;
	
	//####CONSTRUCTOR####
	
	private ErrorDialog(boolean running, Throwable throwable, Thread thread) {
		super(running ? MetaDataEditor.getInstance() : null,"Error");
		this.throwable = throwable;
		this.thread = thread;
		this.running = running;
		setupGUI();
		boolean debug = false;
		if (debug) throwable.printStackTrace();
	}

	
	//####METHODS####

	private void setupGUI() {
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		
		topPanel = new JPanel(new BorderLayout());
		JPanel leftPanel = makeImgPanel();
		if (leftPanel != null) {
			topPanel.add(leftPanel,BorderLayout.WEST);
		}
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(makeMsgPanel(),BorderLayout.CENTER);
		rightPanel.add(makeMoreLessPanel(),BorderLayout.SOUTH);
		topPanel.add(rightPanel, BorderLayout.CENTER);
		
		centerPanel = createStackPanel();
		bottomPanel = createButtonPanel();
		
		less();
		
		int w_self = getWidth()+100;
		int h_self = getHeight();
		int x,y,w,h;
		
		if (running) {
			x = getParent().getX();
			y = getParent().getY();
			w = getParent().getWidth();
			h = getParent().getHeight();
		} else {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			x = 0;
			y = 0;
			w = screen.width;
			h = screen.height;
		}
		this.setBounds(x + (w-w_self)/2, y + (h-h_self)/2, w_self, h_self);
		
		minDim = getSize();
		maxDim = new Dimension(getWidth(),getHeight()+100);
		
		topPanel.setMaximumSize(new Dimension(1280,topPanel.getHeight()));
		bottomPanel.setMaximumSize(bottomPanel.getSize());
		
		setVisible(true);
	}
	
	private JScrollPane createStackPanel() {
		
		StringBuilder b = new StringBuilder();
		
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
		String date = df.format(new Date(System.currentTimeMillis()));
		
		b.append("Date: "+date+"\n");
		
		String build = extractBuildNumber();
		if (build != null) {
			b.append("Build:"+build+"\n");
		}
		
		String arch = System.getProperty("os.arch");
		String osname = System.getProperty("os.name");
		String osversion = System.getProperty("os.version");
		
		b.append("System: "+arch+" "+osname+" v"+osversion+"\n");
		
		b = buildStackTrace(b, throwable);
		
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setText(b.toString());
		return new JScrollPane(area);
	}
	
	private StringBuilder buildStackTrace(StringBuilder b, Throwable t) {
		String threadname = (thread != null)? thread.getName() : "unknown";
		b.append(t.toString()+" in Thread "+threadname+"\n");
		for (StackTraceElement e : throwable.getStackTrace()) {
			b.append("        at "+e.getClassName()+"("+e.getFileName()+":"+e.getLineNumber()+")"+"\n");
		}
		if (t.getCause() != null) {
			b.append("Caused by: ");
			buildStackTrace(b, t.getCause());
		}
		return b;
	}
	
	private String extractBuildNumber() {
		try {
			ZipFile jar = new ZipFile("ovtk2.jar");
			ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");
			BufferedReader br = new BufferedReader(
					new InputStreamReader(jar.getInputStream(manifest)));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("Hudson-Build-Number:")) {
					br.close();
					String[] split = line.split(":");
					return split[1];
				}
			}
			br.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private JPanel makeMoreLessPanel() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		moreLess = makeButton("Details >>","more");
		p.add(moreLess);
		return p;
	}
	
	private JPanel createButtonPanel() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.add(makeButton("OK","ok"));
		return p;
	}
	
	private JButton makeButton(String title, String actionCommand) {
		JButton button = new JButton(title);
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		return button;
	}
	
	private JPanel makeMsgPanel() {
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.PAGE_AXIS));
		
		String msg = (throwable.getMessage() != null) ? throwable.getMessage() : throwable.toString();
		rightPanel.add(new JLabel(" "));
		rightPanel.add(new JLabel("An error occurred:"));
		rightPanel.add(new JLabel(msg));
		
		return rightPanel;
	}
	
	private JPanel makeImgPanel() {
		String s = File.separator;
		File imgFile = new File("config"+s+"themes"+s+"default"+s+"icons"+s+"error25.png");
		if (imgFile.exists() && imgFile.canRead()) {
			try {
				errorImg = ImageIO.read(imgFile);
				if (errorImg != null) {
					JPanel leftPanel = new JPanel() {
						
						private static final long serialVersionUID = 858217031036743682L;

						public void paint(Graphics g) {
							super.paint(g);
							Graphics2D g2 = (Graphics2D) g;
							g2.drawImage(errorImg, null, 10, 17);
						}
						
					};
					Dimension d = new Dimension(errorImg.getWidth() + 20,
							  errorImg.getHeight()+ 20);
					leftPanel.setMinimumSize(d);
					leftPanel.setMaximumSize(d);
					leftPanel.setPreferredSize(d);
					leftPanel.setSize(d);
					return leftPanel;
				}
			} catch (IOException ioe) {
				return null;
			}
		}
		return null;
	}
	
	private void less() {
		moreLess.setText("Details >>");
		moreLess.setActionCommand("more");
		
		getContentPane().removeAll();
		getContentPane().add(topPanel);
		getContentPane().add(bottomPanel);
		
		pack();
		if (minDim != null) {
			setSize(minDim);
		}
	}
	
	private void more() {
		moreLess.setText("<< Details");
		moreLess.setActionCommand("less");
		
		getContentPane().removeAll();
		getContentPane().add(topPanel);
		getContentPane().add(centerPanel);
		getContentPane().add(bottomPanel);
		
		setSize(maxDim);
		validate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("more")) {
			more();
		} else if (cmd.equals("less")) {
			less();
		} else if (cmd.equals("ok")) {
			dispose();
			if (!running) {
				System.exit(1);
			}
		}
		
	}
	
	public static void show(boolean running, Throwable throwable, Thread thread) {
		new ErrorDialog(running, throwable, thread);
	}
	
	public static void show(Throwable throwable) {
		new ErrorDialog(true, throwable, null);
	}
	
}
