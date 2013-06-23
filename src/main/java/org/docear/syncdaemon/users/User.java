package org.docear.syncdaemon.users;

public class User {

	private final String username;
	private final String accessToken;

    private User() {
        username = "";
        accessToken = "";
    }
	public User(String username, String accessToken){
		this.username = username;
		this.accessToken = accessToken;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public String getUsername() {
		return username;
	}
}
