package com.marks.mpos.deployment.check.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import com.marks.mpos.deployment.check.properties.UserProperties;

public class DateUtil {

	public static String getTransactionDate() {
		return formattedString(LocalDate.now());// .minusDays(1);
	}

	public static String getFormattedDate() {
		if (StringUtils.EMPTY.equals(UserProperties.GIVEN_DATE)) {
			LocalDate transactionTime = LocalDate.now();// .minusDays(1);
			String year = String.valueOf(transactionTime.getYear());
			String month = String.valueOf(transactionTime.getMonthValue());
			String day = String.valueOf(transactionTime.getDayOfMonth());
			if (day.length() == 1) {
				day = "0" + day;
			}
			if (month.length() == 1) {
				month = "0" + month;
			}
			return String.format("%s/%s/%s", month, day, year);
		} else {
			return UserProperties.GIVEN_DATE;
		}
	}

	private static String formattedString(LocalDate transactionTime) {
		String year = String.valueOf(transactionTime.getYear());
		String month = String.valueOf(transactionTime.getMonthValue());
		String day = String.valueOf(transactionTime.getDayOfMonth());
		if (day.length() == 1) {
			day = "0" + day;
		}
		if (month.length() == 1) {
			month = "0" + month;
		}
		String currentDate = String.format("%s%s%s", year, month, day);
		
		return currentDate;
	}

	public static String getFormattedDate(String givenDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		return formattedString(LocalDate.parse(givenDate, formatter));
	}
	
	public static String getFormattedDate(String date, String formatterType) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate givenDate= LocalDate.parse(date, inputFormatter);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(formatterType);
		return givenDate.format(outputFormatter);
	}
}
