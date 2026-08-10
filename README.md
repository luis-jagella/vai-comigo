# Vai Comigo

Mobilidade assistida e acessível para pessoas idosas, com apoio de familiares.

> O projeto está em descoberta. O primeiro objetivo é validar uma experiência simples e segura antes de integrar solicitações reais de corrida.

## Problema

Aplicativos de mobilidade podem gerar insegurança em pessoas idosas por exigirem várias decisões, permissões e confirmações em uma interface pouco familiar. O Vai Comigo pretende reduzir essa carga sem retirar a autonomia da pessoa passageira.

## Primeira hipótese de produto

- destinos favoritos apresentados em botões grandes;
- origem obtida da localização atual;
- familiar autorizado pode preparar uma viagem;
- pessoa passageira revisa e confirma a ação;
- abertura do aplicativo oficial de mobilidade por deep link;
- orientações simples para conferir motorista, veículo e placa.

O produto não armazenará senha nem dados de pagamento do serviço de mobilidade.

## Estado atual

O repositório contém a fundação do backend e a documentação inicial do produto. Nenhuma corrida real é solicitada neste estágio.

## Tecnologias

- Java 17
- Spring Boot 4.1
- Maven
- JUnit 5
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

## Documentação

- [Visão do produto](docs/product/vision.md)
- [Roadmap do MVP](docs/product/roadmap.md)
- [Kit de pesquisa com usuários](docs/research/README.md)
- [Arquitetura inicial](docs/architecture/adr/0001-modular-monolith.md)
- [Como contribuir](CONTRIBUTING.md)
- [Política de segurança](SECURITY.md)

## Aviso importante

Este software ainda não deve ser utilizado como mecanismo de emergência ou como substituto dos recursos de segurança dos aplicativos oficiais de mobilidade.
