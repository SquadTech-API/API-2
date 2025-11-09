package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.dao.*;
import br.com.squadtech.bluetech.model.*;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.stream.Collectors;

public class VisualizarPortifolioTGController implements SupportsMainController {

    @FXML
    private FlowPane flowCards;

    @FXML
    private ComboBox<String> comboCurso;

    @FXML
    private Button btnBuscar;

    private PainelPrincipalController painelPrincipalController;

    // DAOs
    private final PerfilAlunoDAO perfilAlunoDAO = new PerfilAlunoDAO();
    private final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private final OrientaDAO orientaDAO = new OrientaDAO();
    private final ProfessorDAO professorDAO = new ProfessorDAO();
    private final TGPortifolioDAO portifolioDAO = new TGPortifolioDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    private void initialize() {
        comboCurso.getItems().addAll("Banco de Dados", "Análise de Sistemas");
        if (flowCards != null) {
            flowCards.setAlignment(Pos.CENTER); // centraliza todos os cards no FlowPane
        }
    }

    @FXML
    private void buscarPortifolio(ActionEvent event) {
        String curso = comboCurso.getValue() != null ? comboCurso.getValue() : "";
        criarCards(null, curso);
    }

    public void criarCards(String semestre, String curso) {
        if (flowCards == null) return;

        flowCards.getChildren().clear();

        List<PerfilAluno> alunos = perfilAlunoDAO.listarAlunosParaCard(null);
        if (alunos == null || alunos.isEmpty()) {
            Label aviso = new Label("Nenhum aluno encontrado para o curso selecionado.");
            aviso.getStyleClass().add("label-title");
            flowCards.getChildren().add(aviso);
            return;
        }

        for (PerfilAluno a : alunos) {
            String nomeAluno = a.getNomeAluno();

            List<Orienta> orientacoes = orientaDAO.findByAlunoId(
                    a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : -1L
            );

            String professores = "Sem professor";
            if (orientacoes != null && !orientacoes.isEmpty()) {
                professores = orientacoes.stream()
                        .map(o -> obterEmailProfessor(o.getProfessorId()))
                        .map(email -> {
                            if (email == null) return null;
                            Usuario u = usuarioDAO.findByEmail(email);
                            return u != null && u.getNome() != null ? u.getNome() : email;
                        })
                        .filter(n -> n != null && !n.isBlank())
                        .collect(Collectors.joining(", "));
                if (professores.isBlank()) professores = "Sem professor";
            }

            TGPortifolio portifolio = portifolioDAO.findByAlunoId(
                    a.getIdPerfilAluno() != null ? a.getIdPerfilAluno().longValue() : -1L
            );

            String statusPortifolio;
            if (portifolio != null) {
                statusPortifolio = portifolio.getStatus() +
                        (portifolio.getPercentualConclusao() != null ?
                                (" - " + portifolio.getPercentualConclusao() + "%") : "");
            } else if (a.getEmailUsuario() != null) {
                int qtd = tgSecaoDAO.countSecoes(a.getEmailUsuario());
                statusPortifolio = qtd > 0 ? ("Seções enviadas: " + qtd) : "Sem envios";
            } else {
                statusPortifolio = "Sem dados";
            }

            // --- FOTO DO ALUNO ---
            ImageView fotoAlunoView = new ImageView();
            fotoAlunoView.setFitHeight(80);
            fotoAlunoView.setFitWidth(80);
            fotoAlunoView.setPreserveRatio(true);
            fotoAlunoView.setSmooth(true);
            fotoAlunoView.setCache(true);

            String caminhoFoto = a.getFoto();
            Image imagem = carregarImagemPadrao();
            if (caminhoFoto != null && !caminhoFoto.isBlank()) {
                try {
                    imagem = new Image("file:" + caminhoFoto);
                    if (imagem.isError()) throw new Exception();
                } catch (Exception ex) {
                    imagem = carregarImagemPadrao();
                }
            }
            fotoAlunoView.setImage(imagem);

            Circle clip = new Circle(40, 40, 40);
            fotoAlunoView.setClip(clip);
            fotoAlunoView.getStyleClass().add("foto-aluno");

            // --- TEXTOS ---
            Label lNome = new Label("ALUNO: " + nomeAluno);
            lNome.getStyleClass().add("card-title");

            Label lProfessor = new Label("ORIENTADOR: " + professores);
            lProfessor.getStyleClass().add("subtitle");

            Label lCurso = new Label("CURSO: " + curso);
            lCurso.getStyleClass().add("subtitle");

            VBox textBox = new VBox(4, lNome, lProfessor, lCurso);
            textBox.setAlignment(Pos.CENTER_LEFT); // centraliza verticalmente ao centro do card
            HBox.setHgrow(textBox, Priority.ALWAYS);

            // --- BOTÃO VISUALIZAR (ÍCONE) ---
            Button btnEye = new Button("👁");
            btnEye.getStyleClass().add("eye-btn"); // mesmo estilo do outro card
            btnEye.setFocusTraversable(false);

            // --- CARD BOX ---
            HBox cardBox = new HBox(15, fotoAlunoView, textBox, btnEye);
            cardBox.setAlignment(Pos.CENTER_LEFT); // centraliza verticalmente todos os elementos
            cardBox.setPadding(new Insets(20));
            cardBox.getStyleClass().add("card");

            // Tamanho fixo do card
            cardBox.setPrefWidth(380);
            cardBox.setPrefHeight(190);
            cardBox.setMinWidth(380);
            cardBox.setMinHeight(190);
            cardBox.setMaxWidth(380);
            cardBox.setMaxHeight(190);

            // Eventos de clique
            cardBox.setOnMouseClicked((MouseEvent e) -> abrirVisualizador(nomeAluno, curso));
            btnEye.setOnAction(e -> abrirVisualizador(nomeAluno, curso));

            flowCards.getChildren().add(cardBox);
        }
    }

    private Image carregarImagemPadrao() {
        try {
            return new Image(getClass().getResourceAsStream("/images/Usuario.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Imagem padrão não encontrada no resources/images/Usuario.png");
            return new Image("https://cdn-icons-png.flaticon.com/512/847/847969.png"); // fallback online
        }
    }

    private void abrirVisualizador(String nomeAluno, String curso) {
        if (painelPrincipalController == null) return;
        try {
            VisualizadorTGController controller =
                    painelPrincipalController.loadContentReturnController(
                            "/fxml/professorTG/VisualizadorTG.fxml",
                            VisualizadorTGController.class
                    );
            if (controller != null) {
                controller.receberDadosAluno(nomeAluno, null, curso);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String obterEmailProfessor(Long professorId) {
        if (professorId == null) return null;
        Professor p = professorDAO.findById(professorId);
        return p != null ? p.getUsuarioEmail() : null;
    }
}
