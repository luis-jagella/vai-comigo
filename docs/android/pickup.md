# Embarque com consentimento

Depois de selecionar um favorito, **Escolher embarque** abre uma tela que
explica a finalidade da localização. O app só solicita permissão quando a
pessoa toca em **Usar minha localização**. Se a permissão já estiver concedida,
a busca ainda exige esse toque explícito. **Digitar endereço de embarque**
funciona sem localização.

## Comportamento implementado

- Solicita permissões de localização precisa e aproximada juntas, somente em
  primeiro plano. Aceita a escolha aproximada sem exigir a precisa novamente.
- Usa os provedores Android disponíveis, sem dependência de Google Play Services.
- A busca termina após uma posição utilizável, cancelamento ou 30 segundos.
- Considera utilizável uma posição finita, com até dois minutos de idade e
  precisão estimada de até 100 metros. Este limite é provisório e precisa de
  validação física antes de um piloto; não garante o ponto exato de embarque.
- Revalida a idade ao revisar a viagem, somando o tempo de espera desde a
  leitura com um relógio monotônico. Se a posição expirou, descarta as
  coordenadas e oferece outra leitura ou endereço manual. A futura integração
  com o Uber também deverá conferir a validade antes de encaminhar a viagem.
- Sem sinal, permissão negada ou precisão insuficiente, oferece endereço manual
  e uma nova tentativa explícita. Não abre configurações nem pede novamente
  sem uma ação da pessoa.
- A revisão mostra embarque e destino e permite uma [abertura explícita do Uber](uber-handoff.md), ainda sem preenchimento automático. Não solicita corrida.
  Na localização automática, mostra a precisão estimada; não resolve endereço
  por geocodificação nem envia coordenadas a um serviço externo do aplicativo.

## Corrigir o endereço manual

Na revisão de um embarque digitado, **Corrigir endereço de embarque** reabre o
campo com o texto anterior, sem exigir redigitação completa nem iniciar localização.
O destino favorito não é alterado. A narração é interrompida e qualquer aviso de
falha da tentativa anterior de abrir o Uber é removido.

Depois de corrigir, é obrigatório tocar em **Revisar viagem** novamente; enquanto
o formulário está aberto, não é possível continuar no Uber. O campo continua
obrigatório, com limite de 240 caracteres. A confirmação antes de abrir o Uber
permanece necessária e os endereços ainda são preenchidos no aplicativo oficial.

**Alterar embarque** continua reiniciando a escolha do método e apagando o texto
anterior. A correção direta não aparece em revisões de localização automática.
O rascunho de correção é temporário: sair ou recriar a tela o descarta, assim como
o embarque original. Nenhum rascunho é salvo, registrado em logs ou enviado à rede.

## Ciclo de vida e privacidade

`PickupSession` mantém endereço manual ou coordenadas apenas em memória.
`PickupLocationReader` cancela listeners e timeout ao terminar a leitura,
trocar para endereço manual ou sair da tela. Callbacks atrasados são ignorados.

Ao entrar em segundo plano, finalizar ou recriar a tela, o embarque é apagado.
Voltar ao app exige uma nova escolha. Isso também ocorre após girar o aparelho;
o endereço manual de embarque não é restaurado. Não há histórico, logs de
posição, serviço em segundo plano nem inclusão do embarque no estado salvo.
O destino favorito continua no armazenamento local existente.

## Verificação automatizada

```bash
cd android
./gradlew assembleDebug lintDebug testDebugUnitTest connectedDebugAndroidTest
```

Os testes de estado cobrem consentimento, concessão, negativa, revogação,
timeout, posições antigas ou imprecisas, callbacks atrasados e descarte de dados.
Os testes de interface cobrem endereço manual, campos obrigatórios, saída para
segundo plano, negativa no diálogo real do Android e concessão com GPS fictício.
Também verificam correção com texto preenchido, preservação do destino,
validação de correções vazias, descarte após recriação e nova revisão/confirmação
antes de abrir o Uber. Os testes de estado impedem reutilizar a revisão anterior
durante a edição e rejeitam correção manual de uma revisão por localização.
O provedor de teste e a autorização de localização simulada são removidos ao
final. Use instalação de teste limpa em emulador, sem dados reais.

## Validação manual pendente

1. Conferir TalkBack e navegação com fonte em 200% em todos os estados.
2. Conceder apenas localização aproximada; se insuficiente, usar a alternativa manual.
3. Negar permanentemente a permissão e verificar que não há bloqueio da alternativa.
4. Revogar a permissão nas configurações do Android durante e após uma leitura;
   ao voltar, nenhuma posição anterior deve ser reaproveitada.
5. Desativar localização do aparelho e testar ausência de sinal até o timeout.
6. Validar o ponto físico e a linguagem com pessoas idosas antes de qualquer piloto.
7. Digitar um embarque fictício, revisar e corrigir apenas o número. Conferir com
   TalkBack e fonte ampliada se a diferença entre corrigir o texto e reiniciar o
   embarque fica clara, sem prometer preservação após sair ou girar o aparelho.

## Referências

- [Permissões de localização em tempo de execução](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime)
- [LocationManager](https://developer.android.com/reference/android/location/LocationManager)
