package com.onb.snaph;

/**
 * This enum limits the requests of the mobile user to the web application, narrowing his/her
 * chances of issuing invalid requests.
 * 
 * @author ken
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
