package com.marks.mpos.deployment.check.businessAreas;

import static com.marks.mpos.deployment.check.properties.UserProperties.POST_CHECK_DATE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GJChronology;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.marks.mpos.deployment.check.beans.BusinessAreaCounts;

public class BusinessAreasDownloader {
	
	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	static String itemsPatternString = "items_ID";
	static String pricePatternString = "prices_ID";
	static String bunitsPatternString = "bunits_ID";
	static String priceAdvPatternString = "promotions_ID";
	static String taxesPatternString = "taxes_ID";
	static String hierarchyPatternString = "hierarchy_ID";
	static String promosPatternString = "promos";
	static String exemptionsPatternString = "tax_exceptions_ID";

	public BusinessAreaCounts downloadFiles(Session sshSession, String dl_file_path, String remote_folder,
			String foldertype, BusinessAreaCounts businessAreaCounts) {
		Channel channel = null;
		int itemRecords = 0;
		int priceRecords = 0;
		int bunitRecords = 0;
		int hierarchyRecords = 0;
		int promoRecords = 0;
		int numberOfPriceFiles = 0;
		int numberOfPriceAdvFiles = 0;
		int numberOfTaxFiles = 0;
		int numberOfPromoFiles = 0;
		int numberOfTaxEexemptions = 0;

		try {
			channel = sshSession.openChannel("sftp");
			if (!channel.isConnected()) {
				channel.connect();
			}
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.cd(remote_folder);
			Set<String> fileList;
			if(new File(dl_file_path).listFiles().length == 0) {
				fileList = getLBOFiles(sftpChannel, remote_folder);
			} else {
				fileList = new HashSet<String>();
				File[] listOfFiles = new File(dl_file_path).listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					fileList.add(listOfFiles[i].getName());
				}
			}
			for (String filename : fileList) {
				if (BusinessAreasValidation.validateFileExtn(filename)) {
					/*OutputStream output = new FileOutputStream(dl_file_path + File.separator + filename);
					sftpChannel.get(filename, output);
					output.close();*/
					if (foldertype.equalsIgnoreCase("POS")) {
						if (BusinessAreasValidation.validateFileType(filename, itemsPatternString)) {
							File mFile = new File(dl_file_path + File.separator + "items.json");
							BusinessAreasValidation.unzip(dl_file_path + File.separator + filename, dl_file_path);
							if (mFile.exists() && !mFile.isDirectory()) {
								itemRecords = BusinessAreasValidation.validateJSON(
										dl_file_path + File.separator + "items.json", "skus") + itemRecords;
								deleteFile(mFile);
							}

						}
						if (BusinessAreasValidation.validateFileType(filename, bunitsPatternString)) {
							File mFile = new File(dl_file_path + File.separator + "bunits.json");
							BusinessAreasValidation.unzip(dl_file_path + File.separator + filename, dl_file_path);
							if (mFile.exists() && !mFile.isDirectory()) {
								bunitRecords = BusinessAreasValidation.validateJSON(
										dl_file_path + File.separator + "bunits.json", "stores") + bunitRecords;
								deleteFile(mFile);
							}

						}
						if (BusinessAreasValidation.validateFileType(filename, hierarchyPatternString)) {
							File mFile = new File(dl_file_path + File.separator + "hierarchy.json");
							BusinessAreasValidation.unzip(dl_file_path + File.separator + filename, dl_file_path);
							if (mFile.exists() && !mFile.isDirectory()) {
								hierarchyRecords = BusinessAreasValidation.validateJSON(
										dl_file_path + File.separator + "hierarchy.json", "categories") + hierarchyRecords;
								deleteFile(mFile);
							}

						}						
					}
					if (foldertype.equalsIgnoreCase("PE")) {
						if (BusinessAreasValidation.validateFileType(filename, pricePatternString)) {
							numberOfPriceFiles = numberOfPriceFiles + 1;
							File mFile = new File(dl_file_path + File.separator + "prices.json");
							BusinessAreasValidation.unzip(dl_file_path + File.separator + filename, dl_file_path);
							BusinessAreasValidation.preparePriceFile(dl_file_path + File.separator + "prices.json");
							if (mFile.exists() && !mFile.isDirectory()) {
								priceRecords = BusinessAreasValidation.validateJSON(
										dl_file_path + File.separator + "prices.json", "prices") + priceRecords;
								deleteFile(mFile);
							}
						}
						if (BusinessAreasValidation.validateFileType(filename, priceAdvPatternString)
								&& !BusinessAreasValidation.isEmptyZip(dl_file_path + File.separator + filename)) {
							numberOfPriceAdvFiles = numberOfPriceAdvFiles + 1;
						}	
						if (BusinessAreasValidation.validateFileType(filename, taxesPatternString)) {
							numberOfTaxFiles = numberOfTaxFiles + 1;
						}
						if (BusinessAreasValidation.validateFileType(filename, exemptionsPatternString)) {
							numberOfTaxEexemptions = numberOfTaxEexemptions + 1;
						}						
						if (BusinessAreasValidation.validateFileType(filename, promosPatternString)) {
							numberOfPromoFiles = numberOfPromoFiles + 1;
							promoRecords = BusinessAreasValidation.zipFileSize(dl_file_path + File.separator + filename) + promoRecords;
						}
					}
				}
			}
		} catch (JSchException | SftpException | IOException e) {
			System.out.println(String.format("Exception while connecting to DTE/LBO --> {File Path:%s} \n {Remote folder:%s}", dl_file_path, remote_folder));
			System.out.println(e.getMessage());
		} finally {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
		}

		if (foldertype.equalsIgnoreCase("POS")) {
			System.out.println("Products Processed: " + itemRecords);
			businessAreaCounts.setProductCount(itemRecords);
			
			System.out.println("Business Units Processed: " + bunitRecords);
			businessAreaCounts.setBuCount(bunitRecords);
			
			System.out.println("Hierarchy Processed: " + hierarchyRecords);
			businessAreaCounts.setHierarchyCount(hierarchyRecords);
			
		} else if (foldertype.equalsIgnoreCase("PE")) {
			System.out.println("Price Files Processed: " + priceRecords);
			businessAreaCounts.setPriceCount(priceRecords);
			
			System.out.println("Price Advanced Files Processed: " + numberOfPriceAdvFiles);
			businessAreaCounts.setPriceAdvCount(numberOfPriceAdvFiles);
			
			System.out.println("Tax Files Processed: " + numberOfTaxFiles);
			businessAreaCounts.setTaxRatesCount(numberOfTaxFiles);
			
			System.out.println("Tax Exemptions Processed: " + numberOfTaxEexemptions);
			businessAreaCounts.setTaxExemptionCount(numberOfTaxEexemptions);
			
			System.out.println("Promos Processed: " + promoRecords);
			businessAreaCounts.setPromoCount(promoRecords);
		}
		return businessAreaCounts;
	}

	private void deleteFile(File mFile) {
		mFile.delete();
	}

	public Set<String> getLBOFiles(ChannelSftp sftpChannel, String remote_folder) throws SftpException {
		return getLBOFiles(Pattern.compile(".*"), sftpChannel, remote_folder);
	}

	public Set<String> getLBOFiles(Pattern pattern, ChannelSftp sftpChannel, String remote_folder)
			throws SftpException {
		// long since = new DateTime().minusDays(days).getMillis() / 1000;
		DateTimeZone zone = DateTimeZone.forID("MST");
		Chronology gregorianJuian = GJChronology.getInstance(zone);
		DateTime checkDate = new DateTime(Integer.parseInt(POST_CHECK_DATE.substring(0, 4)),
				Integer.parseInt(POST_CHECK_DATE.substring(4, 6)), Integer.parseInt(POST_CHECK_DATE.substring(6, 8)),
				12, 0, 0, 0);
		long dateFrom = checkDate.getMillis() / 1000;
		long dateTo = checkDate.plus(new Duration(24L * 60L * 60L * 1000L)).getMillis() / 1000;

		Set<String> files = new HashSet<String>();
		for (Object obj : sftpChannel.ls(remote_folder)) {
			if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
				ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
				// System.out.println(entry.getAttrs().getAtimeString());
				if (entry.getAttrs().getMTime() >= dateFrom && entry.getAttrs().getMTime() <= dateTo) {
					// System.out.println(entry.getAttrs().getAtimeString());
					if (pattern.matcher(entry.getFilename()).matches()) {
						files.add(entry.getFilename());
					}
				}
			}
		}
		return files;
	}

}
