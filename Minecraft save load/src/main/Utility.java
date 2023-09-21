package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

public class Utility {
	public final static File gameDirectory;
	public final static File saveDirectory;
	public final static String temporary, jar;
	public final static ArrayList<File> newSaves;
	public final static GUI gui;

	static {
		String s0 = System.getProperty("user.dir");
		String s1 = "";

		for (int i = 0; i < 3; i++) {
			s1 += s0.substring(0, s0.indexOf("\\") + 1);
			s0 = s0.substring(s0.indexOf("\\") + 1);
		}

		//jar = ClassLoader.getSystemClassLoader().getResource(".").getPath().replace("%20", " "); //for when launching from jar
		jar = "C:\\Users\\HashtagTSwagg\\Desktop\\Applications\\Minecraft save load"; //for when launching in IDE

		gameDirectory = new File(s1 + "\\AppData\\Roaming\\.minecraft\\saves");
		saveDirectory = new File(jar + "\\saves"); 					
		temporary = jar + "\\temp";

		newSaves = new ArrayList<>();
		gui = new GUI();
	}

	public static void main(String[] yargs) {}

	public static File zipFile(File f, String name) throws IOException {
		File zip = new File(temporary + "\\zip\\" + name + ".zip");
		File copy = copyDirectory(f, name);		

		if (!zip.getParentFile().exists()) zip.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(zip);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		zip(copy, copy.getName(), zipOut);
		zipOut.close();
		fos.close();
		return zip;
	}

	public static void zip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zip(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	public static File unzip(File f, String name) throws IOException {
		File destDir = new File(temporary);
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = new File(destDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!newFile.exists()) newFile.mkdir();
				zipEntry = zis.getNextEntry();
				continue;
			}

			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();

		File temp = new File(destDir.getAbsolutePath() + "\\" + f.getName().substring(0, f.getName().indexOf(".zip")));

		return temp;
	}

	public static File copyDirectory(File file, String name) throws IOException {
		if (!file.exists()) return null;

		File copy = new File(temporary + "\\copy\\" + name); copy.mkdirs();		
		Path path = Paths.get(file.getAbsolutePath());

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path path1, BasicFileAttributes attrs) throws IOException {
				String temp =  path1.toString().split(file.getName())[1];
				File f1 = new File(copy.getAbsolutePath() + temp.substring(0, temp.lastIndexOf("\\")));
				if (!f1.exists()) f1.mkdirs();
				Files.copy(path1, f1.toPath().resolve(path1.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;}
		});       
		return copy;
	}

	public static File[] getSaves() {
		ArrayList<File> temp = new ArrayList<>(gameDirectory.listFiles().length);
		for (File f : gameDirectory.listFiles()) if (!f.getName().endsWith(".zip")) temp.add(f);

		File[] saves = new File[temp.size()];
		for (int i = 0; i < saves.length; i++) saves[i] = temp.get(i); 
		return saves;
	}

	public static String getSaveNames(List<File> list) {
		String temp = ""; 
		if (list.size() > 1) {
			for (int i = 0; i < list.size()-2; i++) temp += list.get(i).getName() + ", ";			
			temp += " " + list.get(list.size()-1).getName();
		}
		else temp = " " + list.get(0).getName();
		return temp;
	}

	public static void createErrorLog(Exception e) {
		File logs = new File(jar + "\\error logs");
		if (!logs.exists()) logs.mkdir();

		File error = new File(logs + "\\" + (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(Calendar.getInstance().getTime()) + ".txt");

		try {
			FileWriter fw = new FileWriter(error);
			fw.write(e.getMessage());
			fw.close();
		}
		catch (Exception e0) {
			JOptionPane.showMessageDialog(null, "Error writing log!");
		}
	}	
}
