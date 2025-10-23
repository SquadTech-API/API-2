package br.com.squadtech.bluetech.controller;

import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;

/**
 * Controllers de conteúdo que desejam interagir com o PainelPrincipalController
 * podem implementar esta interface para receber a referência ao controller principal.
 */
public interface SupportsMainController {
    void setPainelPrincipalController(PainelPrincipalController controller);
}

