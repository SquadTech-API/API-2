# BlueTech - Configuração de E-mail (SMTP) e Notificações

Este guia explica como configurar o envio de e-mails pela aplicação BlueTech, utilizando variáveis de ambiente (recomendado) e como funcionam as notificações assíncronas (não bloqueiam a interface).

Se você é usuário comum (não técnico), siga apenas a seção "Passo a passo rápido (Windows)". Se você é técnico, as seções avançadas trazem detalhes e alternativas.

---

## O que você precisa
- Uma conta de e-mail com acesso SMTP (por exemplo, Gmail).
- Uma "senha de app" (App Password) se seu provedor exigir (Gmail exige quando há verificação em duas etapas). Essa senha é diferente da sua senha normal e é específica para aplicativos.

> Importante: Nunca compartilhe sua senha de app. Guarde com segurança.

---

## Como a BlueTech usa e-mail
- A BlueTech envia e-mails em dois momentos:
  1) Quando o aluno envia uma nova versão do TG, o professor orientador é notificado.
  2) Quando o professor envia um feedback, o aluno é notificado.
- O envio é feito em segundo plano (assíncrono), para não travar a tela. Você verá um aviso breve (toast) "Notificação enfileirada" e pode continuar usando o sistema.

---

## Passo a passo rápido (Windows) — usando variáveis de ambiente

1) Abra o Prompt de Comando (cmd).
2) Defina as variáveis de ambiente SMTP (substitua pelos seus dados):

```
set SMTP_HOST=smtp.gmail.com
set SMTP_PORT=587
set SMTP_USER=seu.email@gmail.com
set SMTP_PASS=sua-senha-de-app-aqui
set SMTP_FROM=seu.email@gmail.com
set SMTP_STARTTLS=true
set SMTP_SSL=false
```

3) Com o mesmo Prompt de Comando, abra a aplicação (ou execute o comando de inicialização). Como alternativa, se você usa Maven:

```
cd C:\Repos\API-2\BlueTech
mvn -DskipTests javafx:run
```

4) Pronto. Ao usar as funcionalidades de envio/feedback, os e-mails serão enviados usando a sua configuração.

> Dica: Se você fechar o Prompt, as variáveis valem apenas para aquela janela. Para definir permanentemente, procure por "Editar as variáveis de ambiente do sistema" no Windows e adicione as variáveis em "Variáveis de Usuário".

---

## Configurações SMTP explicadas
- SMTP_HOST: endereço do servidor SMTP (ex.: smtp.gmail.com).
- SMTP_PORT: porta do servidor. Para Gmail: 587 (STARTTLS) ou 465 (SSL).
- SMTP_USER: seu e-mail de login no provedor SMTP.
- SMTP_PASS: sua senha de app. Não use a senha normal da conta.
- SMTP_FROM: remetente padrão. Use o mesmo e-mail do SMTP_USER, a menos que seu provedor permita outro remetente.
- SMTP_STARTTLS: se "true", usa STARTTLS (porta 587).
- SMTP_SSL: se "true", usa SSL/TLS direto (porta 465). Use somente um (STARTTLS OU SSL).

A BlueTech lê essas variáveis quando inicia. Se algo estiver faltando, você verá avisos no log.

---

## Notificações assíncronas (não bloqueiam a tela)
- Quando uma notificação precisa ser enviada, a BlueTech apenas "enfileira" a tarefa de envio e mostra um aviso curto (toast) na tela.
- O envio acontece em segundo plano; mesmo que o servidor de e-mail esteja mais lento, sua navegação não é interrompida.
- Ao sair do sistema, a fila é encerrada com segurança.

---

## Teste rápido do envio
- No painel do Professor Orientador existe um botão "Enviar Email" para teste. Informe:
  - Para: o e-mail de destino
  - Assunto: um texto de teste
  - Mensagem: o conteúdo
- Se a configuração estiver correta, o destinatário receberá a mensagem.

> Caso não chegue:
> - Verifique as variáveis de ambiente (especialmente SMTP_USER e SMTP_PASS).
> - Se usar Gmail, confirme se a senha de app está ativa.
> - Veja se o e-mail foi para a caixa de spam.

---

## Perguntas Frequentes (FAQ)

1) Posso usar outra conta que não seja Gmail?
- Sim. Basta ajustar SMTP_HOST, SMTP_PORT, e os modos (SMTP_STARTTLS/SMTP_SSL) de acordo com o provedor.

2) É seguro colocar a senha no "set SMTP_PASS=..."?
- Sim, mas qualquer pessoa com acesso ao seu computador pode vê-la enquanto a janela estiver aberta. Para mais segurança, use variáveis de ambiente permanentes do Windows e proteja sua sessão.

3) Preciso configurar isso sempre?
- Você só precisa ajustar se mudar de conta ou de provedor SMTP. Caso as variáveis estejam definidas permanentemente, a BlueTech já lerá automaticamente ao iniciar.

4) O envio de e-mail congela a interface?
- Não. O envio é assíncrono. A interface mostra apenas um aviso curto e segue normalmente.

---

## Alternativa avançada: arquivo properties (opcional)
Agora a BlueTech também pode ler as configurações a partir do arquivo `src/main/resources/config/smtp.properties` (quando presente no classpath). ESTE ARQUIVO TEM PRIORIDADE sobre variáveis de ambiente.

1) Edite o arquivo em:
   `src/main/resources/config/smtp.properties`
2) Preencha os campos conforme seu provedor. Exemplo (Gmail):

```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=bluetechapp.fatec@gmail.com
SMTP_PASS=<sua-senha-de-app>
SMTP_FROM=bluetechapp.fatec@gmail.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

3) Compile/execute a aplicação. As configurações do arquivo serão usadas primeiro. Se algum campo ficar em branco no arquivo, a aplicação cairá para as variáveis de ambiente e, por fim, para os padrões.

> Por segurança, evite commitar `SMTP_PASS` no repositório. Você pode deixar `SMTP_PASS` vazio no arquivo e definir apenas a variável de ambiente `SMTP_PASS` na máquina de execução.

---

## Suporte
Se você tiver dificuldades, compartilhe:
- Uma captura da tela de configuração (sem mostrar a senha!).
- O log de erro que aparece no console.
- O provedor de e-mail e a porta usada.

Assim podemos orientar rapidamente.
