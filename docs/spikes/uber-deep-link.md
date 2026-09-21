# Spike: deep link do Uber no Android

## Decisão provisória

O MVP usará o deep link padrão `uber://riderequest` para encaminhar uma viagem preparada ao aplicativo oficial do Uber. O Vai Comigo não solicita nem confirma a corrida.

Essa escolha evita a necessidade de credenciais, pagamento ou acesso privilegiado à Riders API durante a descoberta. A documentação oficial do Uber descreve deep links como opção de menor esforço para abrir o aplicativo nativo com parâmetros de viagem. [Introdução aos deep links](https://developer.uber.com/docs/riders/ride-requests/tutorials/deep-links/introduction)

## Contrato implementado

`UberDeepLinkBuilder` gera um link com:

- coordenadas, apelido e endereço formatado do embarque;
- coordenadas, apelido e endereço formatado do destino;
- valores codificados para URI;
- validação de coordenadas e campos de endereço obrigatórios.

Não enviamos `product_id`: a própria documentação indica que ele depende de uma origem pré-preenchida e pode variar por localização. Também não enviamos `payment_method_id`.

## Limitações conhecidas

- No Android, as informações de destino podem só aparecer depois que a pessoa tocar em “Set Pickup Location” no Uber.
- O comportamento final depende da versão instalada do Uber, permissões e disponibilidade regional.
- Ainda é preciso validar o fluxo em dispositivo Android físico com o Uber instalado.

## Protocolo de validação manual

Usar apenas endereços fictícios ou locais públicos de teste. Não confirmar uma corrida real.

| Cenário | Resultado esperado |
| --- | --- |
| Uber instalado, origem e destino preenchidos | Uber abre no fluxo de solicitação; origem e destino ficam disponíveis para revisão. |
| Uber não instalado | O Vai Comigo informa que o Uber precisa ser instalado e oferece voltar ao início. |
| Link com caracteres acentuados | Apelido e endereço aparecem sem texto corrompido. |
| Localização indisponível | O fluxo não cria link; oferece escolher ou corrigir a origem. |
| Pessoa cancela no Uber | Nenhuma corrida é criada pelo Vai Comigo. |

Para testar por ADB, depois de instalar o Uber em um aparelho conectado:

```bash
adb shell am start -a android.intent.action.VIEW -d "<deep-link-gerado>"
```

## Critério para encerrar a issue

Registrar o resultado de todos os cenários em um dispositivo físico. Se o comportamento divergente impedir a compreensão da pessoa passageira, revisar o desenho do fluxo antes de iniciar a tela Android.
