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
        String requete = "INSERT INTO players (player_id, handle, email, password_hash, rating, is_verified, joined_at) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connect.prepareStatement(requete);
            pstmt.setString(1, obj.getPlayerId());
            pstmt.setString(2, obj.getHandle());
            pstmt.setString(3, obj.getEmail());
            pstmt.setString(4, obj.getPasswordHash());
            pstmt.setInt(5, obj.getRating());
            pstmt.setBoolean(6, obj.isVerified());
            
            if (obj.getJoinedAt() != null)
                pstmt.setTimestamp(7, Timestamp.valueOf(obj.getJoinedAt()));
            else
                pstmt.setTimestamp(7, null);

            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public Player update(Player obj) {
        String requete = "UPDATE players SET handle = ?, email = ?, password_hash = ?, rating = ?, is_verified = ? WHERE player_id = ?";
        try {
            PreparedStatement pstmt = connect.prepareStatement(requete);
            pstmt.setString(1, obj.getHandle());
            pstmt.setString(2, obj.getEmail());
            pstmt.setString(3, obj.getPasswordHash());
            pstmt.setInt(4, obj.getRating());
            pstmt.setBoolean(5, obj.isVerified());
            pstmt.setString(6, obj.getPlayerId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Player read(String id) {
        Player p = null;
        String requete = "SELECT * FROM players WHERE player_id = ?";
        try {
            PreparedStatement pstmt = connect.prepareStatement(requete);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                p = new Player(
                    rs.getString("player_id"),
                    rs.getString("handle"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getInt("rating"),
                    rs.getBoolean("is_verified"),
                    rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime() : null
                );
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    public ArrayList<Player> readAll() {
        ArrayList<Player> list = new ArrayList<>();
        String requete = "SELECT * FROM players";
        try {
            ResultSet rs = stmt.executeQuery(requete);
            while (rs.next()) {
                list.add(new Player(
                    rs.getString("player_id"),
                    rs.getString("handle"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getInt("rating"),
                    rs.getBoolean("is_verified"),
                    rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime() : null
                ));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
