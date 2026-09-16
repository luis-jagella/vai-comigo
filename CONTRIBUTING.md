# Como contribuir

## Fluxo de trabalho

1. Escolha ou crie uma issue com contexto e critérios de aceite.
2. Crie uma branch a partir de `main`:
   - `feat/<descricao>` para funcionalidades;
   - `fix/<descricao>` para correções;
   - `docs/<descricao>` para documentação;
   - `chore/<descricao>` para manutenção.
3. Faça commits pequenos e objetivos, preferencialmente no padrão Conventional Commits.
4. Execute `./mvnw verify` (`mvnw.cmd verify` no Windows).
5. Abra um pull request vinculado à issue.

## Definition of Done

- critérios de aceite atendidos;
- testes automatizados relevantes;
- CI verde;
- documentação atualizada;
- acessibilidade considerada na interface;
- riscos de segurança e privacidade avaliados;
- nenhum segredo ou dado pessoal no repositório.

## Revisão

Mudanças devem entrar em `main` por pull request. Durante a fase individual do projeto, o autor registra no PR as verificações executadas e mantém a branch atualizada.
