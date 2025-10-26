// package br.com.squadtech.bluetech.controller.professorOrientador;
//
// // 👇 IMPORTAÇÕES NECESSÁRIAS ESTÃO AQUI 👇
// import javafx.event.ActionEvent;
// import javafx.fxml.FXML;
// import javafx.scene.control.Alert;
// import javafx.scene.control.Button;
// import javafx.scene.control.TextArea;
//
// public class ControllerFeedback {
//
//     @FXML
//     private Button btn_professorTG_finalizar;
//
//     @FXML
//     private TextArea ta_professorTG_Feedback;
//
//     @FXML
//     private TextArea ta_professorTG_FeedbackVisu;
//
//     // --- Nossas novas variáveis ---
//     private FeedbackDAO feedbackDAO;
//     private TG tgAtual; // Guarda o TG que está sendo exibido na tela
//
//     @FXML
//     public void initialize() {
//         System.out.println("Controlador da Tela de Feedback inicializado.");
//         ta_professorTG_FeedbackVisu.setEditable(false);
//         ta_professorTG_FeedbackVisu.setWrapText(true);
//         ta_professorTG_Feedback.setPromptText("Por favor, insira seu feedback aqui...");
//
//         // Instancia o DAO
//         this.feedbackDAO = new FeedbackDAO();
//     }
//
//
//     @FXML
//     void finalizar(ActionEvent event) {
//         String feedbackTexto = ta_professorTG_Feedback.getText();
//
//         if (tgAtual == null) {
//             mostrarAlerta("Erro", "Nenhum TG foi carregado para dar feedback.");
//             return;
//         }
//
//         if (feedbackTexto.isEmpty()) {
//             mostrarAlerta("Aviso", "O feedback não pode estar vazio.");
//             return;
//         }
//
//         // --- LÓGICA DE SALVAR NO BANCO ---
//         try {
//             // TODO: A tela precisa de botões "Aceito" / "Ajuste"
//             String status = "AJUSTES"; // CORRIGIDO (com 'S')
//
//             // TODO: A tela precisa saber qual professor está logado
//             // Pegando o ID do professor que criamos no script SQL
//             int professorId = 1; // (ID 1 era o Aluno, ID 2 era o Professor)
//
//             // O 'versao_id' da tabela feedback é o 'id' do TG
//             int versaoId = tgAtual.getId();
//
//             // 1. Cria o objeto Feedback
//             Feedback novoFeedback = new Feedback(versaoId, professorId, status, feedbackTexto);
//
//             // 2. Manda o DAO salvar
//             feedbackDAO.salvar(novoFeedback);
//
//             // 3. Sucesso
//             mostrarAlerta("Sucesso", "Feedback salvo!");
//             ta_professorTG_Feedback.clear();
//
//             // TODO: Idealmente, fecharia esta janela após o sucesso
//
//         } catch (Exception e) {
//             e.printStackTrace(); // Imprime o erro no console
//             mostrarAlerta("Erro de Banco de Dados", "Não foi possível salvar o feedback. " + e.getMessage());
//         }
//     }
//
//     /**
//      * Recebe o objeto TG da tela anterior e exibe seu conteúdo.
//      */
//     public void exibirTG(TG tg) {
//         if (tg != null) {
//             this.tgAtual = tg; // Salva o TG atual
//
//             System.out.println("Exibindo TG: " + tg.getNomeArquivo());
//             System.out.println("Conteúdo: " + tg.getConteudo());
//
//             // Mostra o conteúdo da TG no TextArea
//             ta_professorTG_FeedbackVisu.setText(tg.getConteudo());
//         }
//     }
//
//     /**
//      * Método utilitário para mostrar alertas
//      */
//     private void mostrarAlerta(String titulo, String mensagem) {
//         Alert alert = new Alert(Alert.AlertType.INFORMATION);
//         alert.setTitle(titulo);
//         alert.setHeaderText(null);
//         alert.setContentText(mensagem);
//         alert.showAndWait();
//     }
// }
