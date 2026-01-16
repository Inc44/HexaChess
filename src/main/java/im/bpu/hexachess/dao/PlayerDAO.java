package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Player;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO extends DAO<Player> {
	private static final String CREATE =
		"INSERT INTO players (player_id, handle, email, password_hash, avatar, rating, location, "
		+ "is_verified, joined_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE =
		"UPDATE players SET handle = ?, email = ?, password_hash = ?, avatar = ?, rating = ?, "
		+ "location = ?, is_verified = ? WHERE player_id = ?";
	private static final String DELETE = "DELETE FROM players WHERE player_id = ?";
	private static final String READ = "SELECT * FROM players WHERE player_id = ?";
	private static final String READ_ALL = "SELECT * FROM players";
	private static final String GET_PLAYER_BY_HANDLE = "SELECT * FROM players WHERE handle = ?";
	private static final String SEARCH_PLAYERS = "SELECT * FROM players WHERE handle LIKE ?";
	private static final String UPDATE_PASSWORD =
		"UPDATE players SET password_hash = ? WHERE handle = ?";
	private static final String CHECK_PASSWORD =
		"SELECT password_hash FROM players WHERE handle = ?";
	@Override
	public Player create(final Player player) {
		try (final PreparedStatement pstmt = connect.prepareStatement(CREATE)) {
			pstmt.setString(1, player.getPlayerId());
			pstmt.setString(2, player.getHandle());
			pstmt.setString(3, player.getEmail());
			pstmt.setString(4, player.getPasswordHash());
			pstmt.setString(5, player.getAvatar());
			pstmt.setInt(6, player.getRating());
			pstmt.setString(7, player.getLocation());
			pstmt.setBoolean(8, player.isVerified());
			if (player.getJoinedAt() != null)
				pstmt.setTimestamp(9, Timestamp.valueOf(player.getJoinedAt()));
			else
				pstmt.setTimestamp(9, null);
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	@Override
	public Player update(final Player player) {
		try (final PreparedStatement pstmt = connect.prepareStatement(UPDATE)) {
			pstmt.setString(1, player.getHandle());
			pstmt.setString(2, player.getEmail());
			pstmt.setString(3, player.getPasswordHash());
			pstmt.setString(4, player.getAvatar());
			pstmt.setInt(5, player.getRating());
			pstmt.setString(6, player.getLocation());
			pstmt.setBoolean(7, player.isVerified());
			pstmt.setString(8, player.getPlayerId());
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	@Override
	public void delete(final Player player) {
		try (final PreparedStatement pstmt = connect.prepareStatement(DELETE)) {
			pstmt.setString(1, player.getPlayerId());
			pstmt.executeUpdate();
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
	}
	public static Player resultSetToPlayer(final ResultSet rs) throws SQLException {
		final Player player = new Player(rs.getString("player_id"), rs.getString("handle"),
			rs.getString("email"), rs.getString("password_hash"), rs.getInt("rating"),
			rs.getBoolean("is_verified"),
			rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime()
												 : null);
		player.setAvatar(rs.getString("avatar"));
		player.setLocation(rs.getString("location"));
		return player;
	}
	public Player read(final String playerId) {
		Player player = null;
		try (final PreparedStatement pstmt = connect.prepareStatement(READ)) {
			pstmt.setString(1, playerId);
			try (final ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					player = resultSetToPlayer(rs);
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	public ArrayList<Player> readAll() {
		final ArrayList<Player> players = new ArrayList<>();
		try (final Statement stmt = connect.createStatement();
			final ResultSet rs = stmt.executeQuery(READ_ALL)) {
			while (rs.next()) {
				players.add(resultSetToPlayer(rs));
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return players;
	}
	public Player getPlayerByHandle(final String handle) {
		Player player = null;
		try (final PreparedStatement pstmt = connect.prepareStatement(GET_PLAYER_BY_HANDLE)) {
			pstmt.setString(1, handle);
			try (final ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					player = resultSetToPlayer(rs);
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	public ArrayList<Player> searchPlayers(final String handle) {
		final ArrayList<Player> players = new ArrayList<>();
		try (final PreparedStatement pstmt = connect.prepareStatement(SEARCH_PLAYERS)) {
			pstmt.setString(1, "%" + handle + "%");
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
	public boolean updatePassword(final String handle, final String password) {
		try (final PreparedStatement pstmt = connect.prepareStatement(UPDATE_PASSWORD)) {
			pstmt.setString(1, password);
			pstmt.setString(2, handle);
			return pstmt.executeUpdate() > 0;
		} catch (final SQLException exception) {
			exception.printStackTrace();
			return false;
		}
	}
	public boolean checkPassword(final String handle, final String password) {
		try (final PreparedStatement pstmt = connect.prepareStatement(CHECK_PASSWORD)) {
			pstmt.setString(1, handle);
			try (final ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("password_hash").equals(password);
				}
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
		return false;
	}

	public List<Player> getLeaderboard() {
    	List<Player> players = new ArrayList<>();
		 String sql = "SELECT handle, rating FROM players ORDER BY rating DESC LIMIT 50";

    	try (PreparedStatement stmt = connect.prepareStatement(sql);
         	ResultSet rs = stmt.executeQuery()) {

        	while (rs.next()) {
            	Player p = new Player();
            	p.setHandle(rs.getString("handle"));
            	p.setRating(rs.getInt("rating"));
            	players.add(p);
        	}
    	} catch (SQLException e) {
        e.printStackTrace();
    	}
    	return players;
	}
}
