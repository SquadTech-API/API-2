package br.com.squadtech.bluetech.config;

import br.com.squadtech.bluetech.dao.AgendamentoDefesaDAO;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.FeedbackDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.dao.TGPortifolioDAO;
import br.com.squadtech.bluetech.dao.SolicitacaoDAO;
import br.com.squadtech.bluetech.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void init() {
        // evita rodar duas vezes em diferentes entrypoints
        if (!INITIALIZED.compareAndSet(false, true)) {
            log.debug("DatabaseInitializer já executado, ignorando chamada adicional.");
            return;
        }

        //Parte 0: Cria o database se não existir
        ConnectionFactory.createDatabaseIfNotExists();

        //Agora que o DB existe, podemos criar DAOs com segurança
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
        TGVersaoDAO TGVersaoDAO = new TGVersaoDAO();
        TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        ProfessorDAO professorDAO = new ProfessorDAO();
        OrientaDAO orientaDAO = new OrientaDAO();
        TGPortifolioDAO portifolioDAO = new TGPortifolioDAO();
        SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();
        AgendamentoDefesaDAO agendamentoDefesaDAO = new AgendamentoDefesaDAO();

        //Parte 1: Cria tabelas se não existirem
        usuarioDAO.createTableIfNotExists();
        perfilAlunoDAO.createTableIfNotExists();
        TGVersaoDAO.createTableIfNotExists();
        tgSecaoDAO.createTableIfNotExists();
        professorDAO.createTableIfNotExists();
        orientaDAO.createTableIfNotExists();
        portifolioDAO.createTableIfNotExists();
        feedbackDAO.createTableIfNotExists();
        solicitacaoDAO.createTableIfNotExists();
        agendamentoDefesaDAO.createTableIfNotExists();

        //Migrações/índices auxiliares
        TGVersaoDAO.ensureSchemaUpToDate();
        tgSecaoDAO.ensureSchemaUpToDate();
        ensurePerfilAlunoUniqueEmail();

        //Parte 2: Verifica se há dados e seed admin se vazio
        if (usuarioDAO.countUsuarios() == 0) {
            seedAdminInicial(usuarioDAO);
        }

        log.info("Banco inicializado com sucesso!");
    }

    private static void ensurePerfilAlunoUniqueEmail() {
        try (Connection c = ConnectionFactory.getConnection(); Statement st = c.createStatement()) {
            try {
                st.executeUpdate("CREATE UNIQUE INDEX uk_perfil_aluno_email ON Perfil_Aluno(email_usuario)");
            } catch (Exception ignored) {
                //pode já existir
            }
        } catch (Exception e) {
            log.warn("Falha ao garantir índice único em Perfil_Aluno(email_usuario): {}", e.getMessage());
        }
    }

    private static void seedAdminInicial(UsuarioDAO usuarioDAO) {
        Usuario admin = new Usuario("admin@example.com", "Administrador Inicial", "senha123", "ADMIN");
        usuarioDAO.insert(admin);
        log.info("Admin inicial criado! Email: {} | Senha: {} | Mude imediatamente após login!", "admin@example.com", "senha123");
    }
}