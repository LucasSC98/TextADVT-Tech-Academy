package repositorio;

import model.GameState;
import model.Save;

import java.sql.*;

public class SaveDAO {
    private static GameState gameState;

    public static void setGameState(GameState state) {
        gameState = state;
    }

    public static void salvarJogo(int idSave) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/textadventure", "root", "");

            String sql = "INSERT INTO saves (id_save, id_cena_atual, localizacao, visitou_local) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE id_cena_atual = VALUES(id_cena_atual), " +
                    "localizacao = VALUES(localizacao), visitou_local = VALUES(visitou_local)";

            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, idSave);
            stmt.setInt(2, gameState.getCenaAtual().getIdCena());
            stmt.setString(3, gameState.getLocation());
            stmt.setBoolean(4, gameState.isVisitouLocal());

            stmt.executeUpdate();

        } catch (SQLException e) {
            gameState.setMessage("Erro no save: " + e.getMessage());
            throw e;
        } finally {
            // Fechar recursos
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    public static Save carregarUltimoJogo() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Save save = null;

        try {
            conn = Mysql.getConnection();
            String sql = "SELECT * FROM saves ORDER BY id_save DESC"; // Obtém o último save salvo
            assert conn != null;
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                save = new Save();
                save.setIdSave(rs.getInt("id_save"));
                save.setCenaAtual(CenaDAO.findCenaById(rs.getInt("id_cena_atual")));
                save.setLocalizacao(rs.getString("localizacao"));
                save.setVisitouLocal(rs.getBoolean("visitou_local"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        return save;
    }

    //Cria um novo save quando digita "start"

    public static int novoSave(int idCenaAtual, String localizacao, boolean visitouLocal) {
        int novoIdSave = -1;

        String sql = "INSERT INTO saves (id_cena_atual, localizacao, visitou_local) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/textadventure", "root", "");
             PreparedStatement preparedStatement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, idCenaAtual);
            preparedStatement.setString(2, localizacao);
            preparedStatement.setBoolean(3, visitouLocal);

            int linhasAfetadas = preparedStatement.executeUpdate();


            if (linhasAfetadas > 0) {
                try (var chavesGeradas = preparedStatement.getGeneratedKeys()) {
                    if (chavesGeradas.next()) {
                        novoIdSave = chavesGeradas.getInt(1); // Obtem o novo id_save
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return novoIdSave;
    }
}