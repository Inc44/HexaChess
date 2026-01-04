package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Tournament;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList

public class TournamentDAO extends DAO<Tournament> {
	@Override
	public Tournament create(Tournament tournament) {
		String request = "INSERT INTO tournaments (tournament_id, name, description, start_time, "
			+ "end_time, winner_id) VALUES(?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, tournament.getTournamentId());
			pstmt.setString(2, tournament.getName());
			pstmt.setString(3, tournament.getDescription());
			// Gestion des dates pouvant être nulles
			if (tournament.getStartTime() != null)
				pstmt.setTimestamp(4, Timestamp.valueOf(tournament.getStartTime()));
			else
				pstmt.setTimestamp(4, null);
			if (tournament.getEndTime() != null)
				pstmt.setTimestamp(5, Timestamp.valueOf(tournament.getEndTime()));
			else
				pstmt.setTimestamp(5, null);
			pstmt.setString(6, tournament.getWinnerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}
	@Override
	public Tournament update(Tournament tournament) {
		String request =
			"UPDATE tournaments SET name = ?, description = ?, start_time = ?, end_time = "
			+ "?, winner_id = ? WHERE tournament_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, tournament.getName());
			pstmt.setString(2, tournament.getDescription());
			if (tournament.getStartTime() != null)
				pstmt.setTimestamp(3, Timestamp.valueOf(tournament.getStartTime()));
			else
				pstmt.setTimestamp(3, null);
			if (tournament.getEndTime() != null)
				pstmt.setTimestamp(4, Timestamp.valueOf(tournament.getEndTime()));
			else
				pstmt.setTimestamp(4, null);
			pstmt.setString(5, tournament.getWinnerId());
			pstmt.setString(6, tournament.getTournamentId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}
	@Override
	public void delete(Tournament tournament) {
		String request = "DELETE FROM tournaments WHERE tournament_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, tournament.getTournamentId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	public Tournament read(String id) {
		Tournament tournament = null;
		String request = "SELECT * FROM tournaments WHERE tournament_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				tournament = new Tournament(rs.getString("tournament_id"), rs.getString("name"),
					rs.getString("description"),
					rs.getTimestamp("start_time") != null
						? rs.getTimestamp("start_time").toLocalDateTime()
						: null,
					rs.getTimestamp("end_time") != null
						? rs.getTimestamp("end_time").toLocalDateTime()
						: null,
					rs.getString("winner_id"));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}

	public ArrayList<Tournament> readAll() {
		java.util.ArrayList<Tournament> list = new java.util.ArrayList<>();
		String request = "SELECT * FROM tournaments";
		try {
			ResultSet rs = stmt.executeQuery(request);
			while (rs.next()) {
				list.add(new Tournament(rs.getString("tournament_id"), rs.getString("name"),
					rs.getString("description"),
					rs.getTimestamp("start_time") != null
						? rs.getTimestamp("start_time").toLocalDateTime()
						: null,
					rs.getTimestamp("end_time") != null
						? rs.getTimestamp("end_time").toLocalDateTime()
						: null,
					rs.getString("winner_id")));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return list;
	}
}
