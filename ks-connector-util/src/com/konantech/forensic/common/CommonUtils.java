package com.konantech.forensic.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
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

	// 이메일 주소만 추출하기 위한 용도
	public static String getOnlyEmailAddress(String addressesIncluingName) {
		String returnStr = null;
		String regEx = "(?<=&lt;)(.*?)(?=&gt;)";
		Pattern pat = Pattern.compile(regEx);

		if (addressesIncluingName != null && !"".equals(addressesIncluingName)) {
			String[] addressArr = addressesIncluingName.split(",");
			StringBuilder emlAddress = new StringBuilder();
			for (String addr : addressArr) { // 쉼표로 구분된 이메일 각각에 대해
				if (addr.contains("&lt;") && addr.contains("&gt;")) { // case1 : name <account.host.co.kr>
					Matcher match = pat.matcher(addr);
					while (match.find()) { // 주소 리스트 만들기
						if (emlAddress != null && emlAddress.length() > 0)
							emlAddress.append("\n");
						emlAddress.append(match.group().trim());
					}
				} else { // case2 : account.host.co.kr
					if (emlAddress != null && emlAddress.length() > 0)
						emlAddress.append("\n");
					emlAddress.append(addr.trim());
				}
			}
			returnStr = emlAddress.toString();
		}
		return returnStr;
	}

	public static Map<String, ArrayList<String>> setTextFilterFormatInfo(List<Map> textFilterFormatInfoList) {
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		if (textFilterFormatInfoList != null && textFilterFormatInfoList.size() > 0) {

			for (Map formatInfoMap : textFilterFormatInfoList) {
				if (formatInfoMap != null && formatInfoMap.size() > 0) {
					ArrayList<String> formatInfo = new ArrayList<String>();
					formatInfo.add((String) formatInfoMap.get("FILE_FORMAT_GROUP"));
					formatInfo.add((String) formatInfoMap.get("FILE_FORMAT_EXTENSION"));
					// 이메일검색 포맷 그룹화 용도
					if (formatInfoMap.get("FILE_FORMAT_GROUP_EMAIL") != null)
						formatInfo.add((String) formatInfoMap.get("FILE_FORMAT_GROUP_EMAIL"));
					// 화면 출력 포맷 그룹화 용도
					if (formatInfoMap.get("FILE_FORMAT_GROUP_DISPLAY") != null)
						formatInfo.add((String) formatInfoMap.get("FILE_FORMAT_GROUP_DISPLAY"));
					// 파일명의 확장자 변조 검증 대상 여부 용도
					if (formatInfoMap.get("DETECT_EXTENSION_FORGED") != null)
						formatInfo.add((String) formatInfoMap.get("DETECT_EXTENSION_FORGED"));
					// 다운로드 가능 여부 용도
					if (formatInfoMap.get("DOWNLOADABLE") != null)
						formatInfo.add((String) formatInfoMap.get("DOWNLOADABLE"));

					map.put((String) formatInfoMap.get("FILEMETA_FORMAT"), formatInfo);
				}
			}

			if (map != null)
				log.info("setTextFilterFormatInfo completed, " + map.size() + " formats.");

		}
		return map;
	}

	// 메타정보로부터 식별된 포맷에 따른 그룹, 확장자 변조 여부 확인 - String 타입
	public static Map<String, String> setFileFormatInfo(Map<String, ArrayList<String>> formatInfoMap, String filepath, String fileMetaFormat) {
		Map<String, String> resultMap = new HashMap<String, String>();

		String fileNameExtension = null;
		String fileFormatExtension = null;

		try {
			if (filepath != null) {
				fileNameExtension = FilenameUtils.getExtension(filepath).toLowerCase();
				resultMap.put("FILENAME_EXTENSION", fileNameExtension);
			}

			if (fileMetaFormat != null && !"".equals(fileMetaFormat)) {
				// 메타정보로부터 식별된 포맷에 따른 확장자 및 그룹 확인
				ArrayList<String> formatInfo = formatInfoMap.get(fileMetaFormat);
				if (formatInfo != null && formatInfo.size() >= 2) {
					resultMap.put("FILE_FORMAT_GROUP", formatInfo.get(0));
					fileFormatExtension = formatInfo.get(1).toLowerCase();
					resultMap.put("FILE_FORMAT_EXTENSION", fileFormatExtension);

					if (formatInfo.size() >= 3) {
						resultMap.put("FILE_FORMAT_GROUP_EMAIL", formatInfo.get(2));
					}
					if (formatInfo.size() >= 4) {
						resultMap.put("FILE_FORMAT_GROUP_DISPLAY", formatInfo.get(3));
					}
					if (formatInfo.size() >= 5) {
						resultMap.put("DETECT_EXTENSION_FORGED", formatInfo.get(4));
					}
					if (formatInfo.size() >= 6) {
						resultMap.put("DOWNLOADABLE", formatInfo.get(5));
					}
				}
			}

			if (resultMap.get("FILE_FORMAT_GROUP") == null || "".equals(resultMap.get("FILE_FORMAT_GROUP"))) {
				// FILE_FORMAT_GROUP 지정되지 않은 파일에 값 지정
				resultMap.put("FILE_FORMAT_GROUP", Const.FILE_FORMAT_GROUP_ETC);
			}
			if (resultMap.get("FILE_FORMAT_GROUP_EMAIL") == null
					|| "".equals(resultMap.get("FILE_FORMAT_GROUP_EMAIL"))) {
				// FILE_FORMAT_GROUP_EMAIL 지정되지 않은 파일에 값 지정
				resultMap.put("FILE_FORMAT_GROUP_EMAIL", Const.FILE_FORMAT_GROUP_ETC);
			}
			if (resultMap.get("FILE_FORMAT_GROUP_DISPLAY") == null
					|| "".equals(resultMap.get("FILE_FORMAT_GROUP_DISPLAY"))) {
				// FILE_FORMAT_GROUP_DISPLAY 지정되지 않은 파일에 값 지정
				resultMap.put("FILE_FORMAT_GROUP_DISPLAY", Const.FILE_FORMAT_GROUP_ETC);
			}

			if (fileNameExtension != null && fileFormatExtension != null) {
				// 확장자 변조 검증 대상인지 & 메타정보로부터 식별된 포맷 확장자 변조 여부 확인
				if (!"Y".equalsIgnoreCase(resultMap.get("DETECT_EXTENSION_FORGED"))
						|| Arrays.asList(fileFormatExtension.split("\\s*,\\s*")).contains(fileNameExtension)) {
					resultMap.put("FILENAME_EXTENSION_FORGED", "N");
				} else {
					resultMap.put("FILENAME_EXTENSION_FORGED", "Y");
				}
			}

		} catch (Exception e) {
			log.error("setFileFormatInfo error.", e);
		}

		return resultMap;
	}
}
