package com.konantech.forensic.job;

import java.util.HashSet;

import com.google.common.collect.Sets;

public class DataRead {

	private static String[] dataKeys  = {"TEST"};
	
	
	public String getData() {
		HashSet<String> set = Sets.newHashSet(dataKeys);
		return set.toString();
	}
}
