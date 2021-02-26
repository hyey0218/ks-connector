package com.konantech.forensic.job;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.konantech.forensic.common.Const;
import com.konantech.forensic.vo.Record;
import com.konantech.forensic.vo.TagInfo;

import lombok.AllArgsConstructor;

public class DataRead {

	private static String[] dataKeys = { "..EMAIL", "..META_SUBJECT", "..META_DATE", "..META_FROM", "..META_TO",
			"..META_CC", "..META_RECEIVED_DATE", "..FILE:", "..DEPTH:", "..FILE_SAVED:", "..FILESIZE:",
			"..SUMMARY_BEGIN", "..SUMMARY_END", "..RETURN_CODE:", "..FILE_END:", "..CALENDAR", "..META_STARTTIME",
			"..META_ENDTIME", "..CONTACT", "..META_NAME", "..META_COMPANY", "..META_DEPARTMENT", "..META_JOB_TITLE",
			"..META_TEL", "..META_EMAIL" };
	Map<String, TagInfo> tagMap = ImmutableMap.<String, TagInfo>builder()
			.put("..EMAIL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "FILEBODY", false, true))
			.put("..META_SUBJECT", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLTITLE", false, false))
			.put("..META_DATE", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLDATE", false, false))
			.put("..META_FROM", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLFROM", false, false))
			.put("..META_TO", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLTO", false, false))
			.put("..META_CC", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLCC", false, false))
			.put("..META_RECEIVED_DATE",
					new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_RECEIVED_DATE", false, false))
			.put("..FILE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILENAME", true, false))
			.put("..DEPTH:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILE_DEPTH", true, false))
			.put("..FILE_SAVED:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILEPATH", true, false))
			.put("..FILESIZE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILESIZE", true, false))
			.put("..SUMMARY_BEGIN", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILEMETA_SUMMARY", false, false))
			.put("..SUMMARY_END", new TagInfo(Const.TYPE_INTERNAL_FILE, "DUMMY_SUMMARY_END", false, false))
			.put("..RETURN_CODE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILTER_META_ERROR_MSG", true, false))
			.put("..FILE_END", new TagInfo(Const.TYPE_INTERNAL_FILE, "DUMMY_FILE_END", false, false))
			.put("..CALENDAR", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_CALENDAR", false, true))
			.put("..META_STARTTIME", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_STARTTIME", false, false))
			.put("..META_ENDTIME", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_ENDTIME", false, false))
			.put("..CONTACT", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_CONTACT", false, true))
			.put("..META_NAME", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_NAME", false, false))
			.put("..META_COMPANY", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_COMPANY", false, false))
			.put("..META_DEPARTMENT",
					new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_DEPARTMENT", false, false))
			.put("..META_JOB_TITLE", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_JOB_TITLE", false, false))
			.put("..META_TEL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_TEL", false, false))
			.put("..META_EMAIL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_EMAIL", false, false))
			.build();

	private static Pattern pattern = Pattern.compile("^[.]{2}[_A-Z]+[:|$]?", Pattern.MULTILINE);

	public String isContainRecordTag(String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String matchStr = matcher.group();
			if (tagMap.containsKey(matchStr))
				return matcher.group();
			else
				return null;
		} else
			return null;
	}

	public Properties getProperties() throws Exception {

		Properties props = new Properties();
		FileInputStream istream = null;
		String fileFPath = System.getProperty("konan.configuration");
		if (fileFPath == null)
			throw new Exception("run konan.configuration is null");
		System.out.println("konan.configuration : " + fileFPath);
		try {
			istream = new FileInputStream(fileFPath);
			props.load(istream);
			istream.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (istream != null) {
				try {
					istream.close();
				} catch (Throwable ignore) {
				}
			}
		}
		return props;
	}

	public static void getData() {
		ArrayList<String> list = Lists.newArrayList(dataKeys);

		// 1) List interface 의 sort() 메소드 이용
//		List<Entry<String, Integer>> list1 = new ArrayList<>(map.entrySet());
//		list1.sort(Entry.comparingByValue());
//		Integer i = list1.get(0).getValue() + 0;
//		list1.get(0).setValue(i);

		// 2) stream sorted() 메소드 사용
//		map.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().ifPresent(c -> {
//			Integer ii = c.getValue() + 100;
//			System.out.println(c.setValue(i));
//			System.out.println(i);
//		});

		Map<String, Integer> map2 = getEngineMap();
		System.out.println("---------");
		mapValueCompare(map2, 100);
		System.out.println(map2.get("3"));
		mapValueCompare(map2, 50);
		System.out.println(map2.get("3"));
		mapValueCompare(map2, 49);
		System.out.println(map2.get("3"));
		mapValueCompare(map2, 48);
		
	}

	public static HashMap<String, Integer> getEngineMap() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("1", 0);
		map.put("2", 0);
		map.put("3", 0);
		return map;
	}

	public static String mapValueCompare(Map<String, Integer> map, int parseValue) {
		List<Entry<String, Integer>> list2 = new ArrayList<>(map.entrySet());
		Collections.sort(list2, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
				return obj1.getValue().compareTo(obj2.getValue());
			}
		});
		String tmp = list2.get(0).getKey();
		map.put(tmp, list2.get(0).getValue() + parseValue);
		return tmp;
	}

}
