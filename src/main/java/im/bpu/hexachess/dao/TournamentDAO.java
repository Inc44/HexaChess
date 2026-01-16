package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Tournament;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import static im.bpu.hexachess.dao.PlayerDAO.resultSetToPlayer;

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
	private static final String GET_PARTICIPANTS =
		"SELECT p.* FROM players p JOIN participants tp ON p.player_id = tp.player_id WHERE "
		+ "tp.tournament_id = ?";
	@Override
	public Tournament create(final Tournament tournament) {
		try (final PreparedStatement pstmt = connect.prepareStatement(CREATE)) {
			pstmt.setString(1, tournament.getTournamentId());
			pstmt.setString(2, tournament.getName());
			pstmt.setString(3, tournament.getDescription());
			// Management of dates that may be null
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
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}
	@Override
	public Tournament update(final Tournament tournament) {
		try (final PreparedStatement pstmt = connect.prepareStatement(UPDATE)) {
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
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}
	@Override
	public void delete(final Tournament tournament) {
		try (final PreparedStatement pstmt = connect.prepareStatement(DELETE)) {
			pstmt.setString(1, tournament.getTournamentId());
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
	}
	private Tournament resultSetToTournament(final ResultSet rs) throws SQLException {
		final Tournament tournament = new Tournament(rs.getString("tournament_id"),
			rs.getString("name"), rs.getString("description"),
			rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime()
												  : null,
			rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime()
												: null,
			rs.getString("winner_id"));
		return tournament;
	}
	public Tournament read(final String tournamentId) {
		Tournament tournament = null;
		try (final PreparedStatement pstmt = connect.prepareStatement(READ)) {
			pstmt.setString(1, tournamentId);
			try (final ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					tournament = resultSetToTournament(rs);
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return tournament;
	}
	public ArrayList<Tournament> readAll() {
		final ArrayList<Tournament> tournaments = new ArrayList<>();
		try (final Statement stmt = connect.createStatement();
			final ResultSet rs = stmt.executeQuery(READ_ALL)) {
			while (rs.next()) {
				tournaments.add(resultSetToTournament(rs));
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return tournaments;
	}
	public boolean addParticipant(final String tournamentId, final String playerId) {
		try (final PreparedStatement pstmt = connect.prepareStatement(ADD_PARTICIPANT)) {
			pstmt.setString(1, tournamentId);
			pstmt.setString(2, playerId);
			pstmt.executeUpdate();
			return true;
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public ArrayList<Player> getParticipants(final String tournamentId) {
		final ArrayList<Player> players = new ArrayList<>();
		try (final PreparedStatement pstmt = connect.prepareStatement(GET_PARTICIPANTS)) {
			pstmt.setString(1, tournamentId);
			try (final ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					players.add(resultSetToPlayer(rs));
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return players;
	}
}