# Política de segurança

Os riscos e dados previstos para o MVP estão documentados no [modelo de ameaças](docs/security/threat-model.md) e no [inventário inicial de dados](docs/security/data-inventory.md).

## Reportar uma vulnerabilidade

Não publique vulnerabilidades, credenciais ou dados pessoais em issues públicas. Utilize o recurso **Private vulnerability reporting** do GitHub quando estiver habilitado no repositório.

Inclua uma descrição do impacto, passos seguros para reprodução e a versão afetada. O recebimento será confirmado assim que possível.

## Dados sensíveis

Nunca faça commit de:

- tokens de provedores de mobilidade;
- chaves de API;
- credenciais de banco de dados;
- endereços ou coordenadas reais de usuários;
- telefones, documentos ou dados de pagamento.

Use dados fictícios em testes e exemplos.
