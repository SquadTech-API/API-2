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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.stream.Collectors;

public class VisualizarPortifolioTGController implements SupportsMainController {

    @FXML
    private VBox cardsBox;

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
    }

    @FXML
    private void buscarPortifolio(ActionEvent event) {
        String curso = comboCurso.getValue() != null ? comboCurso.getValue() : "";
        criarCards(null, curso);
    }

    public void criarCards(String semestre, String curso) {
        if (cardsBox == null) {
            System.out.println("⚠️ ERRO: cardsBox não foi inicializado. Verifique o fx:id no FXML.");
            return;
        }

        cardsBox.getChildren().clear();

        List<PerfilAluno> alunos = perfilAlunoDAO.listarAlunosParaCard(null);
        if (alunos == null || alunos.isEmpty()) {
            Label aviso = new Label("Nenhum aluno encontrado para o curso selecionado.");
            aviso.getStyleClass().add("label-vazio");
            cardsBox.getChildren().add(aviso);
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
            Image imagem;

            if (caminhoFoto != null && !caminhoFoto.isBlank()) {
                try {
                    imagem = new Image("file:" + caminhoFoto);
                    if (imagem.isError()) throw new Exception();
                } catch (Exception ex) {
                    imagem = carregarImagemPadrao();
                }
            } else {
                imagem = carregarImagemPadrao();
            }

            fotoAlunoView.setImage(imagem);

            // Recorte circular
            Circle clip = new Circle(40, 40, 40);
            fotoAlunoView.setClip(clip);
            fotoAlunoView.getStyleClass().add("foto-aluno");

            // Textos
            Label t1 = new Label("ALUNO: " + nomeAluno);
            t1.getStyleClass().add("title");

            Label t2 = new Label("ORIENTADOR: " + professores);
            t2.getStyleClass().add("subtitle");

            Label t3 = new Label("CURSO: Banco de Dados");
            t3.getStyleClass().add("subtitle");

            VBox textBox = new VBox(4, t1, t2, t3);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            // Botão de visualizar
            Button eye = new Button("👁");
            eye.getStyleClass().add("eye-btn");
            eye.setFocusTraversable(false);

            HBox card = new HBox(15, fotoAlunoView, textBox, eye);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("card-item");
            card.setPadding(new Insets(12));

            // Eventos de clique
            card.setOnMouseClicked((MouseEvent e) -> abrirVisualizador(nomeAluno, curso));
            eye.setOnAction(e -> abrirVisualizador(nomeAluno, curso));

            cardsBox.getChildren().add(card);
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
