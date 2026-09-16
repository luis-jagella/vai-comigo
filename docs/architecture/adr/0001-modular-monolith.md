# ADR 0001 — Monólito modular para o backend inicial

- Status: aceito
- Data: 2026-08-07

## Contexto

O produto ainda precisa validar problema, experiência e integrações. A equipe não possui, neste momento, necessidade comprovada de escalabilidade independente entre componentes.

## Decisão

O backend começará como um monólito modular em Java 17 e Spring Boot. Os módulos de domínio serão separados por capacidade de negócio, mantendo dependências explícitas e evitando divisão prematura em microsserviços.

O aplicativo Android será um cliente separado dentro deste repositório quando a fase de protótipo começar. Integrações com provedores serão isoladas atrás de portas do domínio para permitir substituição e testes.

## Consequências

### Positivas

- configuração, desenvolvimento e observabilidade mais simples;
- transações e testes de integração diretos;
- menor custo operacional durante a descoberta;
- caminho claro para extrair componentes somente quando houver evidência.

### Negativas

- exige disciplina para preservar limites entre módulos;
- uma única implantação concentra as mudanças do backend;
- extrações futuras podem exigir migração de dados e contratos.

## Reavaliação

Reavaliar quando houver necessidade mensurável de escalabilidade independente, equipes autônomas por domínio ou requisitos distintos de disponibilidade.
