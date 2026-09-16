# Visão do produto

## Propósito

Dar mais autonomia e segurança a pessoas idosas ao preparar uma viagem por aplicativo, mantendo familiares de confiança disponíveis quando necessário.

## Problema

Pedir uma corrida envolve encontrar o destino, entender opções, conferir informações e confirmar uma ação que pode gerar custo. Para muitas pessoas idosas, essa sequência causa insegurança e dependência de familiares.

## Pessoas principais

### Pessoa passageira

Quer chegar a um destino conhecido sem precisar compreender todas as opções do aplicativo de mobilidade.

### Familiar de confiança

Quer ajudar quando solicitado, sem assumir silenciosamente o controle da conta ou das decisões da pessoa passageira.

## Proposta

O Vai Comigo será uma camada simples de preparação da viagem. A pessoa escolhe um destino conhecido, revisa um resumo claro e segue para o aplicativo oficial de mobilidade para a confirmação final.

## Princípios do MVP

1. **Autonomia com consentimento:** a pessoa sabe o que acontecerá e confirma ações sensíveis.
2. **Simplicidade:** cada tela apresenta uma decisão principal.
3. **Segurança visível:** destino, veículo e próximos passos devem estar claros.
4. **Privacidade por padrão:** coletar somente o necessário e definir prazo de retenção.
5. **Integração responsável:** usar fluxos oficiais dos provedores de mobilidade.
6. **Acessibilidade desde o início:** contraste, tamanho de toque, linguagem e leitor de tela são critérios de aceite.

## Hipótese do MVP

Se oferecermos destinos favoritos, uma confirmação simples e apoio opcional de um familiar, a pessoa idosa conseguirá iniciar a solicitação no aplicativo oficial com menos medo e menos erros.

## Fluxo inicial

```text
Escolher destino conhecido
        -> revisar origem e destino
        -> confirmar intenção
        -> abrir aplicativo oficial
        -> confirmar a corrida no provedor
```

## Dentro do MVP

- perfil local da pessoa passageira;
- até cinco destinos favoritos;
- localização de embarque com consentimento;
- resumo falado e visual da intenção de viagem;
- abertura do Uber com dados pré-preenchidos, quando oficialmente suportado;
- contato rápido com familiar;
- telemetria sem dados sensíveis para medir conclusão e abandono.

## Fora do MVP

- solicitar ou pagar uma corrida sem confirmação no aplicativo oficial;
- armazenar cartão, senha ou token do provedor;
- substituir recursos de emergência;
- acompanhar localização continuamente sem consentimento explícito;
- prometer suporte a mais de um provedor antes de validar o primeiro fluxo.

## Métricas iniciais

- percentual de tentativas que chegam ao aplicativo oficial;
- tempo mediano entre escolher o destino e abrir o aplicativo oficial;
- quantidade de pedidos de ajuda por tentativa;
- sucesso em testes de usabilidade com pessoas idosas;
- incidentes de privacidade ou segurança: meta zero.
