package main;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

public class Delete {
	private final static JFrame frame;
	private final static JList<File> l0, l1;
	private final static JRadioButton save;

	static {
		frame = Utility.gui.getFrame();
		l0 = Utility.gui.getL0();
		l1 = Utility.gui.getL1();
		save = Utility.gui.getSave();		
	}

	//Checks to see if the file exists, and if so, visits and deletes all subfiles in given directory before deleting the directory itself
	public static void delete(File f) throws IOException {
		if (!f.exists()) return;
		Path path = Paths.get(f.getAbsolutePath());

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {Files.delete(dir); return FileVisitResult.CONTINUE;}
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {Files.delete(file); return FileVisitResult.CONTINUE;}
		});       
	}

	public static void confirm() {
		List<File> f = null;
		String name = null;
		int list = -1;		
		//Checks to see if file(s) selected are from the Worlds list or Backups list and confirms the user wants to delete the selected file(s)
		if (!l1.isSelectionEmpty()) {
			f = l1.getSelectedValuesList(); list = 1;
			name = Utility.getSaveNames(f);
			if (JOptionPane.showConfirmDialog(frame, "Would you like to delete backup " + (f.size() == 1 ? "save " : "saves ") + name + "?", "Delete?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
		}
		else if (!l0.isSelectionEmpty()) {
			name = Utility.getSaveNames(l0.getSelectedValuesList());
			if (JOptionPane.showConfirmDialog(frame, "Would you like to delete " + (save.isSelected() ? "WORLD " : "SAVE FOLDER (and all contained backups)") + name + "?", "Delete?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				f = l0.getSelectedValuesList();
				list = 0;		
			}
			else return;
		}
		else return;

		//Requests user input to delete files one more time
		if (JOptionPane.showConfirmDialog(frame, "Are you ABSOLUTELY sure you want to delete" + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
		
		//Checks to see if any of the saves to be deleted have been created during the current session and removes them from the list of new saves if so
		try {					
			//If World list is selected, check all saves under that world
			if (list == 0) {
				for (int i = 0; i < f.size(); i++)
					for (File f2 : f.get(i).listFiles())
						if (Utility.newSaves.contains(f2)) Utility.newSaves.remove(f2);
			}
			//If Backups list is selected, check for that specific file
			else {
				for (int i = 0; i < f.size(); i++)
						Utility.newSaves.remove(f.get(i));
			}

			//Run through and delete every file given
			for (int i = 0; i < f.size(); i++) Delete.delete(f.get(i));           
			if (save.isSelected()) {
				//If deleting a world, deselect indices on both lists and ask to delete any exists backups of the world being deleted
				if (list == 0) {
					l0.setListData(Utility.getSaves());
					l0.setSelectedIndex(-1);
					l1.setSelectedIndex(-1);
					l1.setListData(new File[] {});
					if (JOptionPane.showConfirmDialog(frame, "Would you like to delete all backups of" + name + " as well?", "Delete Backups", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						for (File f1 : Utility.saveDirectory.listFiles()) {if (f1.getName().equals(f.get(0).getName())) Delete.delete(f1);}		
				}
				//If deleting a specific save file, update l1 with remaining backups if any, or to blank list if no file exists
				else {
				    File f1 = null;
					for (File f2 : Utility.saveDirectory.listFiles()) {if (f2.getName().equals(l0.getSelectedValue().getName())) {f1 = f2; break;}}
					if (f1 != null) l1.setListData(f1.listFiles());
					else l1.setListData(new File[] {});
				}
			}
			else {
				//if an entire world backup file is deleted, update l0 to no longer include it, l1 to blank, and deselect everything from both lists 
				if (list == 0) {
					l0.setListData(Utility.saveDirectory.listFiles());
					l1.setListData(new File[] {});
					l0.setSelectedIndex(-1);
					l1.setSelectedIndex(-1);
				}
				//if a single save was deleted, update contents and selection of l1
				else {
					l1.setSelectedIndex(-1);
					l1.setListData(l0.getSelectedValue().listFiles());
				}
			}
			JOptionPane.showMessageDialog(frame, name + " successfully deleted.");
		}
		//If a FileSystemException occurs, the files are most likely locked as a result of being loaded into by the game - inform the user and continue
		catch (FileSystemException e0) {
			JOptionPane.showMessageDialog(frame, "An error has occured - if this world is currently being played, please save and quit to title and try again.");
		}
		//For if a completely unexpected error occurs
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Unknown error, save not deleted.");
		}
	}
}
