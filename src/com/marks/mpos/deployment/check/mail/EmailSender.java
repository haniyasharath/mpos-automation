package com.marks.mpos.deployment.check.mail;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.DEPLOY_CHECK_DIR;
import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.POST_CHECK_DIR;
import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.USER_HOME;
import static com.marks.mpos.deployment.check.properties.UserProperties.POST_CHECK_DATE;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

import com.marks.mpos.deployment.check.beans.CssHealth;
import com.marks.mpos.deployment.check.calculator.DataCalculator;

public class EmailSender {
	public static boolean sendEmail(String from, String password, String sender, String to[], String givenDate) {
		String host = "outlook.office365.com";
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", from);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", 587);
		props.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage mimeMessage = new MimeMessage(session);

		Optional<CssHealth> cssHealthChk = new DataCalculator().getCSSHealth();
		CssHealth cssHealth = cssHealthChk.orElse(new CssHealth());
		int queueDepth = cssHealth.getUnSentQueueDepth();
		String cssNode1 = cssHealth.getNode1();
		String cssNode2 = cssHealth.getNode1();
		String cssNode3 = cssHealth.getNode1();
		String cssNode4 = cssHealth.getNode1();
		String body = "Hi All" + System.lineSeparator() + System.lineSeparator() + "PFA Transaction Report "
				+ System.lineSeparator() + System.lineSeparator() + "Queue depth for CSS is : " + queueDepth
				+ System.lineSeparator() + System.lineSeparator() + " Css Node1 Health " + cssNode1
				+ System.lineSeparator() + System.lineSeparator() + " Css Node2 Health" + cssNode2
				+ System.lineSeparator() + System.lineSeparator() + " Css Node3 Health" + cssNode3
				+ System.lineSeparator() + System.lineSeparator() + " Css Node4 Health" + cssNode4
				+ System.lineSeparator() + System.lineSeparator() + "Thanks,"
				+ System.lineSeparator() + sender;
		try {
			mimeMessage.setFrom(new InternetAddress(from));
			InternetAddress[] toAddress = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}
			for (int i = 0; i < toAddress.length; i++) {
				mimeMessage.addRecipient(RecipientType.TO, toAddress[i]);
			}
			mimeMessage.setSubject(" mPOS Hourly Transaction Report for " + givenDate);
			mimeMessage.setText("messge");
			Transport transport = session.getTransport("smtp");
			transport.connect(host, from, password);
			Message message = new MimeMessage(session);
			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Now set the actual message
			messageBodyPart.setText(body);

			// Create a multipar message
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);
			final String FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
					+ File.separator + POST_CHECK_DATE;
			String filename = FILE_PATH + File.separator + "Transaction_details" + ".xlsx";
			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			// String filename =
			// "C:\\Users\\$manjunadh\\deploy_check\\post_deploy\\20161128\\Transaction_details.xlsx";
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName("Transaction_details_" + givenDate + ".xlsx");
			multipart.addBodyPart(messageBodyPart);

			// Send the complete message parts

			mimeMessage.setContent(multipart);
			transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
			transport.close();
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}
}
