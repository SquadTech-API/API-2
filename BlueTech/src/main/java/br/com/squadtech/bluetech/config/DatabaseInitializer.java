package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.Usuario;

public class DatabaseInitializer {

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final AlunoDAO alunoDAO = new AlunoDAO();
    private static final ProfessorDAO professorDAO = new ProfessorDAO();
    private static final OrientaDAO orientaDAO = new OrientaDAO();
    private static final TGPortfolioDAO tgPortfolioDAO = new TGPortfolioDAO();
    private static final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private static final TGVersaoDAO tgVersaoDAO = new TGVersaoDAO();
    private static final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private static final MensagemDAO mensagemDAO = new MensagemDAO();
    private static final DefesaDAO defesaDAO = new DefesaDAO();

    public static void init() {
        // Parte 0: Cria o database se não existir
        ConnectionFactory.createDatabaseIfNotExists();

        // Parte 1: Cria todas as tabelas
        usuarioDAO.createTableIfNotExists();
        alunoDAO.createTableIfNotExists();
        professorDAO.createTableIfNotExists();
        orientaDAO.createTableIfNotExists();
        tgPortfolioDAO.createTableIfNotExists();
        tgSecaoDAO.createTableIfNotExists();
        tgVersaoDAO.createTableIfNotExists();
        feedbackDAO.createTableIfNotExists();
        mensagemDAO.createTableIfNotExists();
        defesaDAO.createTableIfNotExists();

        // Parte 2: Verifica se há dados e seed admin se vazio
        if (usuarioDAO.countUsuarios() == 0) {
            seedAdminInicial();
        }

        System.out.println("Banco inicializado com sucesso!");
    }

    private static void seedAdminInicial() {
        Usuario admin = new Usuario("admin@example.com", "Administrador Inicial", "senha123", "ADMIN");
        usuarioDAO.insert(admin);
        System.out.println("Admin inicial criado! Email: admin@example.com | Senha: senha123 | Mude imediatamente após login!");
    }
}
