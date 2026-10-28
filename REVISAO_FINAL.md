# Revisão final da atividade 4

Esta versão incorpora os ajustes identificados na revisão técnica, mantendo o escopo do enunciado e a interface em uma única tela.

- Corrigidos os avisos de serialização do modelo e da janela, sem suprimir avisos do compilador.
- Compilação rigorosa confirmada nos JDKs 17.0.19 e 23.0.2, ambos com `--release 17 -Xlint:all -Werror`. O Maven também aplica esses argumentos.
- Aprovadas 70 verificações de sistema e 50 verificações gráficas em cada JDK. O teste gráfico agora detecta exceções não tratadas na thread de eventos.
- Ajustado o tempo do teste que fecha diálogos, evitando a corrida de ativação nativa do método de entrada do Windows observada durante a revisão.
- Incluídas capturas nativas da execução pelo NetBeans, dos salvamentos por teclado e do histórico recuperado após fechar e reabrir o programa.
- Conferidos Tab, Shift+Tab, Alt+D, Alt+S, Alt+V e Ctrl+S por eventos de teclado do Windows.
- Testada a escala Java de 125%. Acrescentada rolagem externa para preservar o tamanho do conteúdo em áreas de tela menores.
- Atualizado o relatório com o ambiente, a origem das evidências e os limites da verificação.

A ausência de arquivos `.form` foi mantida: o enunciado da atividade 4 solicita um projeto Java no NetBeans, mas não exige o uso do GUI Builder. A interface está integralmente disponível para edição no código Java.

O histórico permanece em CSV. A serialização do modelo é uma característica da classe Swing e não substitui a persistência em arquivo. O bloqueio de arquivo coordena as gravações das instâncias do aplicativo; o arquivo temporário recebe o conteúdo completo antes da substituição atômica do CSV. Caso essa operação não seja suportada pelo sistema de arquivos, o aplicativo informa a falha e preserva o original.

Não foi realizado teste com leitor de tela ou participantes idosos. A documentação não apresenta esses testes como concluídos. Os resultados e as evidências efetivamente obtidos estão em `testes/RESULTADOS.md`.
