package com.konantech.forensic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class EngineInfo {
	short engineNo;
	String engineName;
	Long fileSizeInCharge;

}
