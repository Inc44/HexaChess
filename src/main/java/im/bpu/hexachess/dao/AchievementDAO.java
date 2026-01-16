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
	@Override
	public Achievement create(Achievement achievement) {
		try (PreparedStatement pstmt = connect.prepareStatement(CREATE)) {
			pstmt.setString(1, achievement.getAchievementId());
			pstmt.setString(2, achievement.getName());
			pstmt.setString(3, achievement.getDescription());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return achievement;
	}
	@Override
	public Achievement update(Achievement achievement) {
		try (PreparedStatement pstmt = connect.prepareStatement(UPDATE)) {
			pstmt.setString(1, achievement.getName());
			pstmt.setString(2, achievement.getDescription());
			pstmt.setString(3, achievement.getAchievementId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return achievement;
	}
	@Override
	public void delete(Achievement achievement) {
		try (PreparedStatement pstmt = connect.prepareStatement(DELETE)) {
			pstmt.setString(1, achievement.getAchievementId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	private Achievement resultSetToAchievement(ResultSet rs) throws SQLException {
		Achievement achievement = new Achievement(
			rs.getString("achievement_id"), rs.getString("name"), rs.getString("description"), rs.getBoolean("unlocked"));
		return achievement;
	}
	public Achievement read(String achievementId) {
		Achievement achievement = null;
		try (PreparedStatement pstmt = connect.prepareStatement(READ)) {
			pstmt.setString(1, achievementId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					achievement = resultSetToAchievement(rs);
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return achievement;
	}
	public ArrayList<Achievement> readAll() {
		ArrayList<Achievement> achievements = new ArrayList<>();
		try (Statement stmt = connect.createStatement();
			ResultSet rs = stmt.executeQuery(READ_ALL)) {
			while (rs.next()) {
				achievements.add(resultSetToAchievement(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return achievements;
	}
    public void unlock(String playerId, String achievementId) {
        String sql = "INSERT IGNORE INTO player_achievements (player_id, achievement_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
            pstmt.setString(1, playerId);
            pstmt.setString(2, achievementId);
            pstmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
	public ArrayList<Achievement> readAllForPlayer(String playerId) {
    ArrayList<Achievement> achievements = new ArrayList<>();

    String sql = """
        SELECT
            a.achievement_id,
            a.name,
            a.description,
            CASE
                WHEN pa.player_id IS NOT NULL THEN TRUE
                ELSE FALSE
            END AS unlocked
        FROM achievements a
        LEFT JOIN player_achievements pa
            ON a.achievement_id = pa.achievement_id
            AND pa.player_id = ?
        ORDER BY a.name
    """;

    try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
        pstmt.setString(1, playerId);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Achievement achievement = new Achievement(
                    rs.getString("achievement_id"),
                    rs.getString("name"),
                    rs.getString("description"),
					rs.getBoolean("unlocked")
                );
                achievement.setUnlocked(rs.getBoolean("unlocked"));

                achievements.add(achievement);
            }
        }
    } catch (SQLException exception) {
        exception.printStackTrace();
    }

    return achievements;
}

	
}