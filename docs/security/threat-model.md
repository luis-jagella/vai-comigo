# Modelo de ameaças do MVP

## Objetivo

Antecipar riscos antes de implementar destinos, localização, apoio familiar e abertura do aplicativo de mobilidade. Este documento é uma decisão de engenharia; não substitui avaliação jurídica ou de segurança especializada.

## Limites do MVP

O Vai Comigo prepara uma viagem e encaminha a pessoa ao aplicativo oficial. Não solicita corrida, não processa pagamento e não armazena credenciais do provedor.

## Fluxo de dados previsto

```text
Pessoa passageira
  -> escolhe destino favorito armazenado no dispositivo
  -> autoriza, se desejar, a origem atual
  -> revisa origem e destino
  -> confirma a intenção de seguir
  -> aplicativo oficial recebe apenas os parâmetros necessários
```

O familiar de confiança não participa deste fluxo no MVP local. Qualquer vínculo remoto será tratado como funcionalidade nova e exigirá revisão deste modelo.

## Ativos a proteger

| Ativo | Por que importa |
| --- | --- |
| Destino favorito | Pode revelar residência, tratamento médico ou rotina. |
| Localização de embarque | Pode revelar a posição atual da pessoa. |
| Intenção de viagem | Pode indicar hábitos e horários. |
| Consentimentos | Permitem demonstrar que a pessoa escolheu usar localização ou apoio. |
| Integridade do deep link | Um link alterado pode levar ao destino errado ou a um app malicioso. |

## Ameaças e mitigação inicial

| Ameaça | Impacto | Mitigação de produto e engenharia | Prioridade | Responsável inicial |
| --- | --- | --- | --- | --- |
| Familiar usa a conta para impor uma viagem | Perda de autonomia e possível risco físico | Não criar controle remoto no MVP; exigir confirmação visível da pessoa em toda ação sensível | Alta | Produto |
| Dispositivo perdido ou emprestado | Exposição de destinos e abertura indevida de viagem | Dados locais mínimos; bloqueio do aparelho; não exibir dados de pagamento; avaliar proteção de destinos sensíveis antes do piloto | Alta | Android |
| Deep link adulterado | Destino incorreto ou redirecionamento malicioso | Construir links apenas a partir de parâmetros validados; usar esquema/domínio oficial; mostrar resumo antes de abrir | Alta | Android |
| Endereço ou coordenada em logs | Vazamento de localização | Proibir dados de localização em logs, telemetria e relatórios de erro; revisar eventos antes de publicar | Alta | Backend e Android |
| Permissão de localização concedida sem entendimento | Uso inesperado de dado sensível ao contexto | Solicitar no momento da ação, explicar em linguagem simples e oferecer alternativa manual | Alta | Android e UX |
| Pessoa entra no veículo errado | Risco físico | Exibir e narrar os dados recebidos do provedor somente quando oficialmente disponíveis; orientar conferência de veículo e placa | Alta | UX |
| App oficial ausente ou indisponível | Confusão e abandono | Mostrar mensagem clara, sem tentar simular uma corrida, e oferecer retorno ao início | Média | Android |
| Coleta excessiva em pesquisa | Exposição de participantes | Notas anonimizadas; áudios e contatos fora do repositório; retenção limitada | Alta | Pesquisa |

## Decisões obrigatórias antes do piloto

- Revisar o fluxo real de permissões de localização no Android.
- Confirmar o formato oficial do deep link em dispositivo físico.
- Definir se destinos favoritos terão proteção adicional no aparelho.
- Criar auditoria de ações sensíveis antes de qualquer apoio remoto familiar.
- Fazer revisão de privacidade e segurança do fluxo completo antes de testar com pessoas idosas.

## Gatilhos para reavaliar

Revisar este documento se o produto passar a armazenar conta, localização histórica, dados de pagamento, vínculo familiar remoto, notificações de viagem ou qualquer integração que possa solicitar/cancelar corridas.
