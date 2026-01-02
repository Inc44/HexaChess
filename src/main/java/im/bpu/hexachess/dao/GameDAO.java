package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class GameDAO extends DAO<Game> {
	@Override
	public Game create(Game obj) {
		String requete = "INSERT INTO games (game_id, white_player_id, black_player_id, winner_id, "
			+ "tournament_id, moves, start_time, end_time, victory_type) VALUES(?, ?, "
			+ "?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getGameId());
			pstmt.setString(2, obj.getWhitePlayerId());
			pstmt.setString(3, obj.getBlackPlayerId());
			pstmt.setString(4, obj.getWinnerId());
			pstmt.setString(5, obj.getTournamentId());
			pstmt.setString(6, obj.getMoves());
			if (obj.getStartTime() != null)
				pstmt.setTimestamp(7, Timestamp.valueOf(obj.getStartTime()));
			else
				pstmt.setTimestamp(7, null);
			if (obj.getEndTime() != null)
				pstmt.setTimestamp(8, Timestamp.valueOf(obj.getEndTime()));
			else
				pstmt.setTimestamp(8, null);
			pstmt.setString(9, obj.getVictoryType());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}
	@Override
	public Game update(Game obj) {
		String requete = "UPDATE games SET moves = ?, end_time = ?, winner_id = ?, victory_type = "
			+ "? WHERE game_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getMoves());
			if (obj.getEndTime() != null)
				pstmt.setTimestamp(2, Timestamp.valueOf(obj.getEndTime()));
			else
				pstmt.setTimestamp(2, null);
			pstmt.setString(3, obj.getWinnerId());
			pstmt.setString(4, obj.getVictoryType());
			pstmt.setString(5, obj.getGameId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}
	@Override
	public void delete(Game obj) {
		String requete = "DELETE FROM games WHERE game_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getGameId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	public Game read(String id) {
		Game g = null;
		String requete = "SELECT * FROM games WHERE game_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				g = new Game(rs.getString("game_id"), rs.getString("white_player_id"),
					rs.getString("black_player_id"), rs.getString("winner_id"),
					rs.getString("tournament_id"), rs.getString("moves"),
					rs.getTimestamp("start_time") != null
						? rs.getTimestamp("start_time").toLocalDateTime()
						: null,
					rs.getTimestamp("end_time") != null
						? rs.getTimestamp("end_time").toLocalDateTime()
						: null,
					rs.getString("victory_type"));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return g;
	}
}
