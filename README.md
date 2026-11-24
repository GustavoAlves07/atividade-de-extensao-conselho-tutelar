# Atividade de Extensão Conselho Tutelar

Aplicativo Android do projeto acadêmico de ADS da UNIASSELVI para orientar a população de Boituva sobre o Conselho Tutelar e os canais de proteção. O harness reúne instruções para contribuir, catálogo local, verificação de conteúdo, testes e CI.

O **Início** abre os canais oficiais em **Preciso de ajuda**. **Direitos** reúne orientações, artigos e dúvidas frequentes. **Rede** mostra os serviços de proteção e as fichas de atendimento. Os contatos, guias e fichas ficam disponíveis offline. O aparelho abre o discador, os links e o mapa em serviços externos. A confirmação da ligação é feita no discador.

## Ambiente

Instale o JDK 17, Python 3.9 ou mais recente e Android SDK com a plataforma 35. Para testes de interface, use um emulador ou dispositivo com depuração habilitada. Abra a raiz do projeto no Android Studio para sincronizar pelo Gradle Wrapper.

## Arquitetura

O app segue Clean Architecture com MVVM em um módulo Gradle. `domain` define o catálogo e o contrato do repositório em Kotlin. `data` lê os arquivos do APK em uma thread de I/O e converte os registros em modelos do domínio. `presentation` expõe o estado de carregamento, a navegação e os avisos pelo `CouncilViewModel`. As telas Compose observam o estado com o ciclo de vida e mantêm o estado visual local, como posição da rolagem e seções expandidas.

A `MainActivity` injeta o repositório e conecta a interface a `platform`, que traduz ações em intents ou usa a área de transferência. O histórico de navegação é restaurado pelo `SavedStateHandle`.

## Verificações

```sh
python3 -m unittest discover -s scripts -v
python3 scripts/verify_content.py app/src/main/assets/canais.json --offline \
  app/src/main/assets/guia.json app/src/main/assets/rede_protecao.json app/src/main/assets/editorial.json
./gradlew :app:verifyContent :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

O último comando requer um emulador ou dispositivo conectado. No Windows, use `gradlew.bat` e configure a variável `PYTHON` se o comando disponível for `python`. A CI executa a compilação e os testes instrumentados em jobs separados e anexa os resultados como artefatos. O APK de depuração fica em `app/build/outputs/apk/debug/`.

O catálogo de produção fica em `app/src/main/assets/canais.json`. Os testes de interface usam uma fixture do APK de teste. Para adicionar um contato, inclua ID, nome, categoria, telefone, fonte, data de conferência e estado. Use `whatsapp` com DDI e DDD e `website` com HTTPS, quando disponíveis.

O conteúdo offline fica em `app/src/main/assets/guia.json` (RF03 e RF04) e `app/src/main/assets/rede_protecao.json` (RF05). Os arquivos usam `schemaVersion` e registram fontes, data de conferência e estado editorial. `channelIds` aponta para os IDs de `canais.json`, `articleId` aponta para os artigos do guia e `serviceIds` aponta para os serviços da rede. Preencha os campos municipais opcionais somente após conferência para exibição pública, conforme a validação de campo.

O arquivo `app/src/main/assets/editorial.json` configura campanhas e o canal de correções (RF06 e RF08). Uma campanha inclui `title`, `summary` e `materialUrl`, com `startsOn` e `endsOn` opcionais no formato AAAA-MM-DD. O objeto `corrections` inclui `url`. Os dois usam links HTTPS, `sources` com `name` e `url`, e `checkedOn`. A interface exibe somente registros com `status` igual a `validado_com_orgao`, respeitando as datas da campanha em Boituva. Mantenha `campaigns` vazio e `corrections` como `null` enquanto os itens aguardam aprovação.

## Acompanhamento

- [Instruções para o Codex](AGENTS.md)
- [Requisitos e evidências](docs/requisitos.md)
- [Plano de layout das telas](docs/Plano_de_layout_Conselho_Tutelar.md)
- [Validação de campo e publicação](docs/validacao-campo.md)
