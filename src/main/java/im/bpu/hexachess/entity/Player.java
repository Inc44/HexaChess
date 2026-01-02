package im.bpu.hexachess.entity;

import java.time.LocalDateTime;

public class Player {
	private String playerId;
	private String handle;
	private String email;
	private String passwordHash;
	private int rating;
	private boolean isVerified;
	private LocalDateTime joinedAt;
	private String token;
	private String avatar;
	private String location;
	public Player() {}
	public Player(String playerId, String handle, String email, String passwordHash, int rating,
		boolean isVerified, LocalDateTime joinedAt) {
		this.playerId = playerId;
		this.handle = handle;
		this.email = email;
		this.passwordHash = passwordHash;
		this.rating = rating;
		this.isVerified = isVerified;
		this.joinedAt = joinedAt;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public boolean isVerified() {
		return isVerified;
	}
	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
	public LocalDateTime getJoinedAt() {
		return joinedAt;
	}
	public void setJoinedAt(LocalDateTime joinedAt) {
		this.joinedAt = joinedAt;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
