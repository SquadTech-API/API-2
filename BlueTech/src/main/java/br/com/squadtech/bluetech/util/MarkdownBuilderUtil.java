package br.com.squadtech.bluetech.util;

import br.com.squadtech.bluetech.model.TGSecao;
import br.com.squadtech.bluetech.model.TGVersao;

public final class MarkdownBuilderUtil {

    private MarkdownBuilderUtil() {
    }

    public static String buildMarkdownFromVersao(TGVersao v, TGSecao s) {
        StringBuilder sb = new StringBuilder();

        // Cabeçalho ajustado: Semestre do curso (ano-semestre do ano) | API-Empresa
        // Parceira
        sb.append("# ");
        String semestreCurso = v.getSemestre() != null ? v.getSemestre() : "—";
        sb.append(semestreCurso);
        if (v.getAno() != null) {
            sb.append(" (").append(v.getAno());
            String semestreAno = v.getSemestreAno() != null && !v.getSemestreAno().isBlank() ? v.getSemestreAno() : "—";
            sb.append("-").append(semestreAno).append(")");
        }
        sb.append(" | API-");
        String empresa = v.getEmpresa() != null ? v.getEmpresa() : "Não informado";
        sb.append(empresa);
        sb.append("\n\n");

        // Problema
        sb.append("## Problema\n");
        sb.append(v.getProblema() != null ? v.getProblema() : "Não informado");
        sb.append("\n\n");

        // Solução
        sb.append("## Solução\n");
        sb.append(v.getSolucao() != null ? v.getSolucao() : "Não informado");
        sb.append("\n\n");

        // Link do Repositório (formata como hyperlink se tiver URL)
        sb.append("## Link do Repositório\n");
        if (v.getRepositorio() != null && !v.getRepositorio().isBlank()) {
            String url = normalizeUrl(v.getRepositorio().trim());
            sb.append("[").append(url).append("](").append(url).append(")");
        } else {
            sb.append("Não informado");
        }
        sb.append("\n\n");

        // Opcional: LinkedIn (se existir)
        if (v.getLinkedin() != null && !v.getLinkedin().isBlank()) {
            sb.append("## LinkedIn\n");
            String lnk = normalizeUrl(v.getLinkedin().trim());
            sb.append("[").append(lnk).append("](").append(lnk).append(")\n\n");
        }

        // Tecnologias utilizadas
        sb.append("## Tecnologias Utilizadas\n");
        sb.append(v.getTecnologias() != null ? v.getTecnologias() : "Não informado");
        sb.append("\n\n");

        // Contribuições pessoais
        sb.append("## Contribuições Pessoais\n");
        sb.append(v.getContribuicoes() != null ? v.getContribuicoes() : "Não informado");
        sb.append("\n\n");

        // Hard Skills
        sb.append("## Hard Skills\n");
        sb.append(v.getHardSkills() != null ? v.getHardSkills() : "Não informado");
        sb.append("\n\n");

        return sb.toString();
    }

    private static String normalizeUrl(String url) {
        String u = url.trim();
        if (u.startsWith("http://") || u.startsWith("https://"))
            return u;
        return "https://" + u;
    }
}