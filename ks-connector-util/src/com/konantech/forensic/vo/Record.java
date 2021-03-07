package com.konantech.forensic.vo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Record {
	
	Map<String, String> summaryMap;
	Map<String, TagInfo> tagMap;
	String pk;
	String root;
	String parent;

	public void generateKeys(String root, String parent) {
		this.root = root;
		this.parent = parent;
		this.pk = UUID.randomUUID().toString();
	}
}
