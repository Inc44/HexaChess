package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Tournament;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class TournamentDAO extends DAO<Tournament> {
	private static final String CREATE =
		"INSERT INTO tournaments (tournament_id, name, description, start_time, end_time, "
		+ "winner_id) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String UPDATE =
		"UPDATE tournaments SET name = ?, description = ?, start_time = ?, end_time = ?, winner_id "
		+ "= ? WHERE tournament_id = ?";
	private static final String DELETE = "DELETE FROM tournaments WHERE tournament_id = ?";
	private static final String READ = "SELECT * FROM tournaments WHERE tournament_id = ?";
	private static final String READ_ALL = "SELECT * FROM tournaments";
	private static final String ADD_PARTICIPANT =
		"INSERT INTO participants (tournament_id, player_id) VALUES (?, ?)";
	@Override
	public Tournament create(Tournament tournament) {
		try (PreparedStatement pstmt = connect.prepareStatement(CREATE)) {
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
		try (PreparedStatement pstmt = connect.prepareStatement(UPDATE)) {
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
		try (PreparedStatement pstmt = connect.prepareStatement(DELETE)) {
			pstmt.setString(1, tournament.getTournamentId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	private Tournament resultSetToTournament(ResultSet rs) throws SQLException {
		Tournament tournament = new Tournament(rs.getString("tournament_id"), rs.getString("name"),
			rs.getString("description"),
			rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime()
												  : null,
			rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime()
												: null,
			rs.getString("winner_id"));
		return tournament;
	}
	public Tournament read(String tournamentId) {
		Tournament tournament = null;
		try (PreparedStatement pstmt = connect.prepareStatement(READ)) {
			pstmt.setString(1, tournamentId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					tournament = resultSetToTournament(rs);
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}
	public ArrayList<Tournament> readAll() {
		ArrayList<Tournament> tournaments = new ArrayList<>();
		try (Statement stmt = connect.createStatement();
			ResultSet rs = stmt.executeQuery(READ_ALL)) {
			while (rs.next()) {
				tournaments.add(resultSetToTournament(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return tournaments;
	}
	public boolean addParticipant(String tournamentId, String playerId) {
		try (PreparedStatement pstmt = connect.prepareStatement(ADD_PARTICIPANT)) {
			pstmt.setString(1, tournamentId);
			pstmt.setString(2, playerId);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return false;
	}
	
		public java.util.ArrayList<im.bpu.hexachess.entity.Player> getParticipants(String tournamentId) {
			java.util.ArrayList<im.bpu.hexachess.entity.Player> players = new java.util.ArrayList<>();
			String sql = "SELECT p.* FROM players p " +
			             "JOIN tournament_participants tp ON p.player_id = tp.player_id " +
			             "WHERE tp.tournament_id = ?";
			
			try (java.sql.PreparedStatement pstmt = connect.prepareStatement(sql)) {
				pstmt.setString(1, tournamentId);
				try (java.sql.ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						im.bpu.hexachess.entity.Player p = new im.bpu.hexachess.entity.Player(
							rs.getString("player_id"), rs.getString("handle"), rs.getString("email"), 
							rs.getString("password_hash"), rs.getInt("rating"), rs.getBoolean("is_verified"),
							rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime() : null
						);
						p.setAvatar(rs.getString("avatar"));
						p.setLocation(rs.getString("location"));
						players.add(p);
					}
				}
			} catch (java.sql.SQLException exception) {
				exception.printStackTrace();
			}
			return players;
		}
		
}