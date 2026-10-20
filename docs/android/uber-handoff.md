# Resumo falado e abertura do Uber

Na revisão do embarque, **Ouvir resumo** lê origem, destino e próximo passo.
**Parar leitura** interrompe a voz. **Continuar no Uber** pede uma confirmação
explícita antes de tentar abrir o aplicativo.

## Limite desta entrega

O link abre o fluxo oficial, mas não preenche origem ou destino. A interface
explica essa limitação antes da confirmação. Os favoritos atuais ainda não
possuem coordenadas confirmadas. Não há geocodificação, cópia para a área de
transferência ou envio de endereços no Intent.

O [ADR 0003](../architecture/adr/0003-explicit-uber-handoff.md) registra essa
etapa e a dependência para preenchimento automático. O spike #3 e a issue #7
não estão concluídos apenas com a abertura do aplicativo.

## Voz

- Inicializa o TextToSpeech somente quando solicitado pela pessoa.
- Seleciona uma voz portuguesa instalada e anunciada pelo mecanismo como não
  dependente de rede, preferindo `pt-BR`.
- Descarta vozes com dados ainda não instalados e vozes que exigem rede.
- Se não houver voz compatível, informa a indisponibilidade e mantém o resumo visual.
- Interrompe e libera o mecanismo ao sair da tela, alterar o embarque ou abrir a confirmação.
- Não gera arquivo de áudio nem escreve o resumo em logs.

A disponibilidade e a qualidade da voz dependem do mecanismo TTS do aparelho.
O contrato de voz offline é informado por esse mecanismo; não há verificação
de tráfego de um mecanismo de terceiros pelo aplicativo.

## Abertura e retorno

O Intent usa `ACTION_VIEW`, `uber://riderequest` e o pacote `com.ubercab`.
Não há consulta de disponibilidade prévia: a tentativa de abertura trata
`ActivityNotFoundException` e `SecurityException`. Se falhar, a pessoa continua
na revisão com uma mensagem e pode tentar novamente ou voltar ao destino.

A posição é revalidada antes da confirmação e novamente ao confirmar. Ao sair
para o Uber, os dados transitórios do embarque são descartados. Ao voltar,
é necessário preparar o embarque de novo.

## Testar

```bash
cd android
./gradlew testDebugUnitTest assembleDebug lintDebug connectedDebugAndroidTest
```

Use emulador sem Uber para os testes: o sucesso é interceptado com Espresso
Intents e a falha usa a ausência real do aplicativo. Nenhuma corrida real é
iniciada. Os testes verificam cancelamento, confirmação, pacote correto,
ausência de dados da viagem no Intent e descarte ao sair durante o diálogo.
Testes de vozes sintéticas verificam seleção local, idioma e dados instalados;
eles não validam a qualidade do áudio real.

Antes de um piloto, testar em aparelho físico: áudio em português, interrupção
ao ir para segundo plano, TalkBack, fonte em 200%, Uber instalado e ausente,
retorno após cancelar e clareza do aviso de preenchimento manual. Não confirmar
uma corrida real durante a validação técnica.
