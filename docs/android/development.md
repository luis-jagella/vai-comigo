# Desenvolvimento Android

O módulo `android/` é um cliente Java independente do backend Maven. Esta
primeira versão contém navegação entre Destinos, Ajuda e Configurações. Não
possui cadastro de favoritos nem integração com Uber; esses incrementos estão
nas issues #5 a #7. O teste físico do deep link permanece na issue #3.

## Compilar

Pré-requisitos: JDK 17, Android SDK Platform 36 e Build Tools 35.0.0. Defina
`ANDROID_HOME` para o SDK ou configure `android/local.properties` localmente
(o arquivo é ignorado pelo Git). O Gradle é fornecido pelo wrapper.

```bash
cd android
./gradlew assembleDebug lintDebug
```

No Windows, use `gradlew.bat`. O APK fica em
`android/app/build/outputs/apk/debug/app-debug.apk`.

## Testes de navegação

Com um emulador ou aparelho conectado por ADB:

```bash
./gradlew connectedDebugAndroidTest
```

Os testes instrumentados exercitam as telas de ajuda e configurações, o
retorno a destinos e os alvos de toque. A integração contínua compila o APK,
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
