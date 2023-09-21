package main;
import java.io.File;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

public class Load {	
	private final static JFrame frame;
	private final static JList<File> l0, l1;

	static {
		frame = Utility.gui.getFrame();
		l0 = Utility.gui.getL0();
		l1 = Utility.gui.getL1();		
	}

	public static void load() {
		Path old_save = null; 
		File new_save;
		File backup = new File(Utility.temporary + "\\" + "backup"); //File being overwritten moved here in case the load fails, allowing user to at least return to old save
		
		try {
			//Catches the load button erroneously being available if a world is not selected
			if (l0.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please select world and save to load");
				return;
			}
			//Catches the load button erroneously being available if a save is not selected
			else if (l1.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please select save to load");
				return;
			}
			
			//Finds the world save file being overwritten
			for (File f : Utility.getSaves()) {	
				if (f.getName().equals(l0.getSelectedValue().getName())) {
					old_save = f.toPath(); break;
				}
			}
			
			//If no old save exists, the world is being restored; confirm with user first
			if (old_save == null) {
				if (JOptionPane.showConfirmDialog(frame, "This world was previously deleted - would you like to restore it?", "Restore World?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
				old_save = Paths.get(Utility.gameDirectory + "\\" + l0.getSelectedValue().getName());
			}						
			
			//Creates a backup of the world in a temporary directory
			else if (old_save.toFile().exists()) {
				backup.mkdirs();
				Files.move(old_save, backup.toPath().resolve(old_save.getFileName()));	
			}

			//If the file being loaded is a .zip file, unzip it first
			new_save = (l1.getSelectedValue().getName().endsWith(".zip") ? Utility.unzip(l1.getSelectedValue(), old_save.toFile().getName()) : Utility.copyDirectory(l1.getSelectedValue(), l1.getSelectedValue().getName()));
			
			//Name the save file after the world to prevent errors
			new_save.renameTo(old_save.toFile());

			//The load was successful at this point, delete the temporary backup directory and the old save with it
			Delete.delete(new File(Utility.temporary));
			
			JOptionPane.showMessageDialog(frame, l1.getSelectedValue().getName() + " loaded successfully!");
		}
		//This error would occur before the world being loaded could be moved, delete the backup folder and proceed normally
		catch (AccessDeniedException e0) {
			backup.delete();
			JOptionPane.showMessageDialog(frame, "This world is currently being played; please save and quit to title and try again.");
		}
		//Fix an unexpected error by trying to load the old file back to its original location if one exists
		catch (Exception e1) {
			try {
				//A save was being overwritten, try to load original file back to location
				if (old_save.toFile().exists()) {
					Files.move(Paths.get(backup.getAbsolutePath() + "\\" + old_save.toFile().getName()), Utility.gameDirectory.toPath().resolve(Paths.get(backup.getAbsolutePath() + "\\" + old_save.toFile().getName()).getFileName()));
					(new File(Utility.temporary)).delete();
					JOptionPane.showMessageDialog(frame, "Error loading, old save successfully restored.");
				}
				//A world was being restored from a backup save, no original save to restore
				else {
					JOptionPane.showMessageDialog(frame, "Failed to restore world.");
				}

			}
			//Inform user if original save was unable to be restored
			catch (Exception e2) {
				JOptionPane.showMessageDialog(frame, "Error loading, old save could NOT be restored. Please check application folder for a backup of the world being overwritten.");
				e1.printStackTrace();
			}	
			e1.printStackTrace();
		}
	}
}
