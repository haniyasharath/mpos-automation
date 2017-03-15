package com.marks.mpos.deployment.check.UUID;

import java.util.UUID;

public class UUIDGenerator {
	public static String generate() {
	    final String uuid = UUID.randomUUID().toString();
	    //System.out.println("uuid = " + uuid);
		return uuid;
	}
}
