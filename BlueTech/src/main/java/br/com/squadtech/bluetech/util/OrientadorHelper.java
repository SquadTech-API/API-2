package br.com.squadtech.bluetech.util;

import br.com.squadtech.bluetech.dao.OrientaDAO;
import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.ProfessorDAO;
import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.model.Orienta;
import br.com.squadtech.bluetech.model.Professor;
import br.com.squadtech.bluetech.model.Usuario;

import java.util.Optional;

/** Helper centralizando obtenção de orientador/usuário associado. */
public final class OrientadorHelper {
    private static final ProfessorDAO PROFESSOR_DAO = new ProfessorDAO();
    private static final UsuarioDAO USUARIO_DAO = new UsuarioDAO();
    private static final OrientaDAO ORIENTA_DAO = new OrientaDAO();
    private static final PerfilAlunoDAO PERFIL_ALUNO_DAO = new PerfilAlunoDAO();
    private OrientadorHelper() {}

    /** Retorna o email do orientador ativo do aluno (email do professor usuário) */
    public static Optional<String> emailOrientadorAtivoDoAluno(String alunoEmail) {
        var perfil = PERFIL_ALUNO_DAO.getPerfilByEmail(alunoEmail);
        if (perfil == null) return Optional.empty();
        var orientas = ORIENTA_DAO.findByAlunoId((long) perfil.getIdPerfilAluno());
        var ativo = orientas.stream().filter(Orienta::isAtivo).findFirst();
        if (ativo.isEmpty()) return Optional.empty();
        Professor professor = PROFESSOR_DAO.findById(ativo.get().getProfessorId());
        if (professor == null) return Optional.empty();
        Usuario u = USUARIO_DAO.findByEmail(professor.getUsuarioEmail());
        return Optional.ofNullable(u).map(Usuario::getEmail);
    }

    /** Retorna o email do usuário associado ao professorId. */
    public static Optional<String> emailDoProfessorUsuario(Long professorId) {
        if (professorId == null) return Optional.empty();
        Professor p = PROFESSOR_DAO.findById(professorId);
        if (p == null) return Optional.empty();
        Usuario u = USUARIO_DAO.findByEmail(p.getUsuarioEmail());
        return Optional.ofNullable(u).map(Usuario::getEmail);
    }
}

