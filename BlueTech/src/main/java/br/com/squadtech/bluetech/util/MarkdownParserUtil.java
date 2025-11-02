package br.com.squadtech.bluetech.util;

import br.com.squadtech.bluetech.model.TGVersao;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarkdownParserUtil {
    private MarkdownParserUtil() {}

    // Regex ajustado para novo header: "# <SemestreCurso> (<Ano>-<SemestreAno>) | API-<Empresa>"
    private static final Pattern HEADER_PATTERN = Pattern.compile("^#\\s*(.+?)\\s*\\((\\d{4})-([12])\\)\\s*\\|\\s*API-(.+?)$", Pattern.CASE_INSENSITIVE);
    // Regex para headings de seção: "## Título"
    private static final Pattern HEADING_PATTERN = Pattern.compile("^##\\s*(.+?)\\s*$");
    // Regex para markdown link: [text](url)
    private static final Pattern MD_LINK_PATTERN = Pattern.compile("\\[.*?\\]\\((.*?)\\)");

    public static void parseMarkdownIntoFields(TGVersao v, String markdown) {
        if (v == null || markdown == null) return;
        String normalized = markdown.replace("\r", "");
        String[] lines = normalized.split("\n");

        // 1) Novo header: # <SemestreCurso> (<Ano>-<SemestreAno>) | API-<Empresa>
        for (String line : lines) {
            Matcher m = HEADER_PATTERN.matcher(line.trim());
            if (m.matches()) {
                v.setSemestre(m.group(1).trim());
                try {
                    v.setAno(Integer.parseInt(m.group(2).trim()));
                } catch (Exception ignore) {}
                v.setSemestreAno(m.group(3).trim());
                v.setEmpresa(m.group(4).trim());
                break;
            }
        }

        // 2) Split por headings e interromper ao encontrar rodapé/meta (--- ou linhas de meta)
        Map<String, StringBuilder> secMap = new HashMap<>();
        String current = null;
        boolean metaReached = false;
        for (String rawLine : lines) {
            String t = rawLine.trim();
            if (t.equals("---") || t.startsWith("API número:") || t.startsWith("Versão:") || t.startsWith("Data da versão:")) {
                metaReached = true;
                current = null;
                continue;
            }
            if (metaReached) continue; // ignora tudo após meta

            Matcher h = HEADING_PATTERN.matcher(t);
            if (h.matches()) {
                current = h.group(1).trim().toLowerCase();
                secMap.putIfAbsent(current, new StringBuilder());
            } else if (current != null) {
                secMap.get(current).append(rawLine).append("\n");
            }
        }

        // 3) Normalizar e aplicar nos campos do TGVersao (sem empresa, pois agora no header)
        v.setProblema(extract(secMap, "problema"));
        v.setSolucao(extract(secMap, "solução", "solucao"));

        String repo = extract(secMap, "link do repositório", "link do repositorio");
        v.setRepositorio(extractUrl(repo));

        String linkedin = extract(secMap, "linkedin");
        v.setLinkedin(extractUrl(linkedin));

        v.setTecnologias(extract(secMap, "tecnologias utilizadas"));
        v.setContribuicoes(extract(secMap, "contribuições pessoais", "contribuicoes pessoais"));
        v.setHardSkills(extract(secMap, "hard skills"));
        v.setSoftSkills(extract(secMap, "soft skills"));
    }

    public static void safeParseIntoFields(TGVersao v, String markdown) {
        if (v == null || markdown == null) return;
        String normalized = markdown.replace("\r", "");
        String[] lines = normalized.split("\n");

        // Parse do header (atualiza apenas se matcher der match; senão, mantém antigo)
        for (String line : lines) {
            Matcher m = HEADER_PATTERN.matcher(line.trim());
            if (m.matches()) {
                v.setSemestre(m.group(1).trim());
                try {
                    v.setAno(Integer.parseInt(m.group(2).trim()));
                } catch (Exception ignore) {}
                v.setSemestreAno(m.group(3).trim());
                v.setEmpresa(m.group(4).trim());
                break;  // Atualiza apenas se encontrou; senão, mantém copiado
            }
        }

        // Split por headings
        Map<String, StringBuilder> secMap = new HashMap<>();
        String current = null;
        boolean metaReached = false;
        for (String rawLine : lines) {
            String t = rawLine.trim();
            if (t.equals("---") || t.startsWith("API número:") || t.startsWith("Versão:") || t.startsWith("Data da versão:")) {
                metaReached = true;
                current = null;
                continue;
            }
            if (metaReached) continue;

            Matcher h = HEADING_PATTERN.matcher(t);
            if (h.matches()) {
                current = h.group(1).trim().toLowerCase();
                secMap.putIfAbsent(current, new StringBuilder());
            } else if (current != null) {
                secMap.get(current).append(rawLine).append("\n");
            }
        }

        // Atualiza apenas se extrair valor válido; senão, mantém copiado
        String problema = extract(secMap, "problema");
        if (problema != null && !problema.isBlank()) v.setProblema(problema);

        String solucao = extract(secMap, "solução", "solucao");
        if (solucao != null && !solucao.isBlank()) v.setSolucao(solucao);

        String repo = extract(secMap, "link do repositório", "link do repositorio");
        String repoUrl = extractUrl(repo);
        if (repoUrl != null && !repoUrl.isBlank()) v.setRepositorio(repoUrl);

        String linkedin = extract(secMap, "linkedin");
        String linkedinUrl = extractUrl(linkedin);
        if (linkedinUrl != null && !linkedinUrl.isBlank()) v.setLinkedin(linkedinUrl);

        String tecnologias = extract(secMap, "tecnologias utilizadas");
        if (tecnologias != null && !tecnologias.isBlank()) v.setTecnologias(tecnologias);

        String contribuicoes = extract(secMap, "contribuições pessoais", "contribuicoes pessoais");
        if (contribuicoes != null && !contribuicoes.isBlank()) v.setContribuicoes(contribuicoes);

        String hardSkills = extract(secMap, "hard skills");
        if (hardSkills != null && !hardSkills.isBlank()) v.setHardSkills(hardSkills);

        String softSkills = extract(secMap, "soft skills");
        if (softSkills != null && !softSkills.isBlank()) v.setSoftSkills(softSkills);
    }

    private static String extract(Map<String, StringBuilder> map, String... keys) {
        for (String k : keys) {
            StringBuilder sb = map.get(k);
            if (sb != null) {
                String s = sb.toString().trim();
                return trimTrailingNewlines(removeMetaLines(s));
            }
        }
        return null;
    }

    private static String removeMetaLines(String s) {
        String[] ls = s.split("\n");
        StringBuilder out = new StringBuilder();
        for (String raw : ls) {
            String t = raw.trim();
            if (t.equals("---") || t.startsWith("API número:") || t.startsWith("Versão:") || t.startsWith("Data da versão:")) {
                // descarta linhas de meta se por acaso entraram
                continue;
            }
            out.append(raw).append("\n");
        }
        return out.toString();
    }

    private static String trimTrailingNewlines(String s) {
        int i = s.length();
        while (i > 0 && (s.charAt(i - 1) == '\n' || s.charAt(i - 1) == '\r')) i--;
        return s.substring(0, i);
    }

    private static String extractUrl(String s) {
        if (s == null || s.isBlank()) return s;
        Matcher m = MD_LINK_PATTERN.matcher(s);
        if (m.find()) {
            return m.group(1).trim();
        }
        return s.trim();
    }
}