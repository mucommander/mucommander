package com.mucommander.ui;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class AboutDialog extends FocusDialog implements ActionListener, KeyListener {
	
	private SinEffectPanel effectPanel;
	private JButton okButton;

	private static String aboutText = "<html><body text=\"#00FFFF\" bgcolor=\"#000084\">"
		+"<table width=\"387\">"
		+"<tr><td>"
		+"<b>muCommander v"+com.mucommander.Launcher.MUCOMMANDER_VERSION+"</b>"
		+"<br>&copy;2002 Maxence Bernard, all rights reserved."
		+"<br><br><i>Code &amp; design:</i> Maxence Bernard."
		+"<br><i>Contributors:</i> Ewan Harrow, Nicolas Rinaudo, Roger Sondermann."
		+"<br><br>muCommander is powered by jEdit, Ant, yGuard and NSIS."
		+"<br>SMB support provided by the jCIFS library under the LGPL license."
		+"<br><br><i>Official website</i>: <b>http://www.mucommander.com</b>."
		+"<br><br><b>Java VM:</b> "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version")
//		+"<br><br><b>Memory:</b> "+runtime.freeMemory()+" / "+runtime.totalMemory()
		+"<br><b>OS:</b> "+System.getProperty("os.name")+" "+System.getProperty("os.arch")+" "+System.getProperty("os.version")
		+"</tr></td>"
		+"</table>"
		+"</body></html>";


	public AboutDialog(MainFrame mainFrame) {
		super(mainFrame, "About muCommander", mainFrame);
			
		okButton = new JButton("OK");
		okButton.addKeyListener(this);
		setInitialFocusComponent(okButton);
		
		Container contentPane = getContentPane();
		Color bgColor = new Color(0, 0, 132);
		contentPane.setBackground(bgColor);
		// Resolves the URL of the image within the JAR file
		URL imageURL = getClass().getResource("/logo.png");
//		effectPanel = new LifeEffectPanel(new ImageIcon(imageURL).getImage());
        effectPanel = new SinEffectPanel(new ImageIcon(imageURL).getImage());
        contentPane.add(effectPanel, BorderLayout.NORTH);

		JLabel label = new JLabel(aboutText);
		label.setBackground(bgColor);
		contentPane.add(label, BorderLayout.CENTER);

		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);
	
		okButton.setBackground(bgColor);
		JPanel okPanel = DialogToolkit.createOKPanel(okButton, this);
		okPanel.setBackground(bgColor);
		contentPane.add(okPanel, BorderLayout.SOUTH);
	}


	public void actionPerformed(ActionEvent e) {
		effectPanel.stop();
		dispose();
	}
	

	/***********************
	 * KeyListener methods *
	 ***********************/

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (e.getSource()==okButton && keyCode == KeyEvent.VK_ESCAPE) {
			okButton.doClick();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

}