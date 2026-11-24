# Atividade de Extensão Conselho Tutelar

Aplicativo Android do projeto acadêmico de ADS da UNIASSELVI para orientar a população de Boituva sobre o Conselho Tutelar e os canais de proteção. O harness reúne instruções para contribuir, catálogo local, verificação de conteúdo, testes e CI.

A primeira jornada é **Início → Preciso de ajuda → escolher um canal**. Os contatos são carregados do APK e ficam disponíveis offline. As ligações passam pelo discador do aparelho e os links abrem um serviço externo. A confirmação da ligação é feita no discador.

## Ambiente

Instale o JDK 17, Python 3.9 ou mais recente e Android SDK com a plataforma 35. Para testes de interface, use um emulador ou dispositivo com depuração habilitada. Abra a raiz do projeto no Android Studio para sincronizar pelo Gradle Wrapper.

## Verificações

```sh
python3 -m unittest discover -s scripts -v
python3 scripts/verify_content.py app/src/main/assets/canais.json
./gradlew :app:verifyContent :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

O último comando requer um emulador ou dispositivo conectado. No Windows, use `gradlew.bat` e configure a variável `PYTHON` se o comando disponível for `python`. A CI executa a compilação e os testes instrumentados em jobs separados e anexa os resultados como artefatos. O APK de depuração fica em `app/build/outputs/apk/debug/`.

O catálogo de produção fica em `app/src/main/assets/canais.json`. Os testes de interface usam uma fixture do APK de teste. Para adicionar um contato, inclua ID, nome, categoria, telefone, fonte, data de conferência e estado. Use `whatsapp` com DDI e DDD e `website` com HTTPS, quando disponíveis.

## Acompanhamento

- [Instruções para o Codex](AGENTS.md)
- [Requisitos e evidências](docs/requisitos.md)
- [Validação de campo e publicação](docs/validacao-campo.md)
