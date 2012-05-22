package com.onb.snaph;

/**
 * 
 * 
 *
 */
public class UserAccount {
	private String accessToken;
	private String userId;
	private boolean toPost;
	
	public UserAccount(String accessToken, String userId, boolean toPost) {
		this.accessToken = accessToken;
		this.userId = userId;
		this.toPost = toPost;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isToPost() {
		return toPost;
	}
	
	public String toPost() {
		return toPost?"Yes":"No";
	}

	@Override
	public String toString() {
		return "UserAccount [" + accessToken + "," + userId + "," + toPost + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessToken == null) ? 0 : accessToken.hashCode());
		result = prime * result + (toPost ? 1231 : 1237);
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserAccount)) {
			return false;
		}
		UserAccount other = (UserAccount) obj;
		if (accessToken == null) {
			if (other.accessToken != null) {
				return false;
			}
		} else if (!accessToken.equals(other.accessToken)) {
			return false;
		}
		if (toPost != other.toPost) {
			return false;
		}
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		} else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}
	
	
}
