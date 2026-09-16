# Inventário inicial de dados

## Objetivo

Registrar o mínimo de dados necessário para o MVP e deixar explícito o que não deve ser coletado. A LGPD estabelece o princípio da necessidade: o tratamento deve se limitar ao mínimo pertinente, proporcional e não excessivo para a finalidade. Consulte a [Lei nº 13.709/2018](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm) e os materiais da [ANPD](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes) antes de qualquer lançamento.

> Este inventário é um artefato de produto e engenharia, não um parecer jurídico. A base legal e os prazos definitivos precisam de validação apropriada antes do piloto.

## Dados no MVP local

| Dado | Finalidade | Onde fica | Acesso | Retenção proposta | Decisão de privacidade |
| --- | --- | --- | --- | --- | --- |
| Nome amigável do destino | Permitir escolha simples, como “Casa” | Dispositivo | Pessoa passageira | Até remoção pela pessoa | Não enviar ao backend no MVP. |
| Endereço/coordenada do destino | Preparar a viagem no provedor oficial | Dispositivo e memória do fluxo | Pessoa passageira | Até remoção; memória apenas durante o fluxo | Não registrar em logs. |
| Localização atual de embarque | Preencher a origem quando a pessoa desejar | Memória do fluxo | Pessoa passageira | Somente durante a preparação | Solicitar permissão contextual; não persistir. |
| Estado da permissão | Saber se é possível oferecer origem atual | Dispositivo | Aplicativo | Conforme sistema operacional | Explicar a finalidade antes da solicitação. |
| Evento técnico agregado | Medir conclusão ou erro sem identificar alguém | Futuro backend, somente após revisão | Equipe autorizada | Prazo a definir antes do piloto | Não incluir endereço, coordenada, telefone ou identificador de conta. |

## Dados explicitamente proibidos neste estágio

- senha, token ou sessão do provedor de mobilidade;
- cartão, conta bancária ou histórico de pagamento;
- localização histórica;
- áudio, notas brutas ou contato de participantes de pesquisa no repositório;
- identificação de motorista, placa ou detalhes de corrida real em testes;
- dados de saúde ou documentos.

## Pendências para funcionalidade de apoio familiar

Antes de criar contas, convites ou notificações, definir e documentar:

1. finalidade específica e base legal aplicável;
2. quem é controlador e quais terceiros atuam como operadores;
3. mecanismo de convite, consentimento, revogação e auditoria;
4. prazo de retenção para cada categoria de dado;
5. procedimento para atender direitos da pessoa titular e responder a incidentes.

## Controles mínimos

- Controle de acesso por padrão de menor privilégio.
- Segredos apenas em gerenciador apropriado, nunca no repositório.
- Dados reais proibidos em testes, exemplos e logs.
- Revisão de eventos de telemetria antes de habilitá-los.
- Processo documentado de remoção e resposta a incidentes.
