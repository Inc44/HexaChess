package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO extends DAO<Settings> {
	@Override
	public Settings create(Settings obj) {
		String requete = "INSERT INTO settings (player_id, theme, show_legal_moves, "
			+ "auto_promote_queen, ai_difficulty_level) VALUES(?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getPlayerId());
			pstmt.setString(2, obj.getTheme());
			pstmt.setBoolean(3, obj.isShowLegalMoves());
			pstmt.setBoolean(4, obj.isAutoPromoteQueen());
			pstmt.setInt(5, obj.getAiDifficultyLevel());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}

	@Override
	public Settings update(Settings obj) {
		String requete = "UPDATE settings SET theme = ?, show_legal_moves = ?, auto_promote_queen "
			+ "= ?, ai_difficulty_level = ? WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getTheme());
			pstmt.setBoolean(2, obj.isShowLegalMoves());
			pstmt.setBoolean(3, obj.isAutoPromoteQueen());
			pstmt.setInt(4, obj.getAiDifficultyLevel());
			pstmt.setString(5, obj.getPlayerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}

	@Override
	public void delete(Settings obj) {
		String requete = "DELETE FROM settings WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getPlayerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	public Settings read(String playerId) {
		Settings s = null;
		String requete = "SELECT * FROM settings WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, playerId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				s = new Settings(rs.getString("player_id"), rs.getString("theme"),
					rs.getBoolean("show_legal_moves"), rs.getBoolean("auto_promote_queen"),
					rs.getInt("ai_difficulty_level"));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return s;
	}
}
