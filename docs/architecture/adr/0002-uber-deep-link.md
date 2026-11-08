# ADR 0002 — Deep link oficial para encaminhar ao Uber

- Status: proposto
- Data: 2026-09-23

## Contexto

O MVP precisa reduzir a quantidade de passos necessários para preparar uma
viagem, sem assumir o controle da conta da pessoa passageira nem confirmar uma
corrida em seu nome.

As alternativas consideradas foram integrar a Riders API, automatizar a
interface do aplicativo do provedor ou abrir o fluxo oficial por deep link.
Integrações autenticadas e automação de interface aumentariam o tratamento de
credenciais, o risco de ações sem consentimento e o custo da validação inicial.

## Decisão

O MVP preparará origem e destino e abrirá o aplicativo oficial do Uber pelo
deep link `uber://riderequest`. A pessoa passageira continuará responsável por
revisar e confirmar a corrida no Uber.

O Vai Comigo não enviará `payment_method_id`, não armazenará credenciais do
provedor e não tentará confirmar a corrida. O uso da decisão permanece
condicionado à validação dos cenários documentados em um dispositivo Android
físico.

## Consequências

### Positivas

- mantém pagamento, autenticação e confirmação no aplicativo oficial;
- reduz os dados sensíveis tratados pelo Vai Comigo;
- permite validar o fluxo antes de investir em uma integração autenticada;
- preserva uma confirmação explícita pela pessoa passageira.

### Negativas

- o comportamento pode variar conforme versão, região e disponibilidade do Uber;
- o destino pode aparecer somente após a confirmação do ponto de embarque;
- é necessário oferecer uma saída segura quando o Uber não estiver instalado;
- mudanças no contrato de deep link exigirão nova validação.

## Validação pendente

Antes de aceitar esta decisão, executar o protocolo descrito em
[`docs/spikes/uber-deep-link.md`](../../spikes/uber-deep-link.md) com dados
fictícios em um aparelho Android. A validação não deve confirmar uma corrida
real.

## Reavaliação

Reavaliar se o deep link deixar de ser oficialmente suportado, não preservar
origem e destino de forma compreensível ou criar uma barreira relevante nos
testes com pessoas idosas.
