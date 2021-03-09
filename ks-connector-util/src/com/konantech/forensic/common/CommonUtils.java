package com.konantech.forensic.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {
	private static final Logger log = LoggerFactory.getLogger(CommonUtils.class);

	public static Map<String, String> getFileMetaSummaryMapFromString(String textExtracted) {
		Map<String, String> resultMap = new HashMap<String, String>();

		if (textExtracted != null && !textExtracted.equals("")) {
			String[] textLinesArr = textExtracted.split("\\R");
			String fieldName = "";
			String fieldValue = "";

			for (String textLine : textLinesArr) {
				if (textLine != null && !textLine.trim().equals("")) {
					int fieldValueOffset = 0;

					if (textLine.contains(":") && textLine.contains("[")
							&& textLine.indexOf(":") < textLine.indexOf("[") && !textLine.trim().startsWith(":")) {
						// 메타 필드 정의 시작문장인 경우에만
						// String[] wordsArr = textLine.split("[:]\\s*[\\]]");
						fieldName = textLine.substring(0, textLine.indexOf(":")).trim();
						fieldValueOffset = textLine.indexOf("[") + 1;
					}

					if (textLine.trim().endsWith("]")) { // 메타 필드값이 종료된 문장이면
						fieldValue = fieldValue + textLine.substring(fieldValueOffset, textLine.lastIndexOf("]"));

						fieldValue = normalizeMetaValue(fieldName, fieldValue); // 필드값 수정이 필요한 경우

						resultMap.put("FILEMETA_" + fieldName.toUpperCase(), fieldValue); // 볼륨 테이블 필드명으로 변경하여 저장
						// log.info("fieldName : " + fieldName + ", fieldValue" + fieldValue);
						fieldName = ""; // 필드 변수 초기화
						fieldValue = ""; // 필드 변수 초기화
					} else { // 메타 필드값이 아직 끝나지 않았다면
						fieldValue = fieldValue + textLine.substring(fieldValueOffset) + " ";
					}
				}
			}
		}
		return resultMap;
	}

	private static String normalizeMetaValue(String fieldName, String fieldValue) {
		if (fieldName.equalsIgnoreCase("CreateDTM") || fieldName.equalsIgnoreCase("SaveDTM")) {
			// 파일 포맷 간에 날짜 저장 형식이 다를 수 있지만 'YYYYMMDDHHmmss..' 순으로 되어 있다고 가정하고 숫자 외에는 모두
			// 제거한다.
			return fieldValue.replaceAll("[^\\d]", "");
		} else {
			return fieldValue;
		}
	}

	/**
	 * engine map[no-byte] byte sort
	 * 
	 * @param map
	 * @param parseValue
	 * @return
	 */
	public static synchronized String mapValueCompare(Map<String, Long> map, int parseValue) {
		List<Entry<String, Long>> list1 = new ArrayList<>(map.entrySet());
		list1.sort(Entry.comparingByValue());
		String tmp = list1.get(0).getKey();
		map.put(tmp, list1.get(0).getValue() + parseValue);
		return tmp;
	}

	public static String getDigestOfFile(Path path) throws IOException {
		String digestStr = "";
		if (Files.exists(path)) {
			InputStream is = null;
			try {
				is = Files.newInputStream(path);
				digestStr = DigestUtils.md5Hex(is); // "MD5"
			} catch (Exception e) {
				log.error("getDigestOfFile error, " + path.toString(), e);
			} finally {
				is.close();
			}
		}

		return digestStr;
	}

	public static String emlDateFormat(String datefm) {

		SimpleDateFormat formatter_one = new SimpleDateFormat("EEE,ddMMMyyyyhh:mm:ss", Locale.ENGLISH);
		SimpleDateFormat formatter_twe = new SimpleDateFormat("yyyy-MM-dd");

		String outString = "";

		if (datefm.indexOf("Mon,") > -1 || datefm.indexOf("Tue,") > -1 || datefm.indexOf("Wed,") > -1
				|| datefm.indexOf("Thu,") > -1 || datefm.indexOf("Fri,") > -1 || datefm.indexOf("Sat,") > -1
				|| datefm.indexOf("Sun") > -1) {

			formatter_one = new SimpleDateFormat("EEE,ddMMMyyyyhh:mm:ss", Locale.ENGLISH);
			formatter_twe = new SimpleDateFormat("yyyyMMdd");
			String inString = datefm;
			ParsePosition pos = new ParsePosition(0);

			Date frmTime = formatter_one.parse(inString, pos);
			outString = formatter_twe.format(frmTime);
			// System.out.println( " :outString : " + outString );
		} else if (datefm.indexOf("Jan") > -1 || datefm.indexOf("Feb") > -1 || datefm.indexOf("Mar") > -1
				|| datefm.indexOf("Apr") > -1 || datefm.indexOf("May") > -1 || datefm.indexOf("June") > -1
				|| datefm.indexOf("Jul") > -1 || datefm.indexOf("Aug") > -1 || datefm.indexOf("Sept") > -1
				|| datefm.indexOf("Oct") > -1 || datefm.indexOf("Nov") > -1 || datefm.indexOf("Dec") > -1) {

			formatter_one = new SimpleDateFormat("ddMMMyyyyhh:mm:ss", Locale.ENGLISH);
			formatter_twe = new SimpleDateFormat("yyyyMMdd");
			String inString = datefm;
			ParsePosition pos = new ParsePosition(0);

			Date frmTime = formatter_one.parse(inString, pos);
			outString = formatter_twe.format(frmTime);
			// System.out.println( " :outString : " + outString );
		} else {
			formatter_one = new SimpleDateFormat("yyyy-MM-dd");
			formatter_twe = new SimpleDateFormat("yyyyMMdd");
			String inString = datefm;
			ParsePosition pos = new ParsePosition(0);

			Date frmTime = formatter_one.parse(inString, pos);
			outString = formatter_twe.format(frmTime);
			// System.out.println( " :outString : " + outString );
		}

		return outString;
	}

	/* used for K-project */
	public static String removeTags(String htmlString) {
		if (htmlString == null || htmlString.length() == 0) {
			return htmlString;
		}
		String strUnEscapeHTML = htmlString.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
		strUnEscapeHTML = strUnEscapeHTML.replaceAll("<", "&lt;");
		strUnEscapeHTML = strUnEscapeHTML.replaceAll(">", "&gt;");

		return strUnEscapeHTML;
	}

	//이메일 주소만 추출하기 위한 용도
	public static String getOnlyEmailAddress(String addressesIncluingName){
		String returnStr = null;
		String regEx = "(?<=&lt;)(.*?)(?=&gt;)";
		Pattern pat = Pattern.compile(regEx);

		if (addressesIncluingName != null && !"".equals(addressesIncluingName)){
			String[] addressArr = addressesIncluingName.split(",");
			StringBuilder emlAddress = new StringBuilder();
			for (String addr : addressArr) {			//쉼표로 구분된 이메일 각각에 대해 
				if (addr.contains("&lt;") && addr.contains("&gt;")){	// case1 : name <account.host.co.kr>
					Matcher match = pat.matcher(addr);
					while (match.find()) {		//주소 리스트 만들기
						if (emlAddress != null && emlAddress.length() > 0) emlAddress.append("\n");
						emlAddress.append(match.group().trim());
					}
				} else {		// case2 : account.host.co.kr
					if (emlAddress != null && emlAddress.length() > 0) emlAddress.append("\n");
					emlAddress.append(addr.trim());
				}
			} 
			returnStr = emlAddress.toString();
		}
		return returnStr;
	}
}
