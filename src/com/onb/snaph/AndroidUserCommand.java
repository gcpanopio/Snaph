package com.onb.snaph;

/**
 * This enum limits the requests of the mobile user to the web application, narrowing his/her
 * chances of issuing invalid requests. Contains three commands: INSERT, EDIT and DELETE
 *
 */

public enum AndroidUserCommand {
	INSERT("insert"),
	EDIT("edit"),
	DELETE("delete");
	
	private String stringValue;
	
	AndroidUserCommand(String stringValue) {
		this.stringValue = stringValue;
	}
	
	@Override
	public String toString() {
		return stringValue;
	}
}
