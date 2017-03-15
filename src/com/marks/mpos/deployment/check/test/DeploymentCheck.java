package com.marks.mpos.deployment.check.test;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import com.marks.mpos.deployment.check.beans.StoreReport;
import com.marks.mpos.deployment.check.logger.AutomationLogger;
import com.marks.mpos.deployment.check.properties.UserProperties;
import com.marks.mpos.deployment.check.report.StoreDataBuilder;
import com.marks.mpos.deployment.check.report.StoreReportGenerator;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import com.marks.mpos.deployment.check.mail.EmailSender;

public class DeploymentCheck {
	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main(String[] args) {
		try {
			AutomationLogger.setup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LocalDateTime startingTime = LocalDateTime.now();
		LOG.info("Starting Time " +startingTime);
		StringBuilder builder = new StringBuilder();
		String givenDate = null;
		for (String param : args) {
			builder.append(param + " ");
			givenDate = param;
		}
		LOG.info(builder.toString());
		
		Optional<Properties> prop = getPropertyFile();
		if (prop.isPresent()) {
			String date = prop.get().getProperty("checkDate");
			if (date != null) {
				givenDate = date;
			}
			LOG.info("Found transaction date in property file " + givenDate);
			String detailedReport = prop.get().getProperty("detailedReport");
			if (detailedReport != null) {
				UserProperties.DETAILED_REPORT = Boolean.valueOf(detailedReport);
			}
			LOG.info("DetailedReport is set as  " + UserProperties.DETAILED_REPORT);
			String sendMail = prop.get().getProperty("sendEmail");
			if (sendMail != null) {
				UserProperties.SEND_EMAIL = Boolean.valueOf(sendMail);
			}
			LOG.info("SendEmail is set as  " + UserProperties.SEND_EMAIL);
			String transactionReport = prop.get().getProperty("transactionReport");
			if (transactionReport != null) {
				UserProperties.IS_TRANSACTION_CHECK_ENABLED = Boolean.valueOf(transactionReport);
			}
			LOG.info("TransactionCheck is set as  " + UserProperties.IS_TRANSACTION_CHECK_ENABLED);
			String productCheck = prop.get().getProperty("productCheck");
			if (productCheck != null) {
				UserProperties.IS_PRODUCT_CHECK_ENABLED = Boolean.valueOf(productCheck);
				String products = prop.get().getProperty("products");
				if (products != null) {
					UserProperties.setProducts(products);
				}
			}
			LOG.info("Product event check is set as  " + UserProperties.IS_PRODUCT_CHECK_ENABLED);
			String priceEventCheck = prop.get().getProperty("priceEventCheck");
			if (priceEventCheck != null) {
				UserProperties.IS_PRICE_EVENT_CHECK_ENABLED = Boolean.valueOf(priceEventCheck);
				String priceEvents = prop.get().getProperty("priceEvents");
				if (priceEvents != null) {
					UserProperties.setPriceEvents(priceEvents);
				}
			}
			LOG.info("Price event check is set as  " + UserProperties.IS_PRICE_EVENT_CHECK_ENABLED);
			String skipStores = prop.get().getProperty("skipStores");
			if (skipStores != null) {
				UserProperties.SKIP_STORES_ENABLED = Boolean.valueOf(skipStores);
				String requiredStores = prop.get().getProperty("requiredStores");
				if (requiredStores != null) {
					UserProperties.initRequiredStores(requiredStores);
					LOG.info("Required stores are  " + requiredStores);
				}
			}
			LOG.info("Skip stores is set as  " + UserProperties.SKIP_STORES_ENABLED);
			String cssHealthCheckOnly = prop.get().getProperty("cssHealthCheckOnly");
			if (cssHealthCheckOnly != null) {
				UserProperties.CSS_HEALTH_CHECK_ONLY_ENABLED = Boolean.valueOf(cssHealthCheckOnly);
			}
			LOG.info("Check only CSS health is set as  " + UserProperties.CSS_HEALTH_CHECK_ONLY_ENABLED);
		}
		
		if (UserProperties.CSS_HEALTH_CHECK_ONLY_ENABLED) {
			StoreDataBuilder.showCssHealthDetails();
		} else {
			UserProperties.setPostCheckDate(givenDate);
			LOG.info("PriceEvent EffectiveDate is set as  " + UserProperties.PRICE_EVENT_EFFECTIVE_DATE);
			List<StoreReport> resultReportList = StoreDataBuilder.generateStoreDataObjectList();
			if (UserProperties.IS_TRANSACTION_CHECK_ENABLED) {
				StoreReportGenerator.generateReportsForStores(resultReportList);
			}

			if (UserProperties.SEND_EMAIL && prop.isPresent()) {
				String user = prop.get().getProperty("userName");
				String pass = prop.get().getProperty("password");
				String sender = prop.get().getProperty("senderName");
				String[] to = prop.get().getProperty("emailTo").split(",");
				if (EmailSender.sendEmail(user, pass, sender, to, givenDate)) {
					LOG.info("mail sent successfully");
				} else {
					LOG.info("some error while sending mail");
				}
			}
		}
		
		LocalDateTime endingTime = LocalDateTime.now();
		LOG.info("Finshing Time " + endingTime);
		Duration elapsedTime = Duration.between(startingTime, endingTime);
		StringBuilder formatedTime = new StringBuilder();
		formatedTime.append(String.format("Elapsed time HH:MM:SS --> %s:%s:%s", elapsedTime.toHours(),
				(elapsedTime.toMinutes()%60), elapsedTime.getSeconds() % 60));
		LOG.info(formatedTime.toString());
	}

	private static Optional<Properties> getPropertyFile() {
		InputStream input = null;
		try {
			Properties prop = new Properties();
			input = new FileInputStream("./config.properties");
			prop.load(input);
			//System.setProperties(prop);
			input.close();
			return Optional.of(prop);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("File './config.properties' is missing in Specified location ");
		}
		return Optional.empty();
	}
}
