package com.konantech.forensic.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class TagInfo {
	String recordType;		//(내부파일 종류) 기본 레코드 또는 내부 파일 
	String fieldName;		//태그가 가리키는 필드 이름 
	boolean hasValueInLine;		//태그와 같은 줄에 값이 포함되어 있는지 여부 - 값은 하나의 라인으로 종료됨을 가정함
	boolean isRecordStarter;		//내부 레코드(기본 레코드 또는 내부 파일)를 새로 시작하게 하는 태그인지 여부
	StringBuffer content; // 내용
	
	public TagInfo(String recordType, String fieldName, boolean hasValueInLine, boolean isRecordStarter) {
		this.recordType = recordType;
		this.fieldName = fieldName;
		this.hasValueInLine = hasValueInLine;
		this.isRecordStarter = isRecordStarter;
		this.content = new StringBuffer();
	}
}
