package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Achievement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AchievementDAO extends DAO<Achievement> {
	@Override
	public Achievement create(Achievement obj) {
		String requete =
			"INSERT INTO achievements (achievement_id, name, description) VALUES(?, ?, ?)";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getAchievementId());
			pstmt.setString(2, obj.getName());
			pstmt.setString(3, obj.getDescription());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}
	@Override
	public Achievement update(Achievement obj) {
		String requete =
			"UPDATE achievements SET name = ?, description = ? WHERE achievement_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getName());
			pstmt.setString(2, obj.getDescription());
			pstmt.setString(3, obj.getAchievementId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}
	@Override
	public void delete(Achievement obj) {
		String requete = "DELETE FROM achievements WHERE achievement_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getAchievementId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	public Achievement read(String id) {
		Achievement a = null;
		String requete = "SELECT * FROM achievements WHERE achievement_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				a = new Achievement(rs.getString("achievement_id"), rs.getString("name"),
					rs.getString("description"));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return a;
	}
	public ArrayList<Achievement> readAll() {
		ArrayList<Achievement> list = new ArrayList<>();
		String requete = "SELECT * FROM achievements";
		try {
			ResultSet rs = stmt.executeQuery(requete);
			while (rs.next()) {
				list.add(new Achievement(rs.getString("achievement_id"), rs.getString("name"),
					rs.getString("description")));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return list;
	}
}
