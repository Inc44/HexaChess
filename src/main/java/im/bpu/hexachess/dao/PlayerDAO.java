package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class PlayerDAO extends DAO<Player> {
	@Override
	public Player create(Player player) {
		String request =
			"INSERT INTO players (player_id, handle, email, password_hash, avatar, rating, "
			+ "location, is_verified, joined_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
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
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	@Override
	public Player update(Player player) {
		String request = "UPDATE players SET handle = ?, email = ?, password_hash = ?, avatar = ?, "
			+ "rating = ?, location = ?, is_verified = ? WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, player.getHandle());
			pstmt.setString(2, player.getEmail());
			pstmt.setString(3, player.getPasswordHash());
			pstmt.setString(4, player.getAvatar());
			pstmt.setInt(5, player.getRating());
			pstmt.setString(6, player.getLocation());
			pstmt.setBoolean(7, player.isVerified());
			pstmt.setString(8, player.getPlayerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	@Override
	public void delete(Player player) {
		String request = "DELETE FROM players WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, player.getPlayerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	private Player resultSetToPlayer(ResultSet rs) throws SQLException {
		Player player = new Player(rs.getString("player_id"), rs.getString("handle"),
			rs.getString("email"), rs.getString("password_hash"), rs.getInt("rating"),
			rs.getBoolean("is_verified"),
			rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime()
												 : null);
		player.setAvatar(rs.getString("avatar"));
		player.setLocation(rs.getString("location"));
		return player;
	}
	public Player read(String playerId) {
		Player player = null;
		String request = "SELECT * FROM players WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, playerId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				player = resultSetToPlayer(rs);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	public ArrayList<Player> readAll() {
		ArrayList<Player> players = new ArrayList<>();
		String request = "SELECT * FROM players";
		try {
			ResultSet rs = stmt.executeQuery(request);
			while (rs.next()) {
				players.add(resultSetToPlayer(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return players;
	}
	public Player getPlayerByHandle(String handle) {
		Player player = null;
		String request = "SELECT * FROM players WHERE handle = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, handle);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				player = resultSetToPlayer(rs);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return player;
	}
	public ArrayList<Player> searchPlayers(String handle) {
		ArrayList<Player> players = new ArrayList<>();
		String request = "SELECT * FROM players WHERE handle LIKE ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, "%" + handle + "%");
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				players.add(resultSetToPlayer(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return players;
	}
	public boolean updatePassword(String username, String newPassword) {
		String request = "UPDATE players SET password = ? WHERE username = ?";
		try {
			java.sql.PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, newPassword);
			pstmt.setString(2, username);
			return pstmt.executeUpdate() > 0;
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean checkPassword(String username, String passwordCandidate) {
		String request = "SELECT password FROM players WHERE username = ?";
		try {
			java.sql.PreparedStatement pstmt = connect.prepareStatement(request);
			pstmt.setString(1, username);
			java.sql.ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("password").equals(passwordCandidate);
			}
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}