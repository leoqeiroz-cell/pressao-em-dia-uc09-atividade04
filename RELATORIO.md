# Registro de pressão arterial com foco em usabilidade e acessibilidade

**Aluno:** Léo  
**Curso:** Técnico em Desenvolvimento de Sistemas — Senac  
**Unidade curricular:** UC09 — Desenvolver e organizar interface gráfica para aplicações desktop  
**Atividade:** 4  
**Projeto:** Pressão em Dia

## Proposta desenvolvida

O Pressão em Dia foi desenvolvido para atender à proposta de um programa desktop em que o paciente registra as próprias medições de pressão arterial e leva esse histórico ao cardiologista. A solução reúne o formulário e a consulta aos registros em uma única janela. Os dados são gravados em CSV e recuperados ao abrir o programa, permitindo continuar o acompanhamento em diferentes sessões.

A preocupação central da interface é tornar a tarefa compreensível, especialmente para pessoas idosas. Em vez de depender de ícones ou instruções escondidas, a tela apresenta os nomes dos campos, os formatos esperados e as unidades das pressões. As mensagens indicam como corrigir uma entrada e confirmam quando o salvamento foi concluído.

O programa registra os valores informados. Não estabelece faixas clínicas, não classifica a pressão arterial e não produz recomendações de tratamento. O enunciado solicita registro e persistência de dados; por isso, a validação se concentra na integridade da informação digitada.

## Relação com os materiais da disciplina

Os conteúdos de **Interface desktop — partes 1 e 2** fundamentam o uso dos componentes Swing e a organização dos eventos. A janela utiliza `JFrame`, os campos são `JTextField`, a marcação de estresse é um `JCheckBox` e o histórico é apresentado em `JTable`. O evento do botão Salvar reúne as informações, valida os campos e solicita a persistência. O modelo da tabela é atualizado somente depois da confirmação da gravação.

O material de **Usabilidade e acessibilidade** apresenta os métodos `setToolTipText`, `setAccessibleName`, `setAccessibleDescription`, `setLabelFor` e `setMnemonic`. Esses recursos foram aplicados aos controles da tela. Cada campo possui uma orientação sobre seu preenchimento, um nome acessível e um rótulo associado. As letras de atalho permitem acessar os controles sem depender do mouse.

O mesmo material destaca a necessidade de uma ordem coerente de foco. Neste projeto, uma política de foco explícita percorre os campos na ordem visual e depois alcança as ações, a tabela e o caminho do arquivo. A implementação trata a ausência de componente atual ao determinar o foco inicial, evitando a busca por `null` em listas imutáveis. Na tabela, Tab e Shift+Tab seguem a navegação entre componentes; as setas ficam disponíveis para consultar as células.

O conteúdo de **Tratamento de exceções** orienta a prevenção de interrupções abruptas e a apresentação de mensagens compreensíveis. Entradas inadequadas geram `IllegalArgumentException`, tratada pela tela. Falhas de arquivo são comunicadas por `IOException` e apresentadas com orientações para verificar o acesso ou o conteúdo do CSV. O usuário não recebe uma mensagem técnica isolada nem uma confirmação de sucesso quando a gravação falha.

O material de **Manipulação de arquivos** aborda leitura, escrita e uso de leitores e escritores com buffer. O repositório utiliza `BufferedReader` e `BufferedWriter`, com codificação UTF-8 e fechamento automático por `try-with-resources`. Os métodos de `Files` e `Path` organizam os caminhos, a criação da pasta e a substituição do arquivo. A gravação em um arquivo temporário, seguida de substituição atômica, reduz o risco de deixar um histórico parcialmente escrito.

## Decisões de interface

As fontes dos controles utilizam 18 pontos, e o título e o cabeçalho do histórico recebem destaque adicional. As linhas da tabela têm 36 pixels de altura. O fundo claro, os textos escuros e a seleção com texto branco sobre azul escuro ajudam a distinguir os elementos. A informação de sucesso ou falha é expressa por palavras, sem depender exclusivamente de cor.

Para monitores menores e escalas ampliadas, a janela respeita a área útil disponível e oferece rolagem externa. Essa decisão evita comprimir as linhas do histórico ou reduzir as fontes para fazer o conteúdo caber. A revisão incluiu uma conferência em escala Java de 125%.

Data e hora são preenchidas com o horário local do computador para reduzir a digitação. O usuário pode alterá-las, pois também pode precisar registrar uma medição feita anteriormente. As duas pressões permanecem vazias para evitar que valores de exemplo sejam confundidos com valores medidos. Depois de um salvamento confirmado, o formulário prepara uma nova entrada e mantém o registro anterior visível no histórico.

O local do CSV aparece na própria janela porque o objetivo do paciente inclui levar o arquivo à consulta. Esse campo é somente para leitura, mas pode receber foco e ter seu texto copiado. O botão Recarregar histórico permite repetir a leitura sem fechar a aplicação.

As operações de arquivo são executadas com `SwingWorker`. Assim, a thread de eventos do Swing permanece disponível para desenhar a interface. Durante a operação, as ações de gravação e recarga ficam desabilitadas para evitar solicitações duplicadas; o fechamento da janela aguarda a conclusão da operação.

## Validação e persistência

A data deve estar no formato `dd/mm/aaaa` e corresponder a uma data existente, incluindo a regra de anos bissextos. A hora deve seguir o formato de 24 horas, de `00:00` a `23:59`. As pressões devem ser inteiros positivos, em mmHg. Valores vazios, negativos, decimais ou fora da capacidade do tipo inteiro são rejeitados. Não foi criada uma faixa de normalidade clínica nem uma regra de comparação entre sistólica e diastólica, pois isso extrapolaria o registro solicitado.

No primeiro uso, a ausência do arquivo representa um histórico vazio. Após salvar, cada linha contém data, hora, sistólica, diastólica e estresse. A leitura é estrita: se houver cabeçalho incompatível ou uma linha inválida, o arquivo não é regravado. A mensagem identifica o problema, e os dados digitados são mantidos.

Um bloqueio de arquivo coordena gravações feitas por instâncias deste aplicativo. A proteção não coordena edições simultâneas realizadas por outros programas que não utilizem esse bloqueio. O formato CSV foi escolhido por ser simples, legível e compatível com a entrega proposta.

## Organização do código

| Classe | Responsabilidade |
|---|---|
| `Aplicacao` | Definir fontes e iniciar a interface na thread de eventos |
| `TelaMedicoes` | Apresentar os componentes, tratar eventos e informar o usuário |
| `Medicao` | Representar o registro e validar data, hora e pressões |
| `RepositorioCsv` | Ler e gravar o histórico em UTF-8 |
| `ModeloMedicoes` | Fornecer as linhas e colunas da tabela |
| `OrdemFoco` | Definir a sequência de navegação pelo teclado |

O projeto utiliza Java 17 e Maven, podendo ser aberto no NetBeans pela pasta que contém `pom.xml`. A construção da interface está no código Java e utiliza gerenciadores de layout. Não foram utilizados banco de dados ou bibliotecas externas de execução.

## Atendimento ao enunciado

| Requisito | Implementação |
|---|---|
| Uma tela com data, hora e pressões inteiras | Formulário na janela `TelaMedicoes` |
| Marcação da situação de estresse | `JCheckBox` com estado salvo como Sim ou Não |
| Botão Salvar e gravação em arquivo | Ação do botão e persistência em CSV |
| Histórico na própria tela | `JTable` somente para consulta |
| Carregar ao abrir e atualizar ao salvar | Leitura inicial e atualização após gravação confirmada |
| Caracteres maiores | Fontes de 18 pontos ou maiores |
| Tooltips e descrições acessíveis | Metadados nos controles e orientações de preenchimento |
| Rótulos associados | `JLabel.setLabelFor` nos quatro campos |
| Ordem de foco | Política explícita e navegação circular |
| Atalhos de teclado | Mnemonics com Alt, Ctrl+S e navegação por Tab |
| Mensagens amigáveis | Orientação para entradas inadequadas e falhas de arquivo |

## Verificação da entrega

Foram executadas verificações automatizadas de validação, persistência, atualização da tabela, construção da tela e metadados de acessibilidade. O teste gráfico aciona o botão Salvar, verifica o CSV, fecha a janela e abre outra para confirmar a recuperação do histórico. Também verifica a rejeição de uma data inválida e a preservação de um CSV danificado após uma tentativa de gravação.

As evidências incluem renderizações dos testes em PNG e capturas nativas da execução pelo NetBeans em JPG, com dados fictícios e arquivo de conferência. Foram verificados também eventos de teclado para navegação e salvamento. Esses registros não representam um teste com pacientes. A inspeção dos metadados acessíveis e da política de foco não substitui uma avaliação com pessoas idosas ou uma sessão com leitor de tela.

A compilação rigorosa foi repetida com JDK 17 e JDK 23, usando `--release 17 -Xlint:all -Werror`. Os dois compiladores aprovaram a revisão sem avisos. Os testes de sistema aprovaram 70 verificações, e os de interface, 50 verificações em cada ambiente. Os resultados, os logs e os limites da conferência estão em `testes/RESULTADOS.md`.

## Referências da disciplina

SENAC EAD. **Atividade 4**. UC09 — Desenvolver e organizar interface gráfica para aplicações desktop. Enunciado fornecido no arquivo `atividade 04 enunciado.docx`.

SENAC EAD. **Interface desktop: construção de interface de usuário, manipulação de eventos, uso de controles, manipulação de janelas, construção de formulários e listagens — parte 1**. Material da UC09, arquivo iniciado por `02 Interface desktop`.

SENAC EAD. **Interface desktop: construção de interface de usuário, manipulação de eventos, uso de controles, manipulação de janelas, construção de formulários e listagens — parte 2**. Material da UC09, arquivo iniciado por `03 Interface desktop`.

SENAC EAD. **Tratamento de exceções em linguagem de programação: comandos, classes, aplicabilidade**. Material da UC09, arquivo iniciado por `04 Tratamento de exceções`, especialmente páginas 1 a 4.

SENAC EAD. **Manipulação de arquivos: entradas e saídas de dados, leitura e escrita de arquivos**. Material da UC09, arquivo iniciado por `07 Estrutura de dados`, especialmente as seções de leitura, escrita e `BufferedReader`.

SENAC EAD. **Usabilidade e acessibilidade: conceitos, aplicabilidade e técnicas**. Material da UC09, arquivo iniciado por `08 Estrutura de dados`, especialmente páginas 4 a 8 e as seções de navegação por teclado, atalhos e sequência de foco.

As referências correspondem aos PDFs da disciplina fornecidos na pasta da UC09. Os conceitos foram aplicados à solução e descritos com redação própria.
