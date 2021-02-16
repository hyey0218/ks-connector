package com.konantech.forensic.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Const {
	public static final Logger log = LoggerFactory.getLogger(Const.class);

	public static final String STATUS_TASK_TASK_REQUESTED = "TASK_REQUESTED";		//작업 요청된 상태
	public static final String STATUS_TASK_LISTING_SOURCE_DIR = "LISTING_SOURCE_DIR";		//파일 목록 얻고 있는 상태
	public static final String STATUS_TASK_RESOURCES_ASSIGNED = "RESOURCES_ASSIGNED";		//색인 자원(엔진 및 볼륨) 할당된 상태
	public static final String STATUS_TASK_FILTERING_FILES = "FILTERING_FILES";		//색인 엔진이 필터링하고 있는 상태
	public static final String STATUS_TASK_INDEXING_COMPLETED = "INDEXING_COMPLETED";		//색인 엔진이 INDEXING 완료한 상태
	//public static final String STATUS_TASK_VOLUME_CACHED = "VOLUME_CACHED";		//색인 볼륨을 캐시 서버로 적재한 상태
	public static final String STATUS_TASK_RETRY_REQUESTED = "RETRY_REQUESTED";		//선택(실패)건 재시도 요청 상태
	public static final String STATUS_TASK_RETRYING_FILTER_FAILED = "RETRYING_FILTER_FAILED";		//선택(실패)건 재시도 처리 중인 상태
	public static final String STATUS_TASK_VOLUME_EXPORTED = "VOLUME_EXPORTED";		//색인 볼륨 다운로드 완료한 상태
	public static final String STATUS_TASK_RESOURCES_RELEASED = "RESOURCES_RELEASED";		//작업에 할당된 자원 회수 완료 상태
	
	public static final String STATUS_RESOURCE_IDLE = "IDLE";		//자원 상태 - 유휴
	public static final String STATUS_RESOURCE_PROCESSING = "PROCESSING";		//자원 상태 - 작업 중 
	public static final String STATUS_RESOURCE_SERVICE = "SERVICE";		//자원 상태 - 서비스 중 

	public static final String TYPE_RESOURCE_ENGINE = "ENGINE";		//자원 종류 - 엔진
	public static final String TYPE_RESOURCE_VIRTUAL_VOLUME = "VIRTUAL_VOLUME";		//자원 종류 - 가상 볼륨
	public static final String TYPE_RESOURCE_VOLUME = "VOLUME";		//자원 종류 - 볼륨 
	public static final String TYPE_RESOURCE_VOLUME_CACHE = "VOLUME_CACHE";		//자원 종류 - 볼륨 캐시

	public static final String RESOURCE_EXCLUSIVE_USE_PARALLEL = "PARALLEL";		//자원 전용 용도 - 병렬
	public static final String RESOURCE_EXCLUSIVE_USE_SERIAL = "SERIAL";		//자원 전용 용도 - 직렬

	public static String ENGINE_ID_STRING_CONTAINS = "ksearch";		//엔진ID에 포함되는 문자열 - 엔진 디렉토리 경로에 포함
	
	public static String DEFAULT_TASK_STEP_SCENARIO = "DEFAULT";

	public static String UPLOAD_SOURCE_LIST_DIR = "../../UPLOAD_SOURCE_LIST"; 		//업로드 source_dir 목록 파일 생성 위치 (공유 스토리지 내에 위치)
	//public static String UPLOAD_SOURCE_LIST_DIR = "/konanfs/konan/UPLOAD_SOURCE_LIST"; 		//업로드 source_dir 목록 파일 생성 위치 (공유 스토리지 내에 위치)
	public static String UPLOAD_SOURCE_BACKUP_DIR = "../source_backup"; 		//업로드 목록 파일 처리 후 이동 (백업) 위치 (local 스토리지 내에 위치)
	public static String UPLOAD_SOURCE_CNT_TARGET_DIR = "../rawdata/fsg_file/bulk"; 		//업로드 파일 필터링 결과 DUMP 위치 (local 스토리지 내에 위치)
	public static String RETRY_CNT_TARGET_DIR = "../rawdata/fsg_file_retry"; 		//실패건 필터링 재시도 결과 DUMP 위치 (local 스토리지 내에 위치)
	public static long SOURCE_FILE_COUNT_MIN_PER_LIST_FILE = 25; 		//업로드 목록 파일에 포함할 원문파일들의 최소 개수 제한
	public static long SOURCE_SIZE_MIN_PER_LIST_FILE = FileUtils.ONE_GB * 5; 		//업로드 목록 파일에 포함할 원문파일들의 크기의 합 제한
	
	public static long sourceDirSizeRequiringParallelProcess = FileUtils.ONE_GB * 0; 	//병렬 처리 필요한 업로드 최소 크기
	
	public static long DELAY_MILLISECONDS_BETWEEN_RUNNING_DUMPMANAGE = 5;

	public static final String FILE_FORMAT_GROUP_ETC = "기타";		//FILE_FORMAT_GROUP 을 갖지 않는 포맷에 지정할 GROUP

	public static int GROUP_BY_KEY_MAX_BYTES = 1000;	//GROUP BY 결과 키 최대 길이값 제약 MAX = 228 Byte (BOT-292, SGBOARD-6838)
	
	public static final String TYPE_EXTERNAL_FILE = "EXTERNAL_FILE";		//(외부파일 종류) SOURCE_DIR에 업로드된 원본 파일 
	public static final String TYPE_INTERNAL_BASE_RECORD = "BASE_RECORD";		//(내부파일 종류) 기본 레코드 - (1)단일 파일 그 자체 또는 (2)이메일 본문 
	public static final String TYPE_INTERNAL_FILE = "INTERNAL_FILE";		//(내부파일 종류) 내부 파일 (1)압축된 내부 파일 또는 (2)이메일 첨부 파일
	//public static final String TYPE_INTERNAL_GARBAGE_RECORD = "GARBAGE_RECORD";		//(내부파일 종류) 불필요 정보 (삭제 대상)
	
	//동일 tag 반복 시 처리 방법 지정
	public static final int TAG_REPEAT_ACTION_START_NEW_RECORD		= 0; 		//새 레코드 시작
	//public static final int TAG_REPEAT_ACTION_REPLACE				= 1; 		//덮어쓰기
	public static final int TAG_REPEAT_ACTION_SKIP					= 2;		//무시하기
	//public static final int TAG_REPEAT_ACTION_APPEND_STRING			= 3;		//다중값 입력
	//public static final int TAG_REPEAT_ACTION_SET_TO_INTERNAL_FILE	= 4;		//현재 INTERNAL_FILE에 값 지정
	//public static final int TAG_REPEAT_ACTION_SET_TO_BASE_RECORD	= 5;		//현재 BASE_RECORD에 값 지정
	
	public static final String INTERNAL_FILE_DIR_FILTER = "../ext/filter/bin/download";		//txf4-conf.rc 파일에 지정된 internal_file_dir 경로값과 동일하게 설정 (파일 경로 용도)
	public static final String INTERNAL_FILE_DIR_PUBLISH = "../../INTERNAL_FILE";		//internal_file_dir 에서 파일들을 옮겨서 보관할 장소 
	
	public static final String FILTER_BIN_DIR = "../ext/filter/bin/";		//문서 필터 실행 디렉토리 경로
	
	public static final long FIELD_VALUE_MAX_LENGTH = 32000000;		//DUMP 시 각 필드 최대 길이 제한 (바이트 단위로 측정해야 마땅하지만 단순화하기 위해 길이로 함)

	public static final boolean KEEP_FILTER_EXTRACTED_TEXT_FILE = false;		//필터에서 출력한 필터 결과 파일을 보존할 것인지 여부
	
	public static final boolean APPLY_PREPROCESSING_FOR_FILTERED_FILENAME = false;		//전처리 대상 파일들에 적용 여부
	public static final List<String> PREPROCESSING_FILENAME_FILTER = Arrays.asList("zip", "tar", "gz");		//전처리 대상 파일명 배열 지정
	public static final String EXTRACTED_DIRECTORY_POSTFIX_WHILE_PREPROCESSING = "._KONAN_";		//전처리 출력을 기록할 경로

	//public static final int INTERNAL_FIELDS_FOUND_COUNT_REQUIRED_TO_FLUSH_RECORD_MAP = 2; 	//내부 레코드 기록을 위해 필터링 결과에 포함되어야 할 최소 필드 수 

	public static final int INTERNAL_PROCESS_STATUS_USE_CURRENT_TAG_VALUE = 0;
	public static final int INTERNAL_PROCESS_STATUS_NEED_TO_START_NEW_RECORD = 1;		//다음 행에서 시작할 레코드 예약
	public static final int INTERNAL_PROCESS_STATUS_DEPTH_OF_CURRENT_RECORD = 2;		//현재의 레코드(종류에 무관하게) 깊이
	
	public static final String FIELD_NAME_TO_IDENTIFYING_ROOT_RECORD_OF_FAMILY = "FILE_FORMAT_GROUP";		//FAMILY의 최상위 레코드를 식별하기 위한 필드명
	public static final String FIELD_VALUE_TO_IDENTIFYING_ROOT_RECORD_OF_FAMILY = "이메일";					//FAMILY의 최상위 레코드를 식별하기 위한 필드값
	
	public static final String INTERNAL_FILE_FILTER_META_ERROR_MSG_STRING_CONTAINS_FOR_DRM_DOCUMENT = "DRM";		//메타 정보 추출 시 포맷 정보 내에 포함되는 DRM 문서 판단 기준 문자열 ;http://zeus.konantech.com/browse/SGBOARD-6445#action_126161
	public static final String INTERNAL_FILE_FILTER_BODY_ERROR_MSG_STRING_FOR_DRM_DOCUMENT = "ERROR_SN3FMT_DRM_DOCUMENT";	//내부 파일인 DRM 문서에 대해 FILTER_BODY_ERROR_MSG 세팅할 문자열 
	
	public static final ArrayList<String> FIELD_MAPPING_LIST = new ArrayList<String>(Arrays.asList(
			"FILEPATH",
			"TASK_ID",
			"DOC_ID",
			"PARENT_DOC_ID",
			"FAMILY_ID",
			"FILENAME",
			"PARENT_FILENAME",
			"FILEBODY",
			"FILESIZE",
			"FILEDATE",
			"RECORD_TYPE",
			"SOURCE_DIR",
			"FILE_DEPTH",
			"FILE_DIRECTORY",
			"FILE_DIRECTORY_KEY",
			"FILE_TOP_FOLDER",
			"FILE_FORMAT_GROUP",
			"FILE_FORMAT_GROUP_EMAIL",
			"FILE_FORMAT_GROUP_DISPLAY",
			"FILE_FORMAT_EXTENSION",
			"FILENAME_EXTENSION",
			"FILENAME_EXTENSION_FORGED",
			"FILEEXT",
			"FILEBODY_SIZE",
			"FILEHASH",
			"FILTER_BODY_RESULT",
			"FILTER_BODY_ERROR_MSG",
			"FILTER_META_RESULT",
			"FILTER_META_ERROR_MSG",
			"FILEMETA_SUMMARY",
			"FILEMETA_FORMAT",
			"FILEMETA_PROGRAM",
			"FILEMETA_TITLE",
			"FILEMETA_SUBJECT",
			"FILEMETA_AUTHOR",
			"FILEMETA_LAST",
			"FILEMETA_KEYWORDS",
			"FILEMETA_COMMENTS",
			"FILEMETA_TEMPLATE",
			"FILEMETA_REVISION",
			"FILEMETA_CREATEDTM",
			"FILEMETA_SAVEDTM",
			"FILEMETA_PAGES",
			"FILEMETA_WORDS",
			"FILEMETA_PARAGRAPHS",
			"FILEMETA_LINES",
			"FILEMETA_CHARACTERS",
			"FILEMETA_DATE",
			"EMLTITLE",
			"EMLTO",
			"EMLTO_ADDRESS",
			"EMLFROM",
			"EMLFROM_ADDRESS",
			"EMLDATE",
			"EMLATTACHFILENAME"
			));
}
