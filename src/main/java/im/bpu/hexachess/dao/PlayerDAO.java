package im.bpu.hexachess.dao;

import im.bpu.hexachess.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class PlayerDAO extends DAO<Player> {
	@Override
	public Player create(Player obj) {
		String requete =
			"INSERT INTO players (player_id, handle, email, password_hash, avatar, rating, "
			+ "location, is_verified, joined_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getPlayerId());
			pstmt.setString(2, obj.getHandle());
			pstmt.setString(3, obj.getEmail());
			pstmt.setString(4, obj.getPasswordHash());
			pstmt.setString(5, obj.getAvatar());
			pstmt.setInt(6, obj.getRating());
			pstmt.setString(7, obj.getLocation());
			pstmt.setBoolean(8, obj.isVerified());

			if (obj.getJoinedAt() != null)
				pstmt.setTimestamp(9, Timestamp.valueOf(obj.getJoinedAt()));
			else
				pstmt.setTimestamp(9, null);

			pstmt.executeUpdate();

		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}

	@Override
	public Player update(Player obj) {
		String requete = "UPDATE players SET handle = ?, email = ?, password_hash = ?, avatar = ?, "
			+ "rating = ?, location = ?, is_verified = ? WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getHandle());
			pstmt.setString(2, obj.getEmail());
			pstmt.setString(3, obj.getPasswordHash());
			pstmt.setString(4, obj.getAvatar());
			pstmt.setInt(5, obj.getRating());
			pstmt.setString(6, obj.getLocation());
			pstmt.setBoolean(7, obj.isVerified());
			pstmt.setString(8, obj.getPlayerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return obj;
	}

	@Override
	public void delete(Player obj) {
		String requete = "DELETE FROM players WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, obj.getPlayerId());
			pstmt.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	private Player resultSetToPlayer(ResultSet rs) throws SQLException {
		Player p = new Player(rs.getString("player_id"), rs.getString("handle"),
			rs.getString("email"), rs.getString("password_hash"), rs.getInt("rating"),
			rs.getBoolean("is_verified"),
			rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime()
												 : null);
		p.setAvatar(rs.getString("avatar"));
		p.setLocation(rs.getString("location"));
		return p;
	}

	public Player read(String id) {
		Player p = null;
		String requete = "SELECT * FROM players WHERE player_id = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				p = resultSetToPlayer(rs);
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return p;
	}

	public ArrayList<Player> readAll() {
		ArrayList<Player> list = new ArrayList<>();
		String requete = "SELECT * FROM players";
		try {
			ResultSet rs = stmt.executeQuery(requete);
			while (rs.next()) {
				list.add(resultSetToPlayer(rs));
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return list;
	}

	public Player login(String handle, String password) {
		Player p = null;
		String requete = "SELECT * FROM players WHERE handle = ? AND password_hash = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, handle);
			pstmt.setString(2, password);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				p = resultSetToPlayer(rs);
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return p;
	}

	public Player getPlayerByHandle(String handle) {
		Player p = null;
		String requete = "SELECT * FROM players WHERE handle = ?";
		try {
			PreparedStatement pstmt = connect.prepareStatement(requete);
			pstmt.setString(1, handle);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				p = resultSetToPlayer(rs);
			}
			rs.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return p;
	}
}
