package com.konantech.forensic.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.konantech.connector.core.TextFilter;
import com.konantech.connector.util.Config;
import com.konantech.forensic.common.CommonUtils;
import com.konantech.forensic.common.Const;
import com.konantech.forensic.common.PropertyConfig;
import com.konantech.forensic.vo.Record;
import com.konantech.forensic.vo.TagInfo;
import com.konantech.kmp.JSONConst;
import com.konantech.kmp.JValue;
import com.konantech.kmp.KJSON;
import com.konantech.kmp.KMP;

import lombok.AllArgsConstructor;

public class DataRead {
	private static final Logger log = LoggerFactory.getLogger(DataRead.class);
	private static Properties konanProps = PropertyConfig.getProperties();

	private static String[] dataKeys = { "..EMAIL", "..META_SUBJECT", "..META_DATE", "..META_FROM", "..META_TO",
			"..META_CC", "..META_RECEIVED_DATE", "..FILE:", "..DEPTH:", "..FILE_SAVED:", "..FILESIZE:",
			"..SUMMARY_BEGIN", "..SUMMARY_END", "..RETURN_CODE:", "..FILE_END:", "..CALENDAR", "..META_STARTTIME",
			"..META_ENDTIME", "..CONTACT", "..META_NAME", "..META_COMPANY", "..META_DEPARTMENT", "..META_JOB_TITLE",
			"..META_TEL", "..META_EMAIL" };

	static HashMap<String, TagInfo> TAG_MAP = createTagMap();

	public static HashMap<String, TagInfo> createTagMap() {
		HashMap<String, TagInfo> tagMap = new HashMap<String, TagInfo>();
		tagMap.put("..EMAIL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "FILEBODY", false, true));
		tagMap.put("..META_SUBJECT", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLTITLE", false, false));
		tagMap.put("..META_DATE", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLDATE", false, false));
		tagMap.put("..META_FROM", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLFROM", false, false));
		tagMap.put("..META_TO", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLTO", false, false));
		tagMap.put("..META_CC", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "EMLCC", false, false));
		tagMap.put("..META_RECEIVED_DATE",
				new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_RECEIVED_DATE", false, false));
		tagMap.put("..FILE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILENAME", true, true));
		tagMap.put("..DEPTH:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILE_DEPTH", true, false));
		tagMap.put("..FILE_SAVED:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILEPATH", true, false));
		tagMap.put("..FILESIZE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILESIZE", true, false));
		tagMap.put("..SUMMARY_BEGIN", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILEMETA_SUMMARY", false, false));
		tagMap.put("..SUMMARY_END", new TagInfo(Const.TYPE_INTERNAL_FILE, "DUMMY_SUMMARY_END", false, false));
		tagMap.put("..RETURN_CODE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "FILTER_META_ERROR_MSG", true, false));
//		tagMap.put("..FILE_END", new TagInfo(Const.TYPE_INTERNAL_FILE, "DUMMY_FILE_END", false, false));
		tagMap.put("..FILE_END", null);
		tagMap.put("..CALENDAR", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_CALENDAR", false, true));
		tagMap.put("..META_STARTTIME",
				new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_STARTTIME", false, false));
		tagMap.put("..META_ENDTIME", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_ENDTIME", false, false));
		tagMap.put("..CONTACT", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_CONTACT", false, true));
		tagMap.put("..META_NAME", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_NAME", false, false));
		tagMap.put("..META_COMPANY", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_COMPANY", false, false));
		tagMap.put("..META_DEPARTMENT",
				new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_DEPARTMENT", false, false));
		tagMap.put("..META_JOB_TITLE",
				new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_JOB_TITLE", false, false));
		tagMap.put("..META_TEL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_TEL", false, false));
		tagMap.put("..META_EMAIL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "DUMMY_META_EMAIL", false, false));
		return tagMap;
	}

	private static Pattern pattern = Pattern.compile("^[.]{2}[_A-Z]+[:|$]?", Pattern.MULTILINE);

	/**
	 * 문자열에 태그가 포함하는지
	 * 
	 * @param line
	 * @return map key값
	 */
	public String isContainRecordTag(String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String matchStr = matcher.group();
			if (TAG_MAP.containsKey(matchStr))
				return matcher.group();
			else
				return null;
		} else
			return null;
	}

	private static String filterPath = konanProps.getProperty("filter.path");

	public String makeFGFFiles(String resultFilePath) {
		String tmp = resultFilePath.substring(resultFilePath.lastIndexOf("/") + 1, resultFilePath.length());
		String filePath = filterPath + File.separator + tmp;

		return "";

	}

	public static void main(String[] args) {
		DataRead dr = new DataRead();
		dr.readFGFToMap("C:\\Users\\hyeyoon\\git\\ks-connector\\ks-connector-util\\resources\\mmm");
	}

	public void readFGFToMap(String filepath) {
		BufferedReader br = null;
		List<Record> records = new ArrayList<Record>();
		try {
			br = new BufferedReader(new FileReader(new File(filepath)));
			String root = UUID.randomUUID().toString();
			
			makeTagInfo(br, root,  records, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(records.size());
		records.stream().forEach(c -> {
			System.out.println("-----------------------------------------------------");
			String file = c.getTagMap().get("..FILE:").getContent().toString();
			if (file.length() > 0)
				System.out.println(file);
			else
				System.out.println(c.getTagMap().get("..META_SUBJECT").getContent().toString());
			System.out.println(c.getTagMap().get("..DEPTH:").getContent().toString());
			System.out.println(c.getPk() + " " + c.getParent());
			System.out.println("-----------------------------------------------------");
		});
		
		
		// 후처리
		// 1. SUMMARY_BEGIN - SUMMARY_END
		Map<String, String> summaryMap = CommonUtils
				.getFileMetaSummaryMapFromString(TAG_MAP.get("..SUMMARY_BEGIN").getContent().toString());

	}
	static Stack<String> parents = new Stack<String>();
	public void makeTagInfo(BufferedReader br, String root, List<Record> records, TagInfo tagInfo) throws IOException {
		String brLine = null;
		while ((brLine = br.readLine()) != null) {
			String key = null;
			if ((key = isContainRecordTag(brLine)) != null) { // ..TAG 포함하는 line
				tagInfo = TAG_MAP.get(key);
				if( tagInfo == null ) {
					parents.pop();
					continue;
				}
				/////////////////////if (tagInfo.isRecordStarter()) START /////////////////////
				if (tagInfo.isRecordStarter()) { // file의 시작을 알리는 태그일 떄 (FILE, EMAIL)
					Record record = new Record();
					HashMap<String, TagInfo> recordTagMap = createTagMap();
					tagInfo = recordTagMap.get(key);
					if (key.lastIndexOf(":") != -1) {
						String tmpTag = brLine.replace(key, "");
						tagInfo.getContent().append(tmpTag);
					}
					if ("..FILE:".equals(key)) {
						if (parents.isEmpty()) {// 최상단 파일일 때
							record.generateKeys(root, root);
						} else {
							record.generateKeys(root, parents.peek());
						}
						parents.push(record.getPk());
					}else if("..EMAIL".equals(key)) {
						record.generateKeys(root, parents.peek());
					}
					recordTagMap.replace(key, tagInfo);
					record.setTagMap(recordTagMap);
					records.add(record);
				} 
				/////////////////////// if (tagInfo.isRecordStarter()) END /////////////////////////
				else {// 그 외 태그
					Record current = records.get(records.size() - 1);
					tagInfo = current.getTagMap().get(key);
					if (key.lastIndexOf(":") != -1) {
						tagInfo.getContent().append(brLine.replace(key, ""));
					}
				}
				makeTagInfo(br, root, records, tagInfo); // Recursive
			} else if (tagInfo != null) { // 태그가 포함되지 않는 내용 line
				if (tagInfo.getContent().length() > 0)
					tagInfo.getContent().append("\n");
				tagInfo.getContent().append(brLine);
			}
		}

	}
}
