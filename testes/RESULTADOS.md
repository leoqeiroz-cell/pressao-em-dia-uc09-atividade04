# Registro de verificação

Data: 27/10/2026. Ambiente: Windows, Microsoft OpenJDK 17.0.19 e BellSoft OpenJDK 23.0.2, tema Java Metal. Projeto também executado pelo Apache NetBeans 25 com JDK 23.

## Resultados

| Verificação | Resultado |
|---|---|
| Compilação com JDK 17: `--release 17 -Xlint:all -Werror` | Aprovada, sem avisos |
| Compilação com JDK 23: `--release 17 -Xlint:all -Werror` | Aprovada, sem avisos |
| `TestesSistema`, nos JDKs 17 e 23 | 70 verificações aprovadas em cada ambiente |
| `TestesInterface`, nos JDKs 17 e 23 | 50 verificações aprovadas em cada ambiente |
| Interface com escala Java 125%, no JDK 23 | 50 verificações aprovadas |
| Empacotamento Maven com JDK 23 | BUILD SUCCESS; executável atualizado |

Os testes usam arquivos temporários e dados fictícios. Não gravam no histórico padrão do usuário.

Os verificadores são programas Java independentes em `testes`, executados pelo script `compilar_e_testar.ps1`. Por isso, o Maven informa “No tests to run”: `mvn package` compila e empacota, mas não substitui a execução dos dois verificadores.

Os logs finais estão em `testes/logs`. A versão anterior passou na compilação rigorosa do JDK 17, mas a repetição com JDK 23 revelou dois avisos de serialização. Portanto, a aprovação anterior não devia ser generalizada para outros compiladores. A versão revisada foi efetivamente recompilada com os dois JDKs.

O modelo passou a armazenar uma `ArrayList<Medicao>`, e `Medicao` implementa `Serializable`. O repositório da janela foi marcado como `transient`, pois é um serviço da sessão, não o mecanismo de persistência da interface. A aplicação continua recuperando seu estado pelo CSV; não oferece restauração de janelas por serialização Java. O teste de sistema inclui a ida e volta da serialização do modelo.

Durante a revisão, o fechamento automático muito rápido de diálogos expôs uma exceção nativa em `WInputMethod.openCandidateWindow`. O teste passou a aguardar 1,5 segundo antes de fechar cada diálogo e passou a falhar caso haja qualquer exceção não tratada na thread gráfica. Os campos numéricos usam digitação direta sem composição IME. As execuções finais dos dois JDKs terminaram sem essa exceção.

## Cobertura

O teste de sistema verifica data bissexta, datas impossíveis, formato de hora, limites de minuto e hora, campos vazios, nulos, números negativos, decimais e estouro do tipo inteiro. Verifica primeiro uso, criação de pasta, gravação, recuperação com outro repositório, manutenção de registros anteriores, estado de estresse e codificação UTF-8.

Também verifica atualização da tabela, histórico somente para consulta, CSV sem quebra de linha final, cabeçalhos inválidos, linhas incompletas, conteúdo inválido e falhas de caminho. Confirma que uma tentativa de gravação não sobrescreve um CSV inválido nem um arquivo que bloqueia a criação da pasta.

O teste gráfico abre uma janela real, verifica fontes ampliadas, tooltips, nomes e descrições acessíveis dos nove componentes da sequência de foco. Verifica quatro rótulos associados com atalhos, foco inicial, sequência, retorno circular e navegação reversa por chamadas à política de foco. Aciona a validação de uma data inexistente e confirma que o histórico não recebe o dado inválido.

O mesmo teste aciona Salvar, confirma o registro no CSV e na tabela, verifica a mensagem de sucesso e fecha e reabre a janela para verificar a leitura automática. Em seguida, insere uma linha inválida no CSV temporário, tenta salvar pela interface e confirma a mensagem de falha, a manutenção dos campos digitados e a preservação do arquivo.

## Evidências

- `evidencias/01_tela_inicial.png`: janela iniciada com histórico vazio.
- `evidencias/02_medicao_salva.png`: registro fictício confirmado na tabela.
- `evidencias/03_historico_reaberto.png`: mesmo registro recuperado ao reabrir a janela.
- `evidencias/04_netbeans_execucao.jpg`: compilação e execução do projeto pelo NetBeans.
- `evidencias/05_execucao_real_inicial.jpg`: captura real da aplicação aberta pelo NetBeans.
- `evidencias/06_salvamento_real_alt_v.jpg`: primeira medição fictícia salva por Alt+V.
- `evidencias/07_salvamento_real_ctrl_s.jpg`: segunda medição fictícia salva por Ctrl+S.
- `evidencias/08_historico_real_reaberto.jpg`: versão final reexecutada pelo NetBeans, recuperando os dois registros do CSV.
- `evidencias/09_atalho_real_alt_d.jpg`: seleção do campo de data após Alt+D.
- `evidencias/10_rolagem_real_rodape.jpg`: acesso ao caminho do CSV e aos atalhos por rolagem externa.
- `evidencias/escala125`: renderizações do teste com `-Dsun.java2d.uiScale=1.25`.

As imagens PNG dos testes foram obtidas desenhando a janela Swing real. As imagens JPG são capturas nativas da janela durante a interação pelo controle do computador, sem recriação da interface. As capturas 05 a 07 registram a execução anterior ao ajuste final de rolagem. Os caminhos temporários mostrados servem apenas à conferência, não são o caminho padrão de produção.

## Limites da verificação

A bateria automatizada verifica a política de foco diretamente. Na execução pelo NetBeans, foram enviados eventos reais de teclado por automação do Windows para Tab, Shift+Tab, Alt+D, Alt+S, Alt+V e Ctrl+S. Tab avançou de data para hora e de sistólica para diastólica; Shift+Tab retornou de hora para data; Alt+S focalizou a sistólica; Alt+D retornou de hora para data, o que foi confirmado selecionando o texto com Ctrl+A. Alt+V e Ctrl+S gravaram, respectivamente, os dois registros fictícios preservados no CSV da conferência. A janela foi fechada e o projeto reexecutado pelo NetBeans: os dois registros reapareceram automaticamente. Isso não representa um teste com participantes humanos.

A inspeção em escala ampliada utilizou a propriedade de escala do Java em 125%, sem alterar as configurações globais do Windows. A tela passou a respeitar a área útil do monitor e disponibilizar rolagem externa quando o conteúdo não couber, preservando o tamanho dos textos e da tabela. Não foi conduzido teste com leitor de tela ou participantes idosos, nem validação em todos os níveis de escala ou outros sistemas operacionais.

Os testes de arquivo foram executados fora do sandbox do assistente, pois o ambiente restrito bloqueava a substituição atômica de arquivos temporários. A execução em sessão normal do Windows concluiu as gravações e as verificações.

## Reproduzir

Execute `compilar_e_testar.ps1` na pasta do projeto, com JDK 17 ou superior. O teste gráfico exige sessão gráfica disponível. Para gerar novas capturas depois de compilar:

```powershell
java -cp build/classes TestesInterface evidencias
```
