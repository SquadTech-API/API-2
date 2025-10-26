package br.com.squadtech.bluetech.config;

import br.com.squadtech.bluetech.dao.*;
import br.com.squadtech.bluetech.model.Usuario;

public class DatabaseInitializer {

    public static void init() {
        // Parte 0: Cria o database se não existir
        ConnectionFactory.createDatabaseIfNotExists();

        // Parte 1: Instancia todos os DAOs
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
        ProfessorDAO professorDAO = new ProfessorDAO();
        OrientaDAO orientaDAO = new OrientaDAO();
        TGPortifolioDAO tgPortifolioDAO = new TGPortifolioDAO();
        TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
        TGVersaoDAO tgVersaoDAO = new TGVersaoDAO();
        FeedbackDAO feedbackDAO = new FeedbackDAO();

        // Parte 2: Criação das tabelas na ordem correta
        // Tabelas independentes primeiro
        usuarioDAO.createTableIfNotExists();
        professorDAO.createTableIfNotExists();
        perfilAlunoDAO.createTableIfNotExists();
        tgPortifolioDAO.createTableIfNotExists();

        // Agora tabelas que dependem de outras
        tgSecaoDAO.createTableIfNotExists();   // depende de TGPortifolio
        tgVersaoDAO.createTableIfNotExists();  // depende de TGSecao
        orientaDAO.createTableIfNotExists();   // depende de Usuario (professor/aluno)
        feedbackDAO.createTableIfNotExists();  // depende de TGVersao e Usuario

        // Parte 3: Seed admin caso não existam usuários
        if (usuarioDAO.countUsuarios() == 0) {
            seedAdminInicial(usuarioDAO);
        }

        System.out.println("Banco inicializado com sucesso!");
    }

    private static void seedAdminInicial(UsuarioDAO usuarioDAO) {
        Usuario admin = new Usuario("admin@example.com", "Administrador Inicial", "senha123", "ADMIN");
        usuarioDAO.insert(admin);
        System.out.println("Admin inicial criado! Email: admin@example.com | Senha: senha123 | Mude imediatamente após login!");
    }
}
