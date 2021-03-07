package com.konantech.forensic.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CommonUtils {

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
	
}
