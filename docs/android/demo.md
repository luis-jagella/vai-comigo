# Demonstração Android — 0.1.0-demo.1

Esta é uma entrega de desenvolvimento para demonstração supervisionada, não
um MVP aprovado para uso real. O aplicativo aparece como **Vai Comigo (teste)**.
O APK é debug, assinado com chave de desenvolvimento, sem publicação em loja,
suporte a iPhone ou atualização automática. Requer Android 8.0 (API 26) ou superior.

## O que esta versão permite demonstrar

- Cadastrar, editar e excluir até cinco lugares locais, com confirmação de exclusão.
- Preparar embarque manual ou solicitar localização com consentimento.
- Revisar e corrigir o embarque, ouvir o resumo se houver voz local em português.
- Buscar sugestões de pontos após consentimento e escolher cada resultado.
- Confirmar a abertura do Uber com os pontos escolhidos ou sem preenchimento.
- Permanecer na revisão com uma mensagem de erro quando o Uber não puder abrir.

O Vai Comigo não pede a corrida nem envia pagamento. A busca pode consultar um
serviço externo do Android. Um resultado de busca não comprova que o endereço está
correto; abrir o Uber não comprova que ele aceitou os pontos. Não é um recurso de emergência.

## Obter o APK e conferir a origem

1. No [CI do repositório](https://github.com/luis-jagella/vai-comigo/actions/workflows/ci.yml),
   escolha uma execução concluída com sucesso da branch `main` e confira o commit.
2. Na seção Artifacts, baixe `vai-comigo-demo-<commit>`. O GitHub pode exigir login.
   A retenção é de 14 dias; depois disso, recompile a revisão desejada.
3. Extraia o arquivo. O APK e seu SHA-256 ficam em
   `android/app/build/outputs/apk/debug/`; este guia fica em `docs/android/`.
4. Compare o hash do APK com `app-debug.apk.sha256` antes de instalar. Por exemplo,
   dentro da pasta do APK, no PowerShell:

```powershell
Get-FileHash -Algorithm SHA256 .\app-debug.apk
Get-Content .\app-debug.apk.sha256
```

Os 64 dígitos devem ser iguais, sem considerar maiúsculas/minúsculas. O hash
detecta alterações no arquivo; não substitui a conferência do repositório e do
commit de origem. Um artefato de PR não é a entrega integrada na `main`.

O CI só disponibiliza o pacote depois de build, lint e testes Android passarem.
Confira também o resultado do job do backend. Para gerar localmente:

```powershell
cd android
.\gradlew.bat --no-daemon assembleDebug lintDebug testDebugUnitTest
```

O resultado local fica em `app/build/outputs/apk/debug/app-debug.apk`.
Compilar localmente não executa os testes de aparelho; use também
`connectedDebugAndroidTest` com um emulador ou Android de teste autorizado.

## Instalar sem perder dados por engano

Use um emulador ou Android dedicado/autorizado para teste. Com as ferramentas
do SDK no PATH, liste os aparelhos e escolha explicitamente o destino:

```powershell
adb devices -l
adb -s <serial-do-aparelho> install -r <caminho-do-app-debug.apk>
```

Não copie os marcadores `<...>` literalmente. No celular, autorize a depuração
USB somente para um computador de confiança; revogue-a quando terminar.
Não tente instalar o APK no iPhone: ele não executa aplicativos Android.

Builds locais e diferentes execuções do CI podem usar chaves debug diferentes.
Se aparecer `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, pare: não desinstale o app para
contornar o erro sem avaliar os dados existentes. Desinstalar apaga os favoritos,
que não têm backup/exportação nesta versão. Prefira outro emulador/aparelho de
teste ou uma atualização assinada pela mesma chave. Não há migração iOS.

## Roteiro curto, sem viagem real

No emulador, mantenha o Uber ausente e não autorize buscas externas neste roteiro.
Use somente textos fictícios; esta etapa não precisa de geocodificação ou GPS real.

1. Abra **Vai Comigo (teste)** e confira o aviso de versão de teste.
2. Adicione `Casa de teste` com `Rua Fictícia, 123, Cidade Exemplo`.
3. Selecione o lugar, escolha embarque manual e digite
   `Avenida de Exemplo, 456, Cidade Exemplo`.
4. Revise origem e destino; corrija o embarque e confira o novo resumo.
5. Escolha continuar sem preencher no Uber. Cancele a confirmação primeiro;
   a revisão deve permanecer. Confirme depois, apenas no emulador sem Uber:
   deve aparecer a mensagem de indisponibilidade, sem perder a revisão.
6. Volte aos destinos, edite o favorito e teste cancelar/confirmar sua exclusão.
7. Abra Ajuda e Configurações. Repita com fonte em 200% e orientação horizontal,
   conferindo rolagem, leitura e acesso aos botões.

Se for testar a integração real em outro momento, use exclusivamente o
[roteiro físico](uber-physical-validation.md), pontos públicos de teste e não
confirme uma corrida. Não envie endereços fictícios ao geocodificador para
avaliar qualidade de correspondência. Não registre endereços pessoais, coordenadas,
URIs de viagem ou capturas de dados pessoais em issues.

## Evidências e pendências

As verificações desta entrega são registradas no PR vinculado à issue #28.
Testes automatizados usam dados fictícios e interceptam a abertura bem-sucedida
do Uber. Não comprovam o funcionamento do serviço externo ou do Uber instalado.

Continuam pendentes, sem fechamento artificial de issues:

- Android físico, geocodificador real e preenchimento no Uber: #3, #7 e #17.
- TalkBack, áudio real, contraste e acessibilidade com pessoas: #4, #5 e #6.
- Entrevistas, síntese e testes com o público-alvo: #1 e #2.

Esta entrega fecha o empacotamento da demonstração, não essas validações nem
as fases futuras de apoio familiar. Antes de qualquer piloto, cumprir os
[critérios do MVP](../product/mvp-acceptance-criteria.md).
