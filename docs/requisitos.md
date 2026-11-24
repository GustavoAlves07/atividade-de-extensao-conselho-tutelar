# Requisitos e evidências

Baseado no levantamento de informações do projeto. **Implementado** indica código disponível para a jornada indicada. A execução dos testes Android deve ser registrada pela CI ou por quem rodar os comandos do README.

| ID | Requisito | Estado e evidência |
|---|---|---|
| RF01 | Acesso rápido a ajuda | Implementado. `HelpFlowTest` cobre a navegação e as três categorias. |
| RF02 | Contatos oficiais | Implementado na primeira jornada. Testes cobrem ação de telefone, cópia e fallback. A CI verifica o formato do catálogo. |
| RF03 | Orientações sobre denúncia e acolhimento | Conteúdo inicial em `guia.json`, com referências para acolhimento, anonimato e protocolo. Tela e revisão editorial planejadas. |
| RF04 | Guia sobre o Conselho e o ECA | Conteúdo inicial em `guia.json`, com artigos, FAQ e referências. Tela e revisão editorial planejadas. |
| RF05 | Diretório da rede de proteção | Catálogo inicial em `rede_protecao.json`. Integração da tela, abertura de rota e validação local pendentes. |
| RF06 | Campanhas e materiais educativos | Desejável. Testar fonte, vigência e compartilhamento quando implementado. |
| RF07 | Colaboração editorial | Processo pendente de validação com o Conselho. Usar `validacao-campo.md` e registrar aprovação de conteúdo. |
| RF08 | Sugestão de correções | Desejável. Validar canal editorial e instrução para relatos de casos. |
| RNF01 | Acesso público e privacidade | Primeira jornada sem cadastro, formulários ou permissões. Conferir o manifesto e os fluxos a cada entrega. |
| RNF02 | Acessibilidade Android | Controles com texto e áreas mínimas de toque de 48 dp. Validar TalkBack, contraste e fonte ampliada no dispositivo. |
| RNF03 | Consulta offline | Dados dos contatos estão no APK. Validar abertura a frio em modo avião. |
| RNF04 | Conteúdo atualizado | Validador exige fonte, data e estado. Confirmar responsável editorial e encaminhar pendências. |
| RNF05 | Comunicação clara | Primeira jornada identifica o serviço externo e permite copiar o telefone se a abertura falhar. |
| RNF06 | Identificação institucional | Identificação acadêmica está no Início. Tela Sobre e aprovação de materiais institucionais serão tratadas antes da publicação. |
