# Tratamento dos dados de pesquisa

## Objetivo

Reduzir o risco de exposição de participantes durante a descoberta do produto. O repositório é público e não é um local apropriado para dados brutos de pesquisa.

## Classificação

| Categoria | Exemplo | Pode ir ao GitHub? | Tratamento |
| --- | --- | --- | --- |
| Resultado agregado | “3 de 5 participantes pediram ajuda no destino” | Sim | Revisar risco de reidentificação. |
| Paráfrase anonimizada | Descrição sem pessoa, local ou viagem identificável | Sim | Agrupar com outras evidências. |
| Nota bruta | Relato individual da sessão | Não | Armazenamento privado e acesso restrito. |
| Contato | Nome, telefone ou e-mail | Não | Manter separado das respostas. |
| Dado de viagem | Endereço, coordenada, placa ou motorista | Não | Não coletar; remover imediatamente se registrado. |
| Credencial ou pagamento | Senha, token, cartão | Nunca | Interromper a coleta e não registrar. |

## Minimização

- coletar somente o que responde às perguntas do plano;
- usar códigos aleatórios para sessões;
- manter contato de recrutamento separado das notas;
- não pedir idade exata, diagnóstico, documento ou renda;
- não gravar áudio ou vídeo nesta rodada;
- revisar notas para remover identificadores antes da síntese.

## Acesso e armazenamento

- somente facilitador e observador da rodada acessam notas brutas;
- usar armazenamento privado com autenticação e controle de acesso;
- não enviar notas brutas em canais públicos ou issues;
- não copiar dados reais para ambientes de desenvolvimento ou testes.

## Retenção proposta

- excluir contatos quando não forem mais necessários para agendamento ou retorno autorizado;
- excluir notas brutas até 30 dias após a síntese aprovada;
- conservar no repositório apenas a síntese anonimizada;
- registrar exceções e justificativas antes de ampliar qualquer prazo.

O prazo é uma decisão operacional inicial e deve ser revisto caso a pesquisa passe a ter requisitos contratuais, institucionais ou jurídicos específicos.

## Resposta a incidente

Se um dado identificável for publicado ou compartilhado indevidamente:

1. interromper novos compartilhamentos;
2. restringir o acesso ao material;
3. remover o dado do local exposto sem reproduzi-lo em outra issue;
4. registrar o incidente por canal privado;
5. avaliar impacto, pessoas afetadas e ações necessárias;
6. revisar o processo para evitar recorrência.
