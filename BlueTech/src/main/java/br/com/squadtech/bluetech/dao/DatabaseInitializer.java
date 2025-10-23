package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.Usuario;

public class DatabaseInitializer {

    public static void init() {
        //Parte 0: Cria o database se não existir
        ConnectionFactory.createDatabaseIfNotExists();

        //Agora que o DB existe, podemos criar DAOs com segurança
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();

        //Parte 1: Cria tabelas se não existirem
        usuarioDAO.createTableIfNotExists();
        perfilAlunoDAO.createTableIfNotExists();

        //Parte 2: Verifica se há dados e seed admin se vazio
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