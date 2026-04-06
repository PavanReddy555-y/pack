package com.myne.ref.util;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
//import org.jsoup.Jsoup;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {

	public static String convertDateToString(Instant date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(date);
		return dateString;
	}

	public static String getISTDateTime() {
		ZoneId istZone = ZoneId.of("Asia/Kolkata");
		ZonedDateTime istDateTime = ZonedDateTime.now(istZone);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String formattedDateTime = istDateTime.format(formatter);
		return formattedDateTime;
	}

	public static LocalDateTime convertInstantToISTInstant() {
		ZoneId istZone = ZoneId.of("Asia/Kolkata");
		ZonedDateTime istDateTime = ZonedDateTime.now(istZone);
		Instant instant = Instant.ofEpochSecond(istDateTime.toEpochSecond(), istDateTime.getNano());
		ZonedDateTime convertedDateTime = instant.atZone(istZone);
		return convertedDateTime.toLocalDateTime();
	}

	public static Date convertStringToSqlDate(String dateString) {
		// Define the format of your input string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		// Parse the string to a LocalDate
		LocalDate localDate = LocalDate.parse(dateString, formatter);

		// Convert LocalDate to java.sql.Date
		return java.sql.Date.valueOf(localDate);
	}

	public static LocalDateTime convertInstantToISTInstant(String formattedDateStr) {
		ZoneId istZone = ZoneId.of("Asia/Kolkata");

		// Parse the formattedDateStr to LocalDate (assumed format is yyyy-MM-dd)
		LocalDate localDate = LocalDate.parse(formattedDateStr);

		// Convert the LocalDate to LocalDateTime at the start of the day (00:00)
		LocalDateTime localDateTime = localDate.atStartOfDay();

		// Convert LocalDateTime to ZonedDateTime in IST
		ZonedDateTime istDateTime = localDateTime.atZone(istZone);

		// Return the LocalDateTime part of the IST ZonedDateTime
		return istDateTime.toLocalDateTime();
	}

	public static String formatIndianCurrency(BigDecimal bigDecimal) {
		if (bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
			return "₹0.00";
		}

		// Convert BigDecimal to double for formatting
		String amountStr = String.format("%.2f", bigDecimal.doubleValue());
		String[] parts = amountStr.split("\\.");
		String integerPart = parts[0];
		String decimalPart = parts[1];

		StringBuilder sb = new StringBuilder();
		int length = integerPart.length();

		// Process the last three digits (thousands place)
		if (length > 3) {
			sb.append(integerPart.substring(length - 3));
			length -= 3;
		} else {
			sb.append(integerPart);
			return "₹" + sb.toString() + "." + decimalPart;
		}

		// Process the remaining digits, adding commas every two digits
		while (length > 0) {
			sb.insert(0, ",").insert(0, integerPart.substring(Math.max(length - 2, 0), length));
			length -= 2;
		}

		return "₹" + sb.toString() + "." + decimalPart;
	}

	public static String formatAmountFromDoubleToString(double amount) {
		// Check if the amount is zero
		if (amount == 0.0) {
			return "0.00";
		}

		// Convert the double to a formatted string with two decimal places
		String amountStr = String.format("%.2f", amount);
		String[] parts = amountStr.split("\\.");
		String integerPart = parts[0];
		String decimalPart = parts[1];

		StringBuilder sb = new StringBuilder();
		int length = integerPart.length();

		if (length > 3) {
			sb.append(integerPart.substring(length - 3));
			length -= 3;
		} else {
			sb.append(integerPart);
			return sb.toString() + "." + decimalPart;
		}

		while (length > 0) {
			sb.insert(0, ",").insert(0, integerPart.substring(Math.max(length - 2, 0), length));
			length -= 2;
		}

		return sb.toString() + "." + decimalPart;
	}

	public static final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
			"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen",
			"Nineteen" };

	public static final String[] tens = { "", // 0
			"", // 1
			"Twenty", // 2
			"Thirty", // 3
			"Forty", // 4
			"Fifty", // 5
			"Sixty", // 6
			"Seventy", // 7
			"Eighty", // 8
			"Ninety" // 9
	};

	public static String convert(final double number) {
		// Split number into integer and decimal parts
		long integerPart = (long) number;
//        int decimalPart = (int) ((number - integerPart) * 100); // Assuming two decimal places

		String integerInWords = convertIntegerPart(integerPart);

//        if (decimalPart > 0) {
//            return integerInWords + " Point " + convertIntegerPart(decimalPart);
//        }

		return integerInWords;
	}

	// Helper method to convert integer part of number
	private static String convertIntegerPart(final long n) {
		if (n < 0) {
//            return "Minus " + convertIntegerPart(-n);
			return "";
		}

		if (n < 20) {
			return units[(int) n];
		}

		if (n < 100) {
			return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " : "") + units[(int) (n % 10)];
		}

		if (n < 1000) {
			return units[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " " : "") + convertIntegerPart(n % 100);
		}

		if (n < 100000) {
			return convertIntegerPart(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "")
					+ convertIntegerPart(n % 1000);
		}

		if (n < 10000000) {
			return convertIntegerPart(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "")
					+ convertIntegerPart(n % 100000);
		}

		return convertIntegerPart(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "")
				+ convertIntegerPart(n % 10000000);
	}

	/*
	 * this will convert string with special characters to - like M/R Middle Nam.e
	 * to M_R_Middle_Nam.e
	 * 
	 */
//	public static String sanitizeString(String str) {
//		if (str == null) {
//			return "";
//		}
//
////		return str.replace(" ", "-").replace(".", "-").replaceAll("[/\\\\:*?\"<>|]", "-");
//
//		// Separate the extension (if present)
//		int lastDotIndex = str.lastIndexOf(".");
//		String namePart = (lastDotIndex != -1) ? str.substring(0, lastDotIndex) : str;
//		String extensionPart = (lastDotIndex != -1) ? str.substring(lastDotIndex) : "";
//
//		// Sanitize only the name part
//		namePart = namePart.replace(" ", "-").replace(".", "-").replaceAll("[/\\\\:*?\"<>|]", "-");
//
//		return namePart + extensionPart; // Reattach extension
//	}
//	

	public static String sanitizeString(String str) {
		if (str == null) {
			return "";
		}
		// 1️⃣ Remove trailing unwanted characters first (., space, -, etc.)
		str = str.replaceAll("[^A-Za-z0-9]+$", "");

		// 2️⃣ Separate the extension (if present)
		int lastDotIndex = str.lastIndexOf(".");
		String namePart = (lastDotIndex != -1) ? str.substring(0, lastDotIndex) : str;
		String extensionPart = (lastDotIndex != -1) ? str.substring(lastDotIndex) : "";

		// 3️⃣ Sanitize the name part only
		namePart = namePart.replace(" ", "-").replace(".", "-").replaceAll("[/\\\\:*?\"<>|]", "-");

		// 4️⃣ Combine and return the sanitized name + extension
		return namePart + extensionPart;
	}

	public static String safeString(Object value) {
		return value != null ? value.toString() : "N/A";
	}

	public static String formatDate(Date date) {
		if (date == null)
			return "N/A";
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(localDate);
	}

	public static String formatLocalDateTime(LocalDateTime dateTime) {
		if (dateTime == null)
			return "N/A";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		return formatter.format(dateTime);
	}

	public static String formatLocalDate(LocalDate date) {
		if (date == null)
			return "N/A";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		return formatter.format(date);
	}

	public static String formatIndianCurrencyWithoutRupeeSymbol(BigDecimal bigDecimal) {
		if (bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
			return "0.00";
		}

		// Convert BigDecimal to double for formatting
		String amountStr = String.format("%.2f", bigDecimal.doubleValue());
		String[] parts = amountStr.split("\\.");
		String integerPart = parts[0];
		String decimalPart = parts[1];

		StringBuilder sb = new StringBuilder();
		int length = integerPart.length();

		// Process the last three digits (thousands place)
		if (length > 3) {
			sb.append(integerPart.substring(length - 3));
			length -= 3;
		} else {
			sb.append(integerPart);
			return sb.toString() + "." + decimalPart;
		}

		// Process the remaining digits, adding commas every two digits
		while (length > 0) {
			sb.insert(0, ",").insert(0, integerPart.substring(Math.max(length - 2, 0), length));
			length -= 2;
		}

		return sb.toString() + "." + decimalPart;
	}

	public static String parseRichtextBoldTagToStrongTag(String subject) {
		if (subject == null || subject.trim().isEmpty()) {
			return "";
		}
		return subject.replaceAll("<strong>", "<b>").replaceAll("<strong", "<b").replaceAll("</strong>", "</b>");
	}

	public static String parseRichtextBoldTagToStrongTagWithListParsing(String subject) {
		if (subject == null || subject.trim().isEmpty()) {
			return "";
		}
		String nestedHtml = "";
//				convertQuillHtmlFixedOne(subject);
		// return nestedHtml.replaceAll("<strong>", "<b>").replaceAll("<strong",
		// "<b").replaceAll("</strong>", "</b>");
		nestedHtml = nestedHtml.replaceAll("<strong>", "<b>").replaceAll("<strong", "<b").replaceAll("</strong>",
				"</b>");

		// remove any inline font-family declarations
		nestedHtml = nestedHtml.replaceAll("font-family:[^;\"']*;?", "").replaceAll("font-size:[^;\"']*;?", "");

		return nestedHtml;
	}

//	public static String convertQuillHtmlFixedOne(String html) {
//
//		if (html == null || html.trim().isEmpty()) {
//			return "";
//		}
//
//		Document doc = Jsoup.parseBodyFragment(html);
//		Element body = doc.body();
//
////        Elements allLists = body.select("ol, ul");
//		Element newRoot = new Element("div"); // Dummy root for output
//
//		// Copy all body children to preserve <p><br></p>
//		for (Element child : body.children()) {
//			if (child.tagName().equals("ol") || child.tagName().equals("ul")) {
//				// Process lists
//				Element currentList = new Element(child.tagName());
//				newRoot.appendChild(currentList);
//				Stack<Element> stack = new Stack<>();
//				stack.push(currentList);
//
//				Elements listItems = child.select("> li"); // Direct children only
//				Element lastLi = null;
//				int lastIndent = -1;
//
//				for (Element li : listItems) {
//					String classAttr = li.className();
//					int indent = 0;
//					if (classAttr.startsWith("ql-indent-")) {
//						indent = Integer.parseInt(classAttr.replace("ql-indent-", ""));
//					}
//
//					// Adjust stack based on indentation level
//					while (stack.size() > indent + 1) {
//						stack.pop();
//					}
//
//					if (indent > lastIndent && lastLi != null) {
//						// Create a new nested list
//						Element nested = new Element(child.tagName());
//						if (child.tagName().equals("ol")) {
//							if (indent == 1)
//								nested.attr("type", "a");
//							else if (indent == 2)
//								nested.attr("type", "i");
//						}
//						lastLi.appendChild(nested);
//						stack.push(nested);
//					}
//
//					// Clone the list item and clean up &nbsp; and &amp;
//					Element clonedLi = new Element("li").html(li.html().replace("&nbsp;", "").replace("&amp;", "&"));
//					stack.peek().appendChild(clonedLi);
//
//					lastLi = clonedLi;
//					lastIndent = indent;
//				}
//			} else {
//				// Copy non-list elements (e.g., <p><br></p>) as-is
//				newRoot.appendChild(child.clone());
//			}
//		}
//
//		return newRoot.html();
//	}

	public static String generateFilterConditions(Map<String, Object> filters) {
		StringBuilder queryBuilder = new StringBuilder();
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			String key = entry.getKey();
			Object[] val = (Object[]) entry.getValue();
			String operator = (String) val[0];
			Object value = val[1];
			queryBuilder.append(" AND ").append(key).append(" ");

			switch (operator) {
			case CommonConstants.EQUALS:
				if (value instanceof Number) {
					queryBuilder.append("= ").append(value);
				} else {
					queryBuilder.append("= '").append(value).append("'");
				}
				break;

			case CommonConstants.NOT_EQUALS:
				if (value instanceof Number) {
					queryBuilder.append("!= ").append(value);
				} else {
					queryBuilder.append("!= '").append(value).append("'");
				}
				break;

			case CommonConstants.LIKE:
				queryBuilder.append("LIKE '%").append(value).append("%'");
				break;

			case CommonConstants.IN:
				if (value instanceof List) {
					queryBuilder.append("IN (")
							.append(((List<?>) value).stream()
									.map(v -> (v instanceof Number) ? v.toString() : "'" + v + "'")
									.collect(Collectors.joining(", ")))
							.append(")");
				} else {
					if (value instanceof Number) {
						queryBuilder.append("IN (").append(value).append(")");
					} else {
						queryBuilder.append("IN ('").append(value).append("')");
					}
				}
				break;
			case CommonConstants.NOT_IN:
				if (value instanceof List) {
					queryBuilder.append("NOT IN (")
							.append(((List<?>) value).stream()
									.map(v -> (v instanceof Number) ? v.toString() : "'" + v + "'")
									.collect(Collectors.joining(", ")))
							.append(")");
				} else {
					if (value instanceof Number) {
						queryBuilder.append("NOT IN (").append(value).append(")");
					} else {
						queryBuilder.append("NOT IN ('").append(value).append("')");
					}
				}
				break;
			case CommonConstants.BETWEEN:
				if (val.length == 3) {
					Object start = val[1];
					Object end = val[2];

					queryBuilder.append("BETWEEN ");

					if (start instanceof Number && end instanceof Number) {
						queryBuilder.append(start).append(" AND ").append(end);
					} else {
						queryBuilder.append("'").append(start).append("'").append(" AND ").append("'").append(end)
								.append("'");
					}
				} else {
					throw new IllegalArgumentException("BETWEEN requires exactly 2 values for key: " + key);
				}
				break;

			default:
				queryBuilder.append(operator).append(" ");
				if (value instanceof Number) {
					queryBuilder.append(value);
				} else {
					queryBuilder.append("'").append(value).append("'");
				}
				break;
			}

		}
		return queryBuilder.toString();
	}

	public static String getPdfendTimeStampFormat() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String timestamp = LocalDateTime.now().format(formatter);
		timestamp = timestamp.trim().replace(" ", "").replace(":", "").replace("_", "").replace("-", "");
		return timestamp;
	}

	public static String loadTemplate(ResourceLoader resourceLoader, String filename) throws java.io.IOException {
		try {
			log.debug("Loading email template: {}", filename);
			Resource resource = resourceLoader.getResource("classpath:templates/" + filename);
			try (InputStream inputStream = resource.getInputStream()) {
				return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			log.error("Failed to load email template '{}': {}", filename, e.getMessage(), e);
			throw new RuntimeException("Unable to load email template: " + filename, e);
		}
	}

	public static String populateTemplate(String template, Map<String, String> values) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			template = template.replace("${" + entry.getKey() + "}", entry.getValue());
		}
		return template;
	}

	@SuppressWarnings("unused")
	public static Map<String, Object> calculateDateRange(Integer rangeOfDays, LocalDateTime customStartDate,
			LocalDateTime customEndDate) {
		LocalDateTime today = LocalDateTime.now();

		Map<String, Object> map = new LinkedHashMap<>();
		if (ObjectUtils.isNotEmpty(customStartDate) && ObjectUtils.isNotEmpty(customEndDate)) {
			log.info("Fetching data for custom start date {} and end date {}", customStartDate, customEndDate);
			map.put("startDate", customStartDate);
			map.put("endDate", customEndDate);

		} else {
			if (rangeOfDays == null || rangeOfDays == 0) {
				LocalDateTime startOfDay = today.with(LocalTime.MIN);
				LocalDateTime endOfDay = today.with(LocalTime.of(23, 59, 59, 999_000_000));
				log.info("Fetching data for today between start {} and end {}", startOfDay, endOfDay);
				map.put("startDate", startOfDay);
				map.put("endDate", endOfDay);

			} else if (rangeOfDays == 90) {
				LocalDateTime endDate = today.with(LocalTime.of(23, 59, 59, 999_000_000));
				LocalDateTime startDate = endDate.minusDays(90).with(LocalTime.MIN);
				log.info("Fetching data for the last 90 days from start {} and end {}", startDate, endDate);
				map.put("startDate", startDate);
				map.put("endDate", endDate);
			} else if (rangeOfDays == 30) {
				LocalDateTime firstDayOfPreviousMonth = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
						.with(LocalTime.MIN);
				LocalDateTime lastDayOfPreviousMonth = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
						.with(LocalTime.MAX);

				log.info("Fetching data for month from start {} and end {} ", firstDayOfPreviousMonth,
						lastDayOfPreviousMonth.with(LocalTime.of(23, 59, 59, 999_000_000)));
				map.put("startDate", firstDayOfPreviousMonth);
				map.put("endDate", lastDayOfPreviousMonth.with(LocalTime.of(23, 59, 59, 999_000_000)));
			}
			// Last 30 days
			else if (rangeOfDays == 130) {
				LocalDateTime startDay = today.minusDays(29).with(LocalTime.MIN); // 30 days including today
				LocalDateTime endDay = today.with(LocalTime.of(23, 59, 59, 999_000_000));

				log.info("Fetching leads for the last 30 days between start {} and end {}", startDay, endDay);

				map.put("startDate", startDay);
				map.put("endDate", endDay);
			}
			// This month
			else if (rangeOfDays == 13) {
				LocalDateTime startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
				LocalDateTime endOfToday = today.with(TemporalAdjusters.lastDayOfMonth())
						.with(LocalTime.of(23, 59, 59, 999_000_000));

				log.info("Fetching leads for this month from {} to {}", startOfMonth, endOfToday);

				map.put("startDate", startOfMonth);
				map.put("endDate", endOfToday);
			}

			else if (rangeOfDays == 999) {

			} else {

				LocalDateTime startDate = today;
				LocalDateTime endDate = today;
				if (rangeOfDays > 0) {
					if (rangeOfDays == 1) {
						startDate = today.minusDays(rangeOfDays);
						endDate = startDate.with(LocalTime.of(23, 59, 59, 999_000_000));
					} else {
						startDate = today.minusDays(rangeOfDays);
						endDate = today;
					}
				} else {
					startDate = today.plusDays(1);
					endDate = today.minusDays(rangeOfDays);
				}
				LocalDateTime startDay = startDate.with(LocalTime.MIN);
				LocalDateTime endDay = endDate.with(LocalTime.of(23, 59, 59, 999_000_000));
				log.info("Fetching leads for the last {} days between start {} and end {}", rangeOfDays, startDay,
						endDay);
				log.info("Fetching data for the last {} days between start {} and end {}", rangeOfDays, startDay,
						endDay);
				map.put("startDate", startDay);
				map.put("endDate", endDay);
			}
		}
		return map;
	}

	public static String formatIndianCurrencyWithoutRupeeSymbol(Double value) {

		if (value == null || value == 0) {
			return "0.00";
		}

		// Format to 2 decimal places
		String amountStr = String.format("%.2f", value);
		String[] parts = amountStr.split("\\.");
		String integerPart = parts[0];
		String decimalPart = parts[1];

		StringBuilder sb = new StringBuilder();
		int length = integerPart.length();

		// Process last three digits
		if (length > 3) {
			sb.append(integerPart.substring(length - 3));
			length -= 3;
		} else {
			sb.append(integerPart);
			return sb.toString() + "." + decimalPart;
		}

		// Add commas for every 2 digits before thousands
		while (length > 0) {
			sb.insert(0, ",").insert(0, integerPart.substring(Math.max(length - 2, 0), length));
			length -= 2;
		}

		return sb.toString() + "." + decimalPart;
	}

}

