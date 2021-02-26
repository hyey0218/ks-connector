package com.konantech.forensic.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Record {
	
	Long filesize;
	List<TagInfo> tags;
	
	
}
