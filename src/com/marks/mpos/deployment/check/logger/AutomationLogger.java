package com.marks.mpos.deployment.check.logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;

public class AutomationLogger {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;

    static public void setup() throws IOException {
    	boolean error = false;
    	int logging_txt_number = 0;
    	do {
	    	try {
	            // get the global logger to configure it
	            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	            // suppress the logging output to the console
	            Logger rootLogger = Logger.getLogger("");
	            Handler[] handlers = rootLogger.getHandlers();
	            if (handlers[0] instanceof ConsoleHandler) {
	                //rootLogger.removeHandler(handlers[0]);
	            }
	
	            logger.setLevel(Level.INFO);
	            
				String txtNumber = String.valueOf(logging_txt_number++);
				if (txtNumber.equalsIgnoreCase("0")) {
					txtNumber = StringUtils.EMPTY;
				}
				fileTxt = new FileHandler("Logging" + txtNumber + ".txt");
	
	            // create a TXT formatter
	            formatterTxt = new SimpleFormatter();
	            fileTxt.setFormatter(formatterTxt);
	            logger.addHandler(fileTxt);
	            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.INFO);
	            error = false;
	    	} catch(Exception ex) {
	    		error = true;
	    		ex.printStackTrace();
	    	}
    	} while (error != false );
    }
}
