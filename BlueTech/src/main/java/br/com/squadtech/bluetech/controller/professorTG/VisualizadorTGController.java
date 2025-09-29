package br.com.squadtech.bluetech.controller.professorTG;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

public class VisualizadorTGController {


    @FXML
    private CheckBox cbx_professorTG_VisuAluno;

    @FXML
    private Button btn_professorTG_finalizar;


    @FXML
    void handleCheckboxAction(ActionEvent event) {
        if (cbx_professorTG_VisuAluno.isSelected()) {
            System.out.println("CheckBox 'TG João P.' foi MARCADO.");
        } else {
            System.out.println("CheckBox 'TG João P.' foi DESMARCADO.");
        }
    }


    @FXML
    void handleFinalizarButtonAction(ActionEvent event) {
        System.out.println("O botão 'Finalizar' foi clicado.");

    }


    @FXML
    void initialize() {
        System.out.println("PrimaryController foi inicializado e está pronto.");

        if (cbx_professorTG_VisuAluno.isSelected()) {
            System.out.println("O CheckBox 'TG João P.' já começou marcado.");
        }
    }
}