# Spike: deep link do Uber no Android

## Decisão provisória

Este spike Java/JVM constrói o deep link padrão `uber://riderequest` em memória.
Ele não abre aplicativos, não cria endpoint HTTP, não consulta serviços e não
solicita ou confirma corridas. Está isolado do cliente Android atual.

No Android, o encaminhamento existente usa `UberHandoff`, com confirmação
explícita, alternativa sem preenchimento e escolha dos pontos após consentimento.
Veja o [fluxo atual](../android/uber-handoff.md) e seus limites; o spike não o substitui.

A decisão, suas consequências e a condição para aceitá-la estão registradas no [ADR 0002](../architecture/adr/0002-uber-deep-link.md).

Essa escolha evita a necessidade de credenciais, pagamento ou acesso privilegiado à Riders API durante a descoberta. A documentação oficial do Uber descreve deep links como opção de menor esforço para abrir o aplicativo nativo com parâmetros de viagem. [Introdução aos deep links](https://developer.uber.com/docs/riders/ride-requests/tutorials/deep-links/introduction)

## Contrato implementado

`UberDeepLinkBuilder` gera um link com:

- coordenadas, apelido e endereço formatado do embarque;
- coordenadas, apelido e endereço formatado do destino;
- valores codificados para URI;
- validação de coordenadas e campos de endereço obrigatórios.

Não enviamos `product_id`: a própria documentação indica que ele depende de uma origem pré-preenchida e pode variar por localização. Também não enviamos `payment_method_id`.

Apelidos são campos desta prova de conceito. O contrato Android atual envia
coordenadas e descrições dos pontos escolhidos, sem depender deste construtor
JVM. Validação de coordenadas/texto não é validação geográfica nem consentimento.

## Limitações conhecidas

- No Android, as informações de destino podem só aparecer depois que a pessoa tocar em “Set Pickup Location” no Uber.
- O comportamento final depende da versão instalada do Uber, permissões e disponibilidade regional.
- Ainda é preciso validar o fluxo em dispositivo Android físico com o Uber instalado.

## Protocolo de validação manual

No teste físico, usar pontos públicos reconhecíveis, com autorização do proprietário
do aparelho e sem confirmar corrida real. Seguir o [roteiro Android atual](../android/uber-physical-validation.md),
que inclui consentimento, escolha dos pontos e revisão. A tabela abaixo registra
cenários para o aplicativo, não funcionalidades de abertura ou fallback do builder JVM.

| Cenário | Resultado esperado |
| --- | --- |
| Uber instalado, origem e destino preenchidos | Uber abre no fluxo de solicitação; origem e destino ficam disponíveis para revisão. |
| Uber não instalado | O cliente Android informa indisponibilidade e mantém a revisão para nova escolha. |
| Link com caracteres acentuados | Apelido e endereço aparecem sem texto corrompido. |
| Localização indisponível | O fluxo não cria link; oferece escolher ou corrigir a origem. |
| Pessoa cancela no Uber | Nenhuma corrida é criada pelo Vai Comigo. |

Não copie a URI de uma viagem pessoal para terminal, histórico, logs ou issues.
Os testes automatizados constroem e inspecionam URIs com dados de teste em memória;
isso não demonstra abertura no Uber nem correspondência real dos pontos.

## Critério para encerrar a issue

Registrar o resultado dos cenários em um dispositivo físico, sem dados pessoais.
Se a troca de aplicativo ou o preenchimento impedir a compreensão, revisar o
fluxo Android já existente. O merge da PR #14 não encerra a issue #3.
