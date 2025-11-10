package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.FeedbackItem;
import br.com.squadtech.bluetech.config.ConnectionFactory;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FeedbackItemDAO {

    public int save(FeedbackItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO feedback_item (feedback_id, campo, status, comentario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, item.getFeedbackId());
            pstmt.setString(2, item.getCampo());
            pstmt.setString(3, item.getStatus());
            pstmt.setString(4, item.getComentario());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating feedback item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating feedback item failed, no ID obtained.");
                }
            }
        }
    }

    // Método save original removido, pois a transação será gerenciada pelo FeedbackDAO

    public List<FeedbackItem> findByFeedbackId(int feedbackId) throws SQLException {
        String sql = "SELECT * FROM feedback_item WHERE feedback_id = ?";
        List<FeedbackItem> items = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, feedbackId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                FeedbackItem item = new FeedbackItem();
                item.setId(rs.getInt("id"));
                item.setFeedbackId(rs.getInt("feedback_id"));
                item.setCampo(rs.getString("campo"));
                item.setStatus(rs.getString("status"));
                item.setComentario(rs.getString("comentario"));
                items.add(item);
            }
        }
        return items;
    }
}
