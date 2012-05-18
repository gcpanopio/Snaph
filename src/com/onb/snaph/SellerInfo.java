package com.onb.snaph;

/*
 * A class whose fields are immutable. The purpose is to wrap into one package user information
 * before sending to the web app.
 */

public class SellerInfo {
	private UserAccount facebook;
	private UserAccount twitter;
	private AndroidUserCommand command;
	
	public SellerInfo(UserAccount facebook, UserAccount twitter,
			AndroidUserCommand command) {
		this.facebook = facebook;
		this.twitter = twitter;
		this.command = command;
	}
	
	public UserAccount getFacebook() {
		return facebook;
	}
	public UserAccount getTwitter() {
		return twitter;
	}
	public AndroidUserCommand getCommand() {
		return this.command;
	}
	public String getCommandAsString() {
		return getCommand().toString();
	}
}
