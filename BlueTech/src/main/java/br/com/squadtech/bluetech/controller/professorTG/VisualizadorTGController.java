package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.dao.PerfilAlunoDAO;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.dao.TGVersaoDAO;
import br.com.squadtech.bluetech.dao.UsuarioDAO;
import br.com.squadtech.bluetech.model.PerfilAluno;
import br.com.squadtech.bluetech.model.TGSecao;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.util.MarkdownBuilderUtil;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizadorTGController {

    private static final Logger log = LoggerFactory.getLogger(VisualizadorTGController.class);

    @FXML
    private Button btnExportar;

    @FXML
    private WebView webViewPortfolio;

    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private final TGVersaoDAO tgVersaoDAO = new TGVersaoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final Parser mdParser = Parser.builder().build();
    private final HtmlRenderer mdRenderer = HtmlRenderer.builder().build();

    private String markdownCompleto;
    private String emailAluno;

    public void receberDadosAluno(String emailAluno) {
        this.emailAluno = emailAluno;
        carregarPortfolio();
    }

    private void carregarPortfolio() {
        if (emailAluno == null) {
            renderHtml("<p>Aluno não selecionado.</p>");
            return;
        }

        Usuario usuario = usuarioDAO.findByEmail(emailAluno);
        if (usuario == null) {
            renderHtml("<p>Usuário não encontrado.</p>");
            return;
        }

        StringBuilder md = new StringBuilder();

        // 1. Header Centralizado com Badges
        PerfilAluno perfil = perfilAlunoDAO.getPerfilByEmail(emailAluno);

        md.append("<div align=\"center\">\n\n");
        md.append("# ").append(usuario.getNome()).append("\n\n");
        md.append("### Portfólio Acadêmico\n\n");

        // Badges de Contato
        md.append(generateBadge("Email", usuario.getEmail(), "D14836", "gmail")).append(" ");

        if (perfil != null) {
            if (perfil.getLinkGithub() != null && !perfil.getLinkGithub().isBlank()) {
                md.append("[").append(generateBadge("GitHub", "Profile", "181717", "github"))
                        .append("](").append(perfil.getLinkGithub()).append(") ");
            }
            if (perfil.getLinkLinkedin() != null && !perfil.getLinkLinkedin().isBlank()) {
                md.append("[").append(generateBadge("LinkedIn", "Profile", "0077B5", "linkedin"))
                        .append("](").append(perfil.getLinkLinkedin()).append(") ");
            }
        }
        md.append("\n\n</div>\n\n");
        md.append("---\n\n");

        if (perfil != null) {
            appendSection(md, "\uD83C\uDFAF Motivação", perfil.getMotivacao());

            // Conhecimentos Técnicos com Badges
            if (perfil.getConhecimentosTecnicos() != null && !perfil.getConhecimentosTecnicos().isBlank()) {
                md.append("## \uD83D\uDEE0\uFE0F Conhecimentos Técnicos\n\n");
                md.append(generateTechBadges(perfil.getConhecimentosTecnicos()));
                md.append("\n\n");
            }

            appendSection(md, "\uD83D\uDCDA Histórico Acadêmico", perfil.getHistoricoAcademico());
            appendSection(md, "\uD83D\uDCBC Histórico Profissional", perfil.getHistoricoProfissional());
        } else {
            md.append("*Perfil não preenchido.*\n\n");
        }

        md.append("---\n\n");

        // 2. Seções de API Aprovadas
        List<TGSecaoDAO.CardDados> cards = tgSecaoDAO.listarCards(emailAluno);

        // Filtrar apenas Aprovadas e Ordenar por API_Numero
        List<TGSecaoDAO.CardDados> aprovadas = cards.stream()
                .filter(c -> "Aprovado".equalsIgnoreCase(c.status))
                .sorted(Comparator.comparingInt(c -> c.apiNumero))
                .collect(Collectors.toList());

        if (aprovadas.isEmpty()) {
            md.append("## \uD83D\uDE80 Projetos de API\n\n");
            md.append("*Nenhum projeto aprovado ainda.*\n");
        } else {
            md.append("## \uD83D\uDE80 Projetos de API\n\n");
            for (TGSecaoDAO.CardDados card : aprovadas) {
                TGVersao versao = tgVersaoDAO.findById(card.idVersao);
                if (versao != null) {
                    // Tenta usar o conteúdo markdown salvo, senão constrói
                    String conteudoVersao = versao.getMarkdownContent();
                    if (conteudoVersao == null || conteudoVersao.isBlank()) {
                        TGSecao secaoTemp = tgSecaoDAO.findByIdAndEmail(card.idSecao, emailAluno);
                        conteudoVersao = MarkdownBuilderUtil.buildMarkdownFromVersao(versao, secaoTemp);
                    }

                    // REMOVER RODAPÉ (Metadados) se existir no conteúdo salvo
                    if (conteudoVersao.contains("API número:")) {
                        conteudoVersao = conteudoVersao.replaceAll("(?s)---\\s*\\nAPI número:.*", "");
                    }

                    md.append("---\n\n");
                    md.append(conteudoVersao).append("\n\n");
                }
            }
        }
        this.markdownCompleto = md.toString();
        renderMarkdown(this.markdownCompleto);
    }

    private void appendSection(StringBuilder sb, String title, String content) {
        if (content != null && !content.isBlank()) {
            sb.append("## ").append(title).append("\n\n");
            sb.append(content).append("\n\n");
        }
    }

    private String generateBadge(String label, String message, String color, String logo) {
        try {
            String encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = String.format("https://img.shields.io/badge/%s-%s-%s?style=for-the-badge&logo=%s",
                    encodedLabel, encodedMessage, color, logo);
            return String.format("![%s](%s)", label, url);
        } catch (Exception e) {
            return label + ": " + message;
        }
    }

    private String generateTechBadges(String text) {
        if (text == null || text.isBlank())
            return "";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            if (line.isBlank()) {
                sb.append("\n");
                continue;
            }

            if (isTechList(line)) {
                String[] techs = line.split("[,;]");
                for (String tech : techs) {
                    String t = tech.trim();
                    if (!t.isEmpty()) {
                        String[] style = resolveTechStyle(t);
                        sb.append(generateBadge(t, "", style[0], style[1])).append(" ");
                    }
                }
                sb.append("\n\n");
            } else {
                sb.append(line).append("\n\n");
            }
        }
        return sb.toString();
    }

    private boolean isTechList(String line) {
        String[] parts = line.split("[,;]");
        // Se for apenas um item, verifica se é uma tecnologia conhecida (tem logo
        // definido)
        if (parts.length == 1) {
            String[] style = resolveTechStyle(parts[0].trim());
            return !style[1].isEmpty();
        }
        // Se tiver múltiplos itens, verifica se não são frases longas
        for (String p : parts) {
            if (p.trim().length() > 40) {
                return false; // Provavelmente é um texto explicativo com vírgulas
            }
        }
        return true;
    }

    private String[] resolveTechStyle(String tech) {
        String t = tech.toLowerCase();
        String color = "007ACC"; // Default Blue
        String logo = "";

        if (t.contains("java") && !t.contains("script")) {
            color = "ED8B00";
            logo = "openjdk";
            if (t.contains("fx")) {
                color = "007396";
                logo = "java";
            }
        } else if (t.contains("python")) {
            color = "3776AB";
            logo = "python";
        } else if (t.contains("script") || t.equals("js")) {
            color = "F7DF1E";
            logo = "javascript";
        } else if (t.contains("html")) {
            color = "E34F26";
            logo = "html5";
        } else if (t.contains("css")) {
            color = "1572B6";
            logo = "css3";
        } else if (t.contains("sql")) {
            color = "4479A1";
            logo = "mysql";
        } else if (t.contains("spring")) {
            color = "6DB33F";
            logo = "spring";
        } else if (t.contains("react")) {
            color = "61DAFB";
            logo = "react";
        } else if (t.contains("angular")) {
            color = "DD0031";
            logo = "angular";
        } else if (t.contains("git")) {
            color = "F05032";
            logo = "git";
        } else if (t.contains("docker")) {
            color = "2496ED";
            logo = "docker";
        } else if (t.contains("intellij")) {
            color = "000000";
            logo = "intellijidea";
        } else if (t.contains("eclipse")) {
            color = "2C2255";
            logo = "eclipseide";
        } else if (t.contains("jira")) {
            color = "0052CC";
            logo = "jira";
        } else if (t.contains("vscode") || t.contains("visual studio")) {
            color = "007ACC";
            logo = "visualstudiocode";
        } else if (t.contains("maven")) {
            color = "C71A36";
            logo = "apachemaven";
        }

        return new String[] { color, logo };
    }

    private void renderMarkdown(String markdown) {
        String htmlBody = mdRenderer.render(mdParser.parse(markdown));
        // CSS aprimorado para visualização no WebView
        String css = "body{font-family: 'Segoe UI', Arial, sans-serif; padding:40px; max-width: 900px; margin: auto; line-height: 1.6; color: #333;} "
                +
                "h1{color:#2b5797; border-bottom: 2px solid #eee; padding-bottom: 10px; text-align: center;} " +
                "h2{color:#2b5797; margin-top: 30px; border-bottom: 1px solid #eee; padding-bottom: 5px;} " +
                "h3{color:#555; text-align: center; margin-top: -10px; margin-bottom: 30px;} " +
                "a{color: #0078d4; text-decoration: none;} a:hover{text-decoration: underline;} " +
                "pre{background:#f6f8fa; padding:15px; border-radius: 6px; overflow-x: auto; border: 1px solid #e1e4e8;} "
                +
                "code{font-family: Consolas, monospace; background: #f6f8fa; padding: 2px 4px; border-radius: 3px;} " +
                "blockquote{border-left: 4px solid #dfe2e5; color: #6a737d; padding-left: 15px; margin-left: 0;} " +
                "img {max-width: 100%; height: auto;} " +
                ".badge {display: inline-block; margin-right: 5px; margin-bottom: 5px;}";

        String html = "<html><head><meta charset=\"utf-8\"><style>" + css + "</style></head><body>" + htmlBody
                + "</body></html>";
        renderHtml(html);
    }

    private void renderHtml(String html) {
        try {
            String base64 = java.util.Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
            webViewPortfolio.getEngine().load("data:text/html;charset=utf-8;base64," + base64);
        } catch (Exception e) {
            log.error("Erro ao renderizar HTML", e);
        }
    }

    @FXML
    public void exportarPortfolio(ActionEvent event) {
        if (markdownCompleto == null || markdownCompleto.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Não há conteúdo para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Portfólio como README.md");
        fileChooser.setInitialFileName("README.md");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        File file = fileChooser.showSaveDialog(btnExportar.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(markdownCompleto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Portfólio exportado com sucesso!");
            } catch (IOException e) {
                log.error("Erro ao salvar arquivo", e);
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar o arquivo: " + e.getMessage());
            }
        }
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
