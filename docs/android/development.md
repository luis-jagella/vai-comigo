# Desenvolvimento Android

O módulo `android/` é um cliente Java independente do backend Maven. Esta
versão contém navegação entre Destinos, Ajuda e Configurações, além de cadastro,
edição, exclusão com confirmação e seleção de até cinco destinos favoritos.
O [fluxo de embarque com consentimento](pickup.md) permite usar localização
ou informar endereço manual e revisar a viagem. A integração com Uber continua
na issue #7; a [abertura inicial com resumo falado](uber-handoff.md) já permite
continuar no app oficial, com [busca e escolha opcional dos pontos](geocoding.md). O teste físico
do deep link permanece na issue #3.

O embarque manual pode ser corrigido a partir da revisão com o texto preenchido,
sem alterar o destino. É necessário revisar novamente antes de continuar; veja
o [comportamento e os limites de privacidade](pickup.md#corrigir-o-endereço-manual).

## Compilar

Pré-requisitos: JDK 17, Android SDK Platform 36 e Build Tools 35.0.0. Defina
`ANDROID_HOME` para o SDK ou configure `android/local.properties` localmente
(o arquivo é ignorado pelo Git). O Gradle é fornecido pelo wrapper.

```bash
cd android
./gradlew assembleDebug lintDebug testDebugUnitTest
```

No Windows, use `gradlew.bat`. O APK fica em
`android/app/build/outputs/apk/debug/app-debug.apk`.

O debug é identificado como **Vai Comigo (teste)**, versão `0.1.0-demo.1`.
O CI disponibiliza APK e SHA-256 após os testes Android. Veja o
[guia da demonstração](demo.md) para download, instalação, cuidados com chaves
debug e roteiro seguro. Essa entrega não encerra a validação física do MVP.

## Testes de navegação

Com um emulador ou aparelho conectado por ADB:

```bash
./gradlew connectedDebugAndroidTest
```

Os testes instrumentados exercitam as telas de ajuda e configurações, o
retorno a destinos, os alvos de toque, o armazenamento e o fluxo completo de
favoritos (incluindo cancelamento da exclusão e restauração do formulário).
A integração contínua compila o APK,
executa lint e esses testes em um emulador.

## Baseline de acessibilidade

- Botões com texto visível e altura mínima de 64 dp.
- Texto em sp, acompanhando o tamanho de fonte do sistema.
- Telas com rolagem, sem alturas fixas para o conteúdo.
- Texto escuro sobre fundo claro; informação não depende apenas de cor.
- Margens respeitam barras do sistema e recortes de tela.
- Configurações oferece acesso à acessibilidade do celular.

Antes de encerrar a issue #4, validar manualmente no aparelho: leitura e ordem
de foco com TalkBack, fonte ampliada em 200%, navegação em orientação horizontal
e contraste dos estados dos botões. Registrar dispositivo, versão Android,
resultados e problemas encontrados. Compilação e lint não substituem esse teste.

## Favoritos locais

Na tela inicial, toque em **Adicionar lugar**, informe nome e endereço e salve.
O cartão do lugar permite selecioná-lo e revisar o endereço. **Organizar meus
lugares** permite editar ou excluir. A exclusão exige confirmação.

Os nomes têm até 40 caracteres e os endereços até 240. Não há busca nem
validação geográfica nesta etapa: o endereço é texto informado pela pessoa.
A seleção ainda não inicia uma corrida.

Tela e modelo removem espaços das bordas, incluindo espaços Unicode como NBSP,
espaço em e espaço não separável estreito, antes de validar os limites. Um nome
ou endereço composto somente por esses espaços é rejeitado. Espaços internos
são preservados. Isso não é validação geográfica nem um filtro de todos os
caracteres invisíveis (por exemplo, espaço de largura zero não faz parte dessa regra).

`FavoriteStore` armazena até cinco registros com identificador estável em
SharedPreferences privadas (`favorites`). Não há envio para backend, geocodificador
ou telemetria. O manifesto desabilita backup e as regras de extração excluem
dados de backup em nuvem e transferência de aparelho. Não há criptografia
adicional implementada pelo aplicativo. Remover o app remove seus favoritos.

Dados inválidos no armazenamento geram uma mensagem de erro e não são
sobrescritos silenciosamente, inclusive quando a preferência tem um tipo
incorreto. **Tentar ler meus lugares novamente** repete a leitura sem alterar
o conteúdo. A Ajuda continua disponível nessa tela.

Cada item do JSON local exige `id`, `name` e `address` como strings. Números,
booleanos, `null`, objetos, listas e campos ausentes são rejeitados, em vez de
convertidos em texto. Textos como `"42"` e `"null"` continuam sendo strings
válidas quando atendem às regras do modelo. O formato dos registros válidos
não mudou e não exige migração. Identidades duplicadas e coleções com mais de
cinco lugares também são consideradas ilegíveis.

Uma coleção ilegível bloqueia leitura, cadastro, edição e remoção individual,
sem sobrescrever o conteúdo original ou alterar outras preferências. Não há
recuperação parcial automática: a ação de apagar os lugares continua exigindo
a confirmação descrita abaixo. Isso valida estrutura e campos locais, não a
existência geográfica de um endereço.

Se o problema persistir, **Apagar dados dos lugares e recomeçar** oferece uma
saída destrutiva com confirmação explícita: todos os lugares antigos serão
perdidos, sem desfazer ou recuperação parcial. Cancelar, sair ou recriar a
tela não confirma a exclusão. O app verifica novamente se os favoritos são
ilegíveis antes de remover apenas a chave `destinations`; uma coleção que
voltou a ser válida é preservada, assim como outras preferências. Depois da
remoção, é possível cadastrar os lugares novamente. Nada é enviado a serviços
externos e o conteúdo ilegível não é registrado em logs.

### Roteiro de teste

1. Cadastre `Casa de teste` em `Rua Fictícia, 123, Cidade Exemplo`.
2. Feche e reabra o app; confirme a presença do cartão.
3. Selecione o lugar e confira o endereço.
4. Edite o nome e confirme a atualização do mesmo favorito.
5. Tente excluir, cancele e confira que o cartão permanece; depois confirme.
6. Cadastre cinco lugares; o sexto deve ser bloqueado, mas a edição deve funcionar.
7. Repita com fonte em 200% e TalkBack, verificando rótulos, rolagem e erros.

### Testar recuperação sem dados reais

`FavoriteRecoveryTest` usa um emulador de desenvolvimento e dados fictícios
para verificar tentativa de leitura, cancelamento, recriação da tela, confirmação
após tipo incorreto, novo cadastro e proteção de dados que voltaram a ser legíveis.
`FavoriteStoreTest` também verifica que outras preferências não são apagadas e
que uma coleção válida ou vazia não pode ser descartada por essa operação.
Não provoque corrupção em um aparelho que contenha favoritos reais. Falhas
reais de escrita em disco e leitura com TalkBack ainda exigem validação específica.

`FavoriteStoreSchemaTest` usa uma preferência exclusiva de teste para verificar
os tipos de cada campo, campos ausentes, duplicidade, limite da coleção e
recuperação explícita. Confere que carregar, salvar e remover não sobrescrevem
uma coleção inválida. Para executar somente essa regressão em um emulador:

```powershell
cd android
.\gradlew.bat --no-daemon connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=br.com.vaicomigo.android.FavoriteStoreSchemaTest"
```

Os dados desses testes são sintéticos. Não injete corrupção nos favoritos reais.
