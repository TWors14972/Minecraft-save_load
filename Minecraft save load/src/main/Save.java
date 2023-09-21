package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Save {
	private final static JFrame frame;
	private final static JList<File> l0, l1;
	private final static JTextField name;
	
	static {
		frame = Utility.gui.getFrame();
		l0 = Utility.gui.getL0();
		l1 = Utility.gui.getL1();
		name = Utility.gui.getName();		
	}
	
	public static void save() {
		String n = null;
		boolean overwrite = false;

		if (l0.isSelectionEmpty()) {JOptionPane.showMessageDialog(frame, "Please select a world to save.");	return;}

		try {
			File test = new File(Utility.saveDirectory + "\\" + l0.getSelectedValue().getName());
			
			//If no name is selected, make sure a file is not being overwritten, keep name if so, otherwise use date/time for name.
			if (name.getText().equals("")) {
				if (l1.getSelectedIndex() != -1 && JOptionPane.showConfirmDialog(frame, "Save " + l1.getSelectedValue().getName() + " is currently selected, would you like to overwrite it?", "Overwrite Save?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					overwrite = true;
					File f1 = new File(Utility.temporary + "\\overwrite"); f1.mkdirs();
					n = l1.getSelectedValue().getName();
					Files.move(l1.getSelectedValue().toPath(), f1.toPath().resolve(l1.getSelectedValue().toPath().getFileName()));
				}
				else n = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(Calendar.getInstance().getTime());
			}
			
			//If the name is not blank, make sure it doesn't match the name of any existing saves, confirm with user if they want to overwrite if it does
			else if (test.exists()) {
				for (File f : new File(Utility.saveDirectory + "\\" + l0.getSelectedValue().getName()).listFiles()) {
					if (f.getName().equals(name.getText())) {
						if (JOptionPane.showConfirmDialog(frame, "Save " + f.getName() + " already exists, would you like to overwrite it?", "Overwrite Save?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							overwrite = true;
							File f1 = new File(Utility.temporary + "\\overwrite"); f1.mkdirs();
							n = name.getText();
							Files.move(f.toPath(), f1.toPath().resolve(f.toPath().getFileName()));
							break;
						}
						else return;
					}
				}
			
				if (n == null) n = name.getText();
			}
			
			else n = name.getText();

			//Creates the save file, makes sure the directory its saving to exists, and adds the new save to the newSaves list
			File temp = Utility.copyDirectory(l0.getSelectedValue(), n);
			File destination = new File(Utility.saveDirectory.getAbsolutePath() + "\\" + l0.getSelectedValue().getName());
			if (!destination.exists()) destination.mkdirs();
			temp.renameTo(new File(destination.getAbsolutePath() + "\\" + n));
			temp = new File(destination.getAbsolutePath() + "\\" + n);
			Utility.newSaves.add(temp);
			Delete.delete(new File(Utility.temporary));

			//Update the list of saves and clear the name field
			l1.setListData(destination.listFiles());
			name.setText("");
			
			JOptionPane.showMessageDialog(frame, name.getText() + " saved successfully!");
		}
		catch(FileNotFoundException e0) {

		}
		//If an error occurs and a save was being overwritten, attempt to restore the old save before continuing operation normally
		catch (Exception e1) {
			if (overwrite) {
				try {
					File temp = new File(Utility.temporary + "\\overwrite");
					Files.move(temp.toPath(), (new File(Utility.saveDirectory + "\\" + Utility.getSaves()[l0.getSelectedIndex()].getName())).toPath().resolve(temp.toPath().getFileName()));
					JOptionPane.showMessageDialog(frame, "Error saving, previous save not overwritten");	
				}
				catch (Exception e2) {
					JOptionPane.showMessageDialog(frame, "Error saving, unable to restore previous save.");
					e2.printStackTrace();
				}
				e1.printStackTrace();
			}
			try {
				Delete.delete(new File(Utility.temporary));
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Error saving, could not delete temporary folders");
			}
			JOptionPane.showMessageDialog(frame, "Error saving");
		}
	}
}
