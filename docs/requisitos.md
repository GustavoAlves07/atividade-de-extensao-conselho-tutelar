# Requisitos e evidências

Baseado no levantamento de informações do projeto. **Implementado** indica código disponível para a jornada. As liberações de conteúdo e as confirmações municipais são registradas em [Validação de campo](validacao-campo.md).

| ID | Requisito | Estado e evidência |
|---|---|---|
| RF01 | Acesso rápido a ajuda | Implementado. Início abre Ajuda e oferece atalho de urgência policial. `HelpFlowTest` cobre as três categorias e confirma que o atalho usa o mesmo número do cartão. |
| RF02 | Contatos oficiais | Implementado. Cartões apresentam fonte e data de conferência. `HelpFlowTest` cobre discador, cópia e fallback com contatos fictícios. `verifyContent` verifica o catálogo. |
| RF03 | Orientações sobre denúncia e acolhimento | Implementado. A orientação offline liga Ajuda aos artigos e apresenta acolhimento, anonimato e protocolo com referências. `ContentFlowTest` verifica a navegação nos dois sentidos. Revisão editorial da equipe pendente. |
| RF04 | Guia sobre o Conselho e o ECA | Implementado. Direitos apresenta artigos, perguntas expansíveis e referências. `ContentFlowTest` cobre leitura e volta à pergunta expandida. Revisão editorial da equipe pendente. |
| RF05 | Diretório da rede de proteção | Implementado. Rede apresenta serviços por categoria, orientações de apoio e fichas com contatos e dados disponíveis. `ContentFlowTest` verifica o destino do mapa e a ausência de campos municipais indisponíveis. Confirmações locais registradas em Validação de campo. |
| RF06 | Campanhas e materiais educativos | Implementado com exibição condicionada à aprovação e à vigência. `ContentFlowTest` verifica visibilidade, material externo e folha de compartilhamento usando fixtures. Publicação de materiais oficiais pendente. |
| RF07 | Colaboração editorial | Fluxo de revisão e registro documentado em Validação de campo. Definição do responsável e aprovação do processo pendentes com o Conselho. |
| RF08 | Sugestão de correções | Implementado em Sobre para um destino editorial aprovado. A tela direciona relatos aos canais oficiais de ajuda. `ContentFlowTest` verifica o encaminhamento com destino fictício. Canal oficial pendente. |
| RNF01 | Acesso público e privacidade | As jornadas são públicas e o manifesto dispensa permissões de telefone, localização e internet. O app encaminha aos serviços externos e dispensa cadastro e formulários de relatos. |
| RNF02 | Acessibilidade Android | Navegação, categorias e ações combinam ícones com rótulos. Controles com áreas mínimas de toque de 48 dp, cabeçalhos semânticos, seleção na navegação e estado acessível para seções expansíveis. Contraste das combinações principais de texto e fundo acima de 4,5:1. Validação com TalkBack em dispositivo pendente. |
| RNF03 | Consulta offline | O repositório lê contatos, artigos, perguntas e fichas do APK em I/O. O ViewModel expõe carregamento, falha e nova tentativa. `verifyContent` valida arquivos e vínculos. |
| RNF04 | Conteúdo atualizado | O validador exige fonte, data e estado editorial, confirma referências e verifica destinos e datas de vigência. Confirmação do responsável e pendências de dados seguem em Validação de campo. |
| RNF05 | Comunicação clara | As ações identificam o discador ou serviço externo. Os números podem ser copiados e os endereços ficam visíveis na ficha, inclusive se o mapa falhar. |
| RNF06 | Identificação institucional | Início e Sobre identificam o projeto acadêmico. Sobre apresenta o objetivo e as fontes de referência. Materiais de identidade institucional aguardam aprovação. |

## Verificação local em 23/11/2026

- Validação dos quatro arquivos de conteúdo e oito testes Python aprovados.
- Dois testes unitários Android aprovados e APK de depuração compilado com JDK 17.
- Cinco testes instrumentados aprovados no emulador Android 13, com destinos fictícios e ações externas interceptadas.
- APK aberto a frio em modo avião com fonte a 150%. Conferida a leitura do 190, da orientação do Disque 100 e do endereço do CRAS Central.
- Lint sem erros e com quatro avisos sobre versões de dependências, ícone do app e regras de backup no manifesto.

## Revisão visual em 23/11/2026

- Início, Ajuda, Direitos, Rede, fichas, orientações e Sobre revisados com a nova identidade. Ações e categorias usam ícone e texto, com destaque visual para o 190.
- Oito testes Python, dois testes unitários Android e cinco testes instrumentados aprovados. APK de depuração compilado e lint sem erros, com três avisos sobre versões de dependências e regras de backup.
- Capturas conferidas no emulador Android 13. Leitura do 190, da orientação do Disque 100 e da ficha do CRAS Central conferida em modo avião com fonte a 150%.

## Migração para Clean Architecture com MVVM em 23/11/2026

- RF01–RF06, RF08, RNF03 e RNF05. O repositório offline e as integrações Android foram separados da apresentação. O ViewModel mantém o estado, o histórico de navegação e os avisos das ações externas.
- Os cinco testes unitários passaram, incluindo restauração da navegação, nova tentativa após falha de leitura e atualização da vigência das campanhas. Os cinco testes instrumentados passaram no emulador Android 13 usando os arquivos fictícios e interceptando ações externas.
- Os oito testes Python e a validação do catálogo passaram. O APK de depuração compilou e o lint registrou zero erros e seis avisos sobre dependências e backup.
- A abertura a frio em modo avião com fonte a 150% permitiu ler o 190, a orientação do Disque 100 e o endereço do CRAS Central.
