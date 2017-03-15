package com.marks.mpos.deployment.check.splunk;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;

import com.marks.mpos.deployment.check.UUID.UUIDGenerator;

public class CreateJSON {
	
	@SuppressWarnings("unchecked")
	public void generate(Number count, String countType, String filePath) throws IOException{
		JSONObject obj = new JSONObject();
		String uuid = UUIDGenerator.generate();
		obj.put("messageid", uuid);
		obj.put("messagetype", "COUNT");
		obj.put("errorcode", "");
		obj.put("count", count);
		obj.put("severity", "");
		obj.put("sourcesystem", "mPOS");
		obj.put("sourcejob", "Deploy Check Automation");
		obj.put("errormessage", "");
		obj.put("context", countType);
		obj.put("contextkeys", "");
		obj.put("remidiation", "");
		obj.put("retrycount", "");

 
		// try-with-resources statement based on post comment below :)
		try (FileWriter file = new FileWriter(filePath)) {
			file.write(obj.toJSONString());
			//System.out.println("Successfully Copied JSON Object to File...");
			//System.out.println("\nJSON Object: " + obj);
		}		
		prepareJSONFile(filePath);
		
	}
	
	public static void prepareJSONFile (String filename ){
		DateTimeZone zone = DateTimeZone.getDefault();
		DateTime checkDate = new DateTime().withZone(zone);
		//System.out.println(zone);
		//System.out.println((checkDate.toString().replace("T", " ")).replace(".000", " "));
		
		String fileDate = checkDate.toString().substring(0, 10) + " " + checkDate.toString().substring(11, 19)+ " " + checkDate.toString().substring(23);
		//System.out.println(fileDate);
		
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(new File(filename), "rw");
		    byte[] text = new byte[(int) file.length()];
		    file.readFully(text);
		    file.seek(0);
		    file.writeBytes(fileDate);
		    file.write(text);
		    file.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
