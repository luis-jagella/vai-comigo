# Desenvolvimento Android

O módulo `android/` é um cliente Java independente do backend Maven. Esta
versão contém navegação entre Destinos, Ajuda e Configurações, além de cadastro,
edição, exclusão com confirmação e seleção de até cinco destinos favoritos.
O [fluxo de embarque com consentimento](pickup.md) permite usar localização
ou informar endereço manual e revisar a viagem. A integração com Uber continua
na issue #7. O teste físico
do deep link permanece na issue #3.

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

`FavoriteStore` armazena até cinco registros com identificador estável em
SharedPreferences privadas (`favorites`). Não há envio para backend, geocodificador
ou telemetria. O manifesto desabilita backup e as regras de extração excluem
dados de backup em nuvem e transferência de aparelho. Não há criptografia
adicional implementada pelo aplicativo. Remover o app remove seus favoritos.

Dados inválidos no armazenamento geram uma mensagem de erro e não são
sobrescritos silenciosamente. Ainda não há uma ferramenta de recuperação
de armazenamento danificado na interface.

### Roteiro de teste

1. Cadastre `Casa de teste` em `Rua Fictícia, 123, Cidade Exemplo`.
2. Feche e reabra o app; confirme a presença do cartão.
3. Selecione o lugar e confira o endereço.
4. Edite o nome e confirme a atualização do mesmo favorito.
5. Tente excluir, cancele e confira que o cartão permanece; depois confirme.
6. Cadastre cinco lugares; o sexto deve ser bloqueado, mas a edição deve funcionar.
7. Repita com fonte em 200% e TalkBack, verificando rótulos, rolagem e erros.
