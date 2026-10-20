# Vai Comigo

Mobilidade assistida e acessível para pessoas idosas, com apoio de familiares.

> O projeto está em descoberta. A primeira meta é validar uma experiência simples e segura antes de integrar solicitações reais de corrida.

## Produto

O Vai Comigo reduz a carga de pedir uma corrida sem retirar da pessoa passageira a decisão final. A primeira versão simplifica destinos conhecidos, explica claramente o próximo passo e encaminha para o aplicativo oficial de mobilidade.

- [Visão do produto](docs/product/vision.md)
- [Glossário](docs/product/glossary.md)
- [Critérios de aceite do MVP](docs/product/mvp-acceptance-criteria.md)
- [Roadmap do MVP](docs/product/roadmap.md)
- [Como transformar entrevistas em próximos passos](docs/research/after-interview-guide.md)
- [Arquitetura inicial](docs/architecture/adr/0001-modular-monolith.md)

## Estado atual

O repositório contém a fundação do backend e a documentação inicial do produto. Nenhuma corrida real é solicitada neste estágio.

O cliente Android oferece uma tela inicial com até cinco destinos favoritos locais, cadastro, edição, exclusão com confirmação e telas de Ajuda e Configurações. Veja [como compilar e testar o aplicativo](docs/android/development.md).

A base Android e os favoritos já estão integrados à `main`. O [fluxo de embarque](docs/android/pickup.md) oferece localização com consentimento ou endereço manual e revisão da viagem. A [abertura do Uber](docs/android/uber-handoff.md) exige confirmação e oferece resumo falado com voz local, quando disponível. Os endereços ainda devem ser preenchidos no Uber. TalkBack, teste com pessoas usuárias e preenchimento automático validado em aparelho físico permanecem pendentes.

## Tecnologias

- Java 17
- Spring Boot 4.1
- Maven
- Android SDK 36 e Gradle 8.14 (cliente Android em Java)
- JUnit 5 (backend) e AndroidX Test/Espresso (Android)
- GitHub Actions

## Executar localmente

Pré-requisito: Java 17. O Maven é fornecido pelo wrapper do projeto.

```bash
./mvnw spring-boot:run
```

Em outro terminal:

```bash
curl http://localhost:8080/api/v1/status
```

## Validar

```bash
./mvnw verify
```

Para compilar e verificar o Android, configure o SDK conforme o [guia de desenvolvimento](docs/android/development.md) e execute:

```bash
cd android
./gradlew assembleDebug lintDebug testDebugUnitTest
```

No Windows, use `gradlew.bat`. Para os testes de interface, execute `connectedDebugAndroidTest` com um emulador ou aparelho conectado.

## Contribuição e segurança

- [Como contribuir](CONTRIBUTING.md)
- [Política de segurança](SECURITY.md)

## Aviso importante

Este software não deve ser usado como mecanismo de emergência ou como substituto dos recursos de segurança dos aplicativos oficiais de mobilidade.
