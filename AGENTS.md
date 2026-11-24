# Orientações para implementar o app Android

- Consulte [os requisitos](docs/requisitos.md) e declare o ID do requisito na tarefa ou no PR. Implemente uma jornada verificável por vez.
- Preserve o acesso público e offline ao conteúdo básico. O aplicativo encaminha aos canais oficiais. A primeira versão dispensa cadastro, formulário de denúncias, coleta de relatos e permissões de telefone ou localização.
- Edite contatos em `app/src/main/assets/canais.json`. Associe fonte e data de conferência. Registre campos municipais não confirmados em [Validação de campo](docs/validacao-campo.md). A equipe responsável revisa os dados publicados.
- Use fixtures fictícias e substitua `openAction` e `copyPhone` nos testes de interface. Testes de intents conferem a ação e o destino sem executar chamadas ou abrir aplicativos externos.
- Rode as verificações do [README](README.md) conforme a alteração. Informe os testes executados, resultados e impedimentos reais. Atualize a evidência do requisito quando a entrega mudar seu estado.
