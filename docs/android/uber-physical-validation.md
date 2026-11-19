# Validação física do preenchimento no Uber

Status: pendente. Emulador, fakes e CI não substituem esta validação.
Não confirmar corrida, inserir pagamento ou usar endereços pessoais no registro.

## Preparar

- Conectar um Android de teste e confirmar autorização ADB com seu proprietário.
- Registrar modelo, versão Android, versão do Uber, data e escala de fonte.
- Instalar o APK debug e usar pontos públicos de teste, sem residência ou dados pessoais.
- Conferir a política do fornecedor de geocodificação do aparelho antes de um piloto.

## Cenários

1. Negar a busca: confirmar que nada foi consultado e a abertura manual não preenche pontos.
2. Usar embarque manual e destino, permitir a busca e confirmar cada resultado.
3. Conferir no mapa do Uber se origem e destino correspondem aos pontos escolhidos.
   Registrar apenas aprovado/reprovado e erro genérico, sem copiar endereços ou URI completa.
4. Cancelar a confirmação de abertura: não deve sair do Vai Comigo.
5. Repetir com GPS; esperar expirar uma posição e verificar descarte e nova escolha.
6. Testar serviço indisponível, endereço não encontrado/ambíguo e consulta cancelada.
7. Corrigir o embarque e voltar após segundo plano/giro: nenhum ponto anterior é reaproveitado.
8. Repetir com fonte em 200%, TalkBack e orientação horizontal, verificando rolagem e rótulos.
9. Testar instalação de desenvolvimento sem Uber e retorno após cancelar no Uber.

## Registrar

Para cada cenário: resultado esperado, resultado observado, aprovado/reprovado,
barreira de acessibilidade e issue de acompanhamento. Não encerrar #3/#17/#7
como validadas fisicamente enquanto esse registro estiver pendente.
