package application;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class guiDemo implements ActionListener {
	
	String absolutePathOfPhoto = "";

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new guiDemo().createGui();
                
            }
        });
	}
	public void createGui() {
        JFrame frame = new JFrame("Photo Descriptor");
        frame.setSize(300, 140);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridBagLayout());
        frame.getContentPane().add(panel);
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        GridBagConstraints c = new GridBagConstraints();

        JButton button1 = new JButton("Open a file...");
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(40, 40, 40, 40);
        panel.add(button1, c);
        
        button1.addActionListener(this);
    }
	
	private static void describePhoto(String absolutePath) throws Exception {
		DetectLabels photoDescription = new DetectLabels();
		String finalDescription = photoDescription.finalDescription(absolutePath);
		photoDescription.playFinalDescription(finalDescription);
	}

    public void actionPerformed(ActionEvent e) {
        String com = e.getActionCommand();
 
        if (com.equals("Open a file...")) {
            JFileChooser j = new JFileChooser();
            //"C:\\Users\\Admin\\Desktop\\Hallym 6 Semester\\Cloud Computing\\Project\\Photos"
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "jpeg");
            j.setFileFilter(filter);
 
            int r = j.showSaveDialog(null);
 
            if (r == JFileChooser.APPROVE_OPTION)
 
            {
                absolutePathOfPhoto = j.getSelectedFile().getAbsolutePath();
                try {
					describePhoto(absolutePathOfPhoto);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
    }
}
