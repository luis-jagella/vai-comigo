# Busca e escolha de pontos para o Uber

Implementação experimental da issue #17. Não está liberada para piloto: falta
validar o serviço real e o preenchimento no Uber em aparelho físico, além de TalkBack.

## Fluxo

Na revisão, **Buscar pontos para preencher no Uber** explica a consulta externa.
Nada é buscado ao selecionar um favorito ou apenas abrir a revisão. Após
**Permitir busca dos endereços**, a pessoa escolhe explicitamente uma sugestão
de embarque e uma de destino, mesmo quando há somente um resultado.

O serviço recebe o destino e, se digitado, o embarque. Quando o embarque veio
do GPS, essa posição não é enviada ao geocodificador: aparece para confirmação
local e somente o destino é pesquisado. A idade da posição e a permissão são
revalidadas durante o fluxo e antes da saída para o Uber.

São exibidas até três sugestões por endereço, com descrição e coordenadas.
Não há escolha automática nem garantia de número, rua, cidade ou ponto exato.
Se não reconhecer a sugestão, não a selecione: use a alternativa manual.

A confirmação final explica que coordenadas e descrições dos dois pontos serão
enviadas ao pacote `com.ubercab`. O link `uber://riderequest` usa seis parâmetros:
`pickup[latitude]`, `pickup[longitude]`, `pickup[formatted_address]` e os três
equivalentes `dropoff[...]`. O construtor de URI faz a codificação dos valores.
Não há produto, pagamento, credencial, solicitação ou confirmação de corrida.
O mapa e o valor devem ser conferidos no Uber, que pode ignorar os parâmetros.

## Serviço, custo e limites

Usa `android.location.Geocoder`, disponível apenas em aparelhos com uma
implementação desse serviço. O fornecedor é definido pelo sistema/aparelho,
não pelo Vai Comigo. Não há conta, chave ou API comercial contratada pelo app.
Isso não garante gratuidade de rede: podem existir custos de dados da operadora.
Não há quota ou SLA garantidos pelo contrato Android; disponibilidade, qualidade,
idioma e política de retenção externa variam com a implementação.

Android 13+ usa a API assíncrona. Android 8–12 usa uma única thread compartilhada,
sem fila: uma chamada antiga bloqueada não acumula novas consultas. A espera da
interface é limitada a 30 segundos por endereço. Cancelar/timeout invalida os
callbacks; não garante interrupção da consulta remota nem desfaz dados já enviados.
Exceções e mensagens do fornecedor não são registradas.

## Privacidade e alternativas

Pontos e resultados existem somente em memória nesta preparação. Não são salvos
nos favoritos, em Bundle, logs, telemetria ou backend. Alterar/corrigir embarque,
sair, recriar a tela ou recomeçar descarta tudo. Como não há cache de coordenadas,
editar um favorito exige uma nova preparação e nova confirmação dos resultados.
O destino é o retrato do favorito selecionado; para trocar seu endereço, volte,
edite o favorito e selecione-o novamente.

Sem serviço, resposta válida, consentimento ou interesse em usar a busca,
**Continuar sem preencher no Uber** cancela a busca local e oferece confirmação
para abrir o Uber sem dados de viagem. Não abre navegador ou loja automaticamente.
Falha ao abrir o Uber mantém a revisão disponível para nova ação explícita.

## Verificação e pendências

`PrefillSessionTest` e `ResolvedPlaceTest` cobrem etapas, confirmação dos dois
pontos, ambiguidade, descarte e intervalos numéricos. `PrefillFlowTest` injeta
somente respostas fictícias: cobre consentimento, alternativas, callbacks
atrasados, recriação, correção e URI final. Sucessos de abertura são interceptados;
nenhuma corrida real é solicitada. Esses testes não comprovam geocodificação
real nem comportamento da versão instalada do Uber.

Antes de piloto, seguir [o roteiro físico](uber-physical-validation.md), registrar
o aparelho/versões e testar TalkBack, fonte em 200%, falhas e retorno ao app.

## Fontes

- [Geocoder e suas limitações](https://developer.android.com/reference/android/location/Geocoder)
- [Deep links oficiais do Uber](https://developer.uber.com/docs/riders/ride-requests/tutorials/deep-links/introduction)
