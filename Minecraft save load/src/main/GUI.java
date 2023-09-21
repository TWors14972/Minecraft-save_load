package main;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI implements ActionListener, ListSelectionListener, WindowListener, MouseListener {
	private JFrame frame;
	private JList<File> l0, l1;
	private JTextField name;
	private JButton accept, delete;
	private ButtonGroup bg;
	private JRadioButton save, load;

	public GUI() {
		//Set size of window and components relative to current screen resolution (tested/created in 1920x1080)
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fs = new Dimension((int)(screenSize.width / 4.25), (int)(screenSize.height/ 2.25));		
		Dimension p0s = new Dimension(fs.width, (int)(fs.height * .825));
		Dimension p1s = new Dimension(fs.width, fs.height - p0s.height);
		Dimension ls = new Dimension((int)(p0s.width/2.5), (int)(p0s.height*.95));

		//Initialize the various components in the window
		bg = new ButtonGroup();
		save = new JRadioButton("Save"); bg.add(save); save.addActionListener(this);
		load = new JRadioButton("Load"); bg.add(load); load.addActionListener(this);

		accept = new JButton("Save"); accept.addActionListener(this); accept.setEnabled(false);
		delete = new JButton("Delete"); delete.addActionListener(this); delete.setEnabled(false);

		Dimension ns = new Dimension((int)(p1s.width/4.25), (int)(p1s.height/3.5));
		name = new JTextField(); 
		name.setSize(ns); name.setMaximumSize(ns); name.setMinimumSize(ns); name.setPreferredSize(ns);

		l0 = new JList<>(); l1 = new JList<>();
		l0.setPreferredSize(ls); l1.setPreferredSize(ls);
		l0.setMaximumSize(ls); l1.setMaximumSize(ls);
		l0.setMinimumSize(ls); l1.setMinimumSize(ls);
		l0.setSize(ls); l1.setSize(ls);
		l0.setEnabled(true); l1.setEnabled(true);
		l0.setCellRenderer(new CCellRenderer()); l1.setCellRenderer(new CCellRenderer());
		l0.addMouseListener(this); l1.addMouseListener(this);
		l0.addListSelectionListener(this); l1.addListSelectionListener(this);

		frame = new JFrame();
		frame.setSize(fs);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JPanel p0 = new JPanel();
		p0.add(l0); p0.add(l1);
		p0.setPreferredSize(p0s);
		p0.setMaximumSize(p0s);
		p0.setMinimumSize(p0s);
		p0.setSize(p0s);
		frame.add(p0);

		JPanel p1 = new JPanel();
		p1.add(accept);
		p1.add(delete);
		p1.add(name);
		p1.add(save); 
		p1.add(load);
		frame.add(p1);

		p1.setPreferredSize(p1s);
		p1.setMaximumSize(p1s);
		p1.setMinimumSize(p1s);
		p1.setSize(p1s);

		save.doClick();

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		frame.addMouseListener(this);
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent arg0) {
		//If button to accept pressed, load or save depending on which radio button is selected
		if (arg0.getSource().equals(accept)) {
			if (save.isSelected()) Save.save();		
			else Load.load();
		}

		//If delete button pressed, confirm with user and delete selected file(s)
		else if (arg0.getSource().equals(delete))
			Delete.confirm();

		//Radio buttons have changed state, adjust contents of l0 to save or load, change accept button text, and clear l1
		else {
			File[] saves = null;
			int index0 = -1, index1 = -1;

			if (save.isSelected()) {saves = Utility.getSaves(); accept.setText("Save");}							
			else {saves = Utility.saveDirectory.listFiles(); accept.setText("Load");}

			if (!l0.isSelectionEmpty()) {
				for (int i = 0; i < saves.length; i++) if (saves[i].getName().equals(l0.getSelectedValue().getName())) {index0 = i; break;}
				index1 = l1.getSelectedIndex();

			}	
			l0.setListData(saves); l0.setSelectedIndex(index0);
			l1.setSelectedIndex(index1);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		//Set button states for saving depending on the new selection
		if (save.isSelected()) {
			if (e.getSource().equals(l0)) {
				if (l0.isSelectionEmpty()) {				
					l1.setListData(new File[] {});
					accept.setEnabled(false);
					delete.setEnabled(false);				
					return;
				}	
				accept.setEnabled(true);
				delete.setEnabled(true);

				for (File f : Utility.saveDirectory.listFiles()) 
					if (f.getName().equals(l0.getSelectedValue().getName())) {
						l1.setListData(f.listFiles());
						return;
					}	
				l1.setListData(new File[] {});
			}
			else {
				accept.setEnabled(true);
				delete.setEnabled(true);
			}
		}
		
		//Set button states for loading depending on the new selection
		else {
			if (e.getSource().equals(l1)) {				
				accept.setEnabled(!l1.isSelectionEmpty());
				delete.setEnabled(true);
			}
			else {
				accept.setEnabled(false);
				if (l0.isSelectionEmpty()) {
					l1.setListData(new File[] {});
					delete.setEnabled(false);
				}
				else {
					l1.setListData(Utility.saveDirectory.listFiles()[l0.getSelectedIndex()].listFiles());
					delete.setEnabled(true);
				}
			}
		}
	}

	public void windowClosing(WindowEvent arg0) {
		//If any new saves have been created, present user with option to compress them to .zip files to save space before closing application
		if (Utility.newSaves.size() != 0) {
			if (JOptionPane.showConfirmDialog(frame, "Convert all new saves into .zips now?", "Convert Saves", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				for (File f : Utility.newSaves) {
					try {
						String filepath = f.getAbsolutePath();
						File zip = Utility.zipFile(f, f.getName());
						zip.renameTo(new File(filepath + ".zip"));
						Delete.delete(f);
						Delete.delete(new File(Utility.temporary));
						JOptionPane.showMessageDialog(frame, f.getName() + " sucessfully compressed!");
					}
					catch (Exception e) {
						if (new File(Utility.temporary).exists())
							try {Delete.delete(new File(Utility.temporary));}
						catch (Exception e1) {}
						JOptionPane.showMessageDialog(frame, "Unable to compress save " + f.getName());	
					}
				}
				JOptionPane.showMessageDialog(frame, "All new save files successfully compressed! Program exiting.");
			}
		}			
		System.exit(0);
	}

	public void mouseClicked(MouseEvent arg0) {
		//If user right clicks, deselect from l1 first, then l0
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			(l1.isSelectionEmpty() ? l0 : l1).clearSelection();
		}
	}	

	//getters
	public JFrame getFrame() {return frame;}
	public JList<File> getL0() {return l0;}
	public JList<File> getL1() {return l1;}
	public JTextField getName() {return name;}
	public JButton getAccept() {return accept;}
	public JButton getDelete() {return delete;}
	public ButtonGroup getBG() {return bg;}
	public JRadioButton getSave() {return save;}
	public JRadioButton getLoad() {return load;}

	//Custom renderer for lists
	@SuppressWarnings("serial")
	private static class CCellRenderer extends JLabel implements ListCellRenderer<File> {
		public Component getListCellRendererComponent(JList<? extends File> arg0, File arg1, int arg2, boolean arg3,
				boolean arg4) {
			setText(arg1.getName());	 
			setEnabled(true);
			setFont(arg0.getFont());
			if (arg3) {
				setForeground(Color.BLUE);
			} else {
				setBackground(arg0.getBackground());
				setForeground(arg0.getForeground());
			}
			return this;
		}
	}

	//Inherited, but unused, Listener functions
	public void windowActivated(WindowEvent arg0) {}	
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}	
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
