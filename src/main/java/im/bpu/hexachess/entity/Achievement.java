package im.bpu.hexachess.entity;

public class Achievement {
	private String achievementId;
	private String name;
	private String description;
	private boolean unlocked;
	public Achievement() {}
	public Achievement(String achievementId, String name, String description, boolean unlocked) {
		this.achievementId = achievementId;
		this.name = name;
		this.description = description;
		this.unlocked = unlocked;
	}
	public String getAchievementId() {
		return achievementId;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public boolean isUnlocked() {
		return unlocked;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}
}