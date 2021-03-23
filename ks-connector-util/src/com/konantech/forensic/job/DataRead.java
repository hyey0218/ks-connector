package com.konantech.forensic.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konantech.forensic.common.CommonUtils;
import com.konantech.forensic.common.Const;
import com.konantech.forensic.dao.DatabaseDao;
import com.konantech.forensic.vo.Record;
import com.konantech.forensic.vo.TagInfo;

public class DataRead {
	private static final Logger log = LoggerFactory.getLogger(DataRead.class);

	private String runningFileName = "";


	static HashMap<String, TagInfo> TAG_MAP = createTagMap();

	public static HashMap<String, TagInfo> createTagMap() {
		HashMap<String, TagInfo> tagMap = new HashMap<String, TagInfo>();
		tagMap.put("..EMAIL", new TagInfo(Const.TYPE_INTERNAL_BASE_RECORD, "MAILBODY", false, true));
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
		tagMap.put("..RETURN_CODE:", new TagInfo(Const.TYPE_INTERNAL_FILE, "RETURN_CODE", true, false));
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

	private static Pattern PATTERN = Pattern.compile("^[.]{2}[_A-Z]+[:|$]?", Pattern.MULTILINE);

	/**
	 * KONAN_TEXTFILTER_FORMAT
	 */
	private static Map<String, ArrayList<String>> FORMAT_INFO = getFormatInfo();

	public static Map<String, ArrayList<String>> getFormatInfo() {
		if(DatabaseDao.isConnectedDataSource())
			return CommonUtils.setTextFilterFormatInfo(DatabaseDao.selectTextFilterFormatInfo());
		else
			return new HashMap<String, ArrayList<String>>();
	}

	/**
	 * 문자열에 태그가 포함하는지
	 * 
	 * @param line
	 * @return map key값
	 */
	public String isContainRecordTag(String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (matcher.find()) {
			String matchStr = matcher.group();
			if (TAG_MAP.containsKey(matchStr))
				return matcher.group();
			else
				return null;
		} else
			return null;
	}

	public void readFGFToMap(String filepath) {
		this.runningFileName = filepath;
		BufferedReader br = null;
		List<Record> records = new ArrayList<Record>();
		try {
			br = new BufferedReader(new FileReader(new File(filepath)));
			String root = UUID.randomUUID().toString();
			log.info("root :: " + root + " ==> " + filepath);

			makeTagInfo(br, root, records, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
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

		// 후처리
		System.out.println(records.size());
		records.stream().forEach(c -> {
			// 1. SUMMARY_BEGIN - SUMMARY_END
			String summaryInformation = c.getTagMap().get("..SUMMARY_BEGIN").getContent().toString();
			Map<String, String> summaryMap = CommonUtils.getFileMetaSummaryMapFromString(summaryInformation);
			c.setSummaryMap(summaryMap);
			// 2. FILE EXTENSION
			if(summaryMap.get("FILEMETA_FORMAT") != null) {
				summaryMap.putAll(CommonUtils.setFileFormatInfo(FORMAT_INFO,
						c.getTagMap().get("..FILE:").getContent().toString(), summaryMap.get("FILEMETA_FORMAT")));
			}

		});

		
		
		
		try {
			RecordWriter rw = new RecordWriter();
//			rw.setSaveFgfPath("C:\\Users\\hyeyoon.cho\\git\\ks-connector-util\\ks-connector-util\\dmp\\");
			rw.writeRecordToFGF(records);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static Stack<String> parents = new Stack<String>();

	public void makeTagInfo(BufferedReader br, String root, List<Record> records, TagInfo tagInfo) throws IOException {
		String brLine = null;
		while ((brLine = br.readLine()) != null) {
			String key = null;
			if ((key = isContainRecordTag(brLine)) != null) { // ..TAG 내용이 있는 line
				tagInfo = TAG_MAP.get(key);
				if (tagInfo == null) {
					parents.pop();
					continue;
				}
				///////////////////// if (tagInfo.isRecordStarter()) START /////////////////////
				if (tagInfo.isRecordStart()) { // file의 시작을 알리는 태그일 떄 (FILE, EMAIL)
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
					} else if ("..EMAIL".equals(key)) {
						record.generateKeys(root, parents.peek());
					}
					recordTagMap.replace(key, tagInfo);
					record.setTagMap(recordTagMap);
					records.add(record);
				}
				/////////////////////// if (tagInfo.isRecordStarter()) END
				/////////////////////// /////////////////////////
				else {// 그 외 태그
					Record current = records.get(records.size() - 1);
					tagInfo = current.getTagMap().get(key);
//					if (key.lastIndexOf(":") != -1) {
					if (tagInfo.isValueInLine()) {
						tagInfo.getContent().append(brLine.replace(key, ""));
					}
				}
				makeTagInfo(br, root, records, tagInfo); // Recursive
			} else if (tagInfo != null) { // 태그 시작된 이후(tagInfo 정보 O) + 태그가 포함되지 않는 내용 line
				if (tagInfo.getContent().length() > 0)
					tagInfo.getContent().append("\n");
				tagInfo.getContent().append(brLine);
			} 
			else {
				log.error("The filtered Tag is weird... \n root file :: " + this.runningFileName);
				if( !brLine.isEmpty() ) {
					log.error("line :: " + brLine);
					if(records.size() > 0 ) {
						Record record = records.get(records.size() - 1);
						log.error("record PK :: " + record.getPk());
					}
				}
					
			}
		}
	}

}
