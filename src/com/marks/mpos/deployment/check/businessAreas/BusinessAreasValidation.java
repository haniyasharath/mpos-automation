package com.marks.mpos.deployment.check.businessAreas;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

public class BusinessAreasValidation {

	private static final int BUFFER_SIZE = 4096;
	private static Pattern fileExtnPtrn = Pattern.compile("([^\\s]+(\\.(?i)(zip))$)");

	public static void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}
	
	public static boolean isEmptyZip(String zipFilePath) throws IOException {
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		boolean isEmpty = entry == null ? true : false;
		zipIn.closeEntry();
		zipIn.close();
		return isEmpty;
	}
	
	public static int zipFileSize (String zipFilePath) throws IOException{
		int numberOfEntries = 0;
		numberOfEntries = new ZipFile(zipFilePath).size();
		return numberOfEntries;
		
	}

	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	public static boolean validateFileExtn(String filename) {

		Matcher mtch = fileExtnPtrn.matcher(filename);
		if (mtch.matches()) {
			return true;
		}
		return false;
	}

	public static boolean validateFileType(String filename, String patternString) {

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(filename);
		boolean found = false;
		while (matcher.find()) {
			found = true;
		}
		return found;
	}

	public static int validateJSON(String filename, String nodename) {

		JSONParser parser = new JSONParser();
		int jsonSize = 0;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(filename);
			Object obj = parser.parse(fileReader);
			JSONObject jsonObject = new JSONObject((Map) obj) ;
			JSONArray skus = (JSONArray) jsonObject.get(nodename);
			//System.out.println(skus.size());
			jsonSize = skus.size();
		} catch (IOException | ParseException e) {
			// TODO
			e.printStackTrace();
		} finally {
			if(Optional.ofNullable(fileReader).isPresent()) {
				try {
					fileReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return jsonSize;
	}
	
	public static void preparePriceFile (String filename){
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(new File(filename), "rw");
		    byte[] text = new byte[(int) file.length()];
		    file.readFully(text);
		    file.seek(0);
		    file.writeBytes("{\"prices\":\n");
		    file.write(text);
		    file.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Files.write(Paths.get(filename), "}".getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
