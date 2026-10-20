# ADR 0003 — Abertura explícita do Uber antes do preenchimento automático

- Status: aceito para a abertura inicial; preenchimento automático pendente
- Data: 2026-10-19

## Contexto

Os favoritos atuais possuem nome e endereço em texto, sem coordenadas
validadas. A documentação do Uber descreve coordenadas para definir os pontos
e trata o endereço formatado como texto de exibição. Não há evidência suficiente
para prometer preenchimento confiável usando apenas esse texto.

## Decisão

Adicionar uma abertura inicial via `uber://riderequest`, direcionada ao pacote
`com.ubercab`, sem parâmetros de viagem. A tela e a confirmação explicam que
a pessoa ainda precisa informar os endereços no Uber. Não enviar endereços
incompletos como se fossem pontos geográficos validados.

O resumo visual pode ser narrado por uma voz local em português anunciada pelo
Android como não dependente de rede. A leitura depende de toque explícito e é
interrompida ao sair, mudar o embarque ou continuar para o Uber.

## Consequências

- Há uma confirmação antes de abrir outro aplicativo.
- Nenhuma corrida, credencial ou pagamento é criado ou enviado pelo Vai Comigo.
- Falha de abertura mantém o resumo disponível enquanto a tela está em primeiro plano.
- Abertura não usa navegador, loja ou outro aplicativo como fallback automático.
- Esta etapa ainda exige digitação no Uber e não conclui a hipótese do MVP.
- O spike de deep link e a issue #7 seguem pendentes de validação física e
  preenchimento com coordenadas confirmadas.

## Próximo incremento

Definir como resolver e confirmar coordenadas dos endereços, com consentimento
para qualquer serviço externo e tratamento de resultados ambíguos. Depois,
validar o contrato do deep link em aparelho físico sem confirmar corrida real.
Essa dependência está na [issue #17](https://github.com/luis-jagella/vai-comigo/issues/17).

## Fontes

- [Deep links oficiais](https://developer.uber.com/docs/riders/ride-requests/tutorials/deep-links/introduction)
- [Aplicativo Uber na Google Play](https://play.google.com/store/apps/details?id=com.ubercab)
- [Voice — dependência de rede](https://developer.android.com/reference/android/speech/tts/Voice#isNetworkConnectionRequired())
