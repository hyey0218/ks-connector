package com.konantech.forensic.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.common.collect.Lists;
import com.konantech.forensic.vo.EngineInfo;

import lombok.AllArgsConstructor;

public class DumpManage {
	private ArrayList<EngineInfo> engineInfos;
	
	public void mainDumpByJob() {
		//1. 엔진 정보 초기화
		engineInfos = Lists.newArrayList(new EngineInfo[] {
				new EngineInfo((short) 1, "ksearch_s_local_e1", (long) 0) 
				, new EngineInfo((short)2, "ksearch_s_local_e2",(long)  0)
				, new EngineInfo((short)3, "ksearch_s_local_e3",(long)  0)
		});
		
	}
	
	public void initialDump() {
		
	}
	
	/**
	 * reproc job
	 */
	public void reprocDump() {

		// 각 엔진 사용 용량 비교하면서 job 나눠줌
		Collections.sort(engineInfos, new Comparator<EngineInfo>() {
			@Override
			public int compare(EngineInfo o1, EngineInfo o2) {
				return o1.getFileSizeInCharge().compareTo(o2.getFileSizeInCharge());
			}
		});
		System.out.println(engineInfos.get(0).getFileSizeInCharge());
	}
}

