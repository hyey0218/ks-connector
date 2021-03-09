package com.konantech.forensic.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konantech.forensic.common.CommonUtils;
import com.konantech.forensic.common.Const;
import com.konantech.forensic.common.PropertyConfig;
import com.konantech.forensic.vo.Record;
import com.konantech.forensic.vo.TagInfo;

public class RecordWriter {
	private static final Logger log = LoggerFactory.getLogger(RecordWriter.class);

	private String saveFgfPath;

	public RecordWriter(String saveFgfPath) throws Exception {
		String confSavePath = PropertyConfig.getProperties().getProperty("filter.fgf.savePath");

		if (!new File(confSavePath).isDirectory())
			throw new Exception("filter.fgf.savePath :: check the path value");
		if (!confSavePath.endsWith("/"))
			confSavePath += "/";
		this.saveFgfPath = confSavePath;
	}

	public boolean writeRecordToFGF(List<Record> recordList) {
		boolean success = false;
		int recordIdx = 0;
		BufferedWriter bw = null;
		for (Record record : recordList) {
			try {
				Map<String, String> fgfMap = new HashMap<String, String>();
				fgfMap.put("DOC_ID", record.getPk());
				fgfMap.put("PARENT_DOC_ID", record.getParent());
				fgfMap.put("FAMILY_ID", record.getRoot());
				
				Map<String, TagInfo> tagmap = record.getTagMap();
				tagmap.entrySet().stream().forEach(c ->{
					TagInfo tag = c.getValue();
					fgfMap.put(tag.getFieldName(), tag.getContent().toString());
				});
				
				fgfMap.putAll(record.getSummaryMap());
				
				/////////////////////// 후처리 START ///////////////////////////
				Map<String, String> afterMap = modifyFieldValueMapAfterLoad(fgfMap);
				fgfMap.putAll(afterMap);
				/////////////////////// 후처리 END ///////////////////////////
				
				StringBuffer fgfName = new StringBuffer(saveFgfPath).append(record.getParent())
						.append("_")
						.append(recordIdx++);
				bw = new BufferedWriter(
						new FileWriter(new File(fgfName.toString())));
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(bw != null )
						bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		success = true;
		return success;
	}

	// 필터링 결과 이외에 수정이 필요한 필드들을 처리함 - 레코드 읽기가 끝난 이후에 수행
	public Map<String, String> modifyFieldValueMapAfterLoad(Map<String, String> fieldValueMap)
			throws IOException {
		// 공통 처리
		Map<String, String> returnMap = new HashMap<String, String>();
		
		// 태그 제거 대상
		returnMap.put("FILEBODY", CommonUtils.removeTags(fieldValueMap.get("FILEBODY")));
		returnMap.put("EMLFROM", CommonUtils.removeTags(fieldValueMap.get("EMLFROM")));
		returnMap.put("EMLTO", CommonUtils.removeTags(fieldValueMap.get("EMLTO")));


		// 이메일 주소가 있는 경우에 - NER2(개체명 추출) 모듈 이용 시 수행 불필요함
		String emlTo = fieldValueMap.get("EMLTO");
		if (emlTo != null && emlTo.length() > 0) {
			returnMap.put("EMLTO_ADDRESS",CommonUtils.getOnlyEmailAddress(emlTo));
		}

		// 이메일 주소가 있는 경우에 - NER2(개체명 추출) 모듈 이용 시 수행 불필요함
		String emlFrom = fieldValueMap.get("EMLFROM");
		if (emlFrom != null && emlFrom.length() > 0) {
			returnMap.put("EMLFROM_ADDRESS",
					CommonUtils.getOnlyEmailAddress(emlFrom));
		}

		// 이메일 날짜 (..META_DATE = EMLDATE) 정제 (정렬 및 검색 용도)
		StringBuilder emlDate = new StringBuilder(fieldValueMap.get("EMLDATE"));
		if (emlDate != null && emlDate.length() > 0) {
			try { // 정제된 날짜를 맨 앞으로, 원문 날짜를 뒤에 참조값으로 예:) 20191202 (2016-01-28T14:56:01.000)
				String dateNormalized = CommonUtils.emlDateFormat(emlDate.toString());
				emlDate.insert(0, " (");
				emlDate.append(")");
				emlDate.insert(0, dateNormalized);
				// log.info("new EMLDATE : " + emlDate);
				returnMap.put("EMLDATE", emlDate.toString());
			} catch (Exception e) {
				log.error("emlDateFormat error - EMLDATE, " + emlDate, e);
			}
		}

		StringBuilder fileBody = new StringBuilder(fieldValueMap.get("FILEBODY"));
		if (fileBody.length() > 0) {
			returnMap.put("FILEBODY_SIZE", Long.toString(fileBody.length()) );
		} else {
			returnMap.put("FILEBODY_SIZE", "0");
		}

		// 필터링 호출 결과 저장 - 내부파일의 경우에 BODY와 META에 대한 결과를 동일하게 기록함 ;실제 값은 메타 정보 추출에 대한 결과값임
		String filterMetaErrorMsg = fieldValueMap.get("FILTER_META_ERROR_MSG");
		if (filterMetaErrorMsg != null && filterMetaErrorMsg.length() > 0) {
			if (filterMetaErrorMsg.toString().equals("0")) { // 메타 정보 추출 성공

				returnMap.put("FILTER_META_RESULT", "SUCCESS");
				returnMap.remove("FILTER_META_ERROR_MSG");

				String drmErrorMsg = getErrorMsgIfDrmError(fieldValueMap.get("FILEMETA_FORMAT"));
				if (drmErrorMsg != null && drmErrorMsg.length() > 0) { // 본문 필터링 결과 에러 메시지가 있는 경우
					returnMap.put("FILTER_BODY_RESULT", "FAILED");
					returnMap.put("FILTER_BODY_ERROR_MSG", drmErrorMsg);
				} else {
					returnMap.put("FILTER_BODY_RESULT", "SUCCESS");
					returnMap.remove("FILTER_BODY_ERROR_MSG");
				}

			} else { // 메타 정보 추출 실패
				returnMap.put("FILTER_META_RESULT", "FAILED");
				returnMap.put("FILTER_BODY_RESULT", "FAILED");
				returnMap.put("FILTER_BODY_ERROR_MSG", filterMetaErrorMsg);
			}
		}
		
		return returnMap;
	}

	// (내부파일 필터링 시에) 메타 정보 문자열을 통해서 본문 텍스트 필터링 에러 메시지를 얻음
	// FILEMETA_FORMAT -> DRM Error 라면 drm 에러메세지 리턴
	public static String getErrorMsgIfDrmError(String metaInfo) {
		// DRM 메타 정보가 발견되면 DRM 문서 에러코드 획득
		if (metaInfo.indexOf(Const.INTERNAL_FILE_FILTER_META_ERROR_MSG_STRING_CONTAINS_FOR_DRM_DOCUMENT) > -1) { // DRM
																													// 문서로
																													// 판명되면
			return Const.INTERNAL_FILE_FILTER_BODY_ERROR_MSG_STRING_FOR_DRM_DOCUMENT;
		} else {
			return null;
		}
	}

}
