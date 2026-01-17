package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Achievement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AchievementDAO extends DAO<Achievement> {
	private static final String CREATE =
		"INSERT INTO achievements (achievement_id, name, description) VALUES(?, ?, ?)";
	private static final String UPDATE =
		"UPDATE achievements SET name = ?, description = ? WHERE achievement_id = ?";
	private static final String DELETE = "DELETE FROM achievements WHERE achievement_id = ?";
	private static final String READ = "SELECT * FROM achievements WHERE achievement_id = ?";
	private static final String READ_ALL = "SELECT * FROM achievements";
	private static final String READ_ALL_FOR_PLAYER =
		"SELECT a.achievement_id, a.name, a.description, CASE WHEN pa.player_id IS NOT NULL THEN "
		+ "TRUE ELSE FALSE END AS unlocked FROM achievements a LEFT JOIN unlocks pa ON "
		+ "a.achievement_id = pa.achievement_id AND pa.player_id = ? ORDER BY a.name ";
	@Override
	public Achievement create(final Achievement achievement) {
		try (final PreparedStatement pstmt = connect.prepareStatement(CREATE)) {
			pstmt.setString(1, achievement.getAchievementId());
			pstmt.setString(2, achievement.getName());
			pstmt.setString(3, achievement.getDescription());
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return achievement;
	}
	@Override
	public Achievement update(final Achievement achievement) {
		try (final PreparedStatement pstmt = connect.prepareStatement(UPDATE)) {
			pstmt.setString(1, achievement.getName());
			pstmt.setString(2, achievement.getDescription());
			pstmt.setString(3, achievement.getAchievementId());
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return achievement;
	}
	@Override
	public void delete(final Achievement achievement) {
		try (final PreparedStatement pstmt = connect.prepareStatement(DELETE)) {
			pstmt.setString(1, achievement.getAchievementId());
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
	}
	private Achievement resultSetToAchievement(final ResultSet rs) throws SQLException {
		final Achievement achievement = new Achievement(rs.getString("achievement_id"),
			rs.getString("name"), rs.getString("description"), rs.getBoolean("unlocked"));
		return achievement;
	}
	public Achievement read(final String achievementId) {
		Achievement achievement = null;
		try (final PreparedStatement pstmt = connect.prepareStatement(READ)) {
			pstmt.setString(1, achievementId);
			try (final ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					achievement = resultSetToAchievement(rs);
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return achievement;
	}
	public ArrayList<Achievement> readAll() {
		final ArrayList<Achievement> achievements = new ArrayList<>();
		try (final Statement stmt = connect.createStatement();
			final ResultSet rs = stmt.executeQuery(READ_ALL)) {
			while (rs.next()) {
				achievements.add(resultSetToAchievement(rs));
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return achievements;
	}
	public void unlock(final String playerId, final String achievementId) {
		final String sql = "INSERT IGNORE INTO unlocks (player_id, achievement_id) VALUES (?, ?)";
		try (final PreparedStatement pstmt = connect.prepareStatement(sql)) {
			pstmt.setString(1, playerId);
			pstmt.setString(2, achievementId);
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
	}
	public ArrayList<Achievement> readAllForPlayer(final String playerId) {
		final ArrayList<Achievement> achievements = new ArrayList<>();
		try (final PreparedStatement pstmt = connect.prepareStatement(READ_ALL_FOR_PLAYER)) {
			pstmt.setString(1, playerId);
			try (final ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					final Achievement achievement =
						new Achievement(rs.getString("achievement_id"), rs.getString("name"),
							rs.getString("description"), rs.getBoolean("unlocked"));
					achievement.setUnlocked(rs.getBoolean("unlocked"));
					achievements.add(achievement);
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return achievements;
	}
}