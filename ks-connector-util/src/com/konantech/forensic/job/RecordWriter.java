package com.konantech.forensic.job;

import java.io.BufferedWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konantech.forensic.vo.Record;

public class RecordWriter {
	private static final Logger log = LoggerFactory.getLogger(RecordWriter.class);
	
	private BufferedWriter bw;
	
	
	public boolean writeRecordToFGF(Record record) {
		boolean success = false;
		
//		record.getTags().stream().forEach(c -> {
//			
//		});
		
		success = true;
		return success;
	}
}
