# Pressão em Dia

[Baixar o projeto completo em ZIP](entrega/PressaoEmDia_UC09_Atividade04_REVISADA_FINAL.zip) · [Executável Java](PressaoEmDia.jar) · [Evidências](evidencias) · [Revisão final](REVISAO_FINAL.md)

Este repositório contém a versão final revisada da Atividade 4. O GitHub Actions compila com `--release 17 -Xlint:all -Werror` nos JDKs 17 e 23 e executa os verificadores de sistema e interface, usando tela virtual para os testes gráficos.

Projeto Java para a Atividade 4 da UC09 — Desenvolver e organizar interface gráfica para aplicações desktop, do curso Técnico em Desenvolvimento de Sistemas do Senac.

O programa permite registrar medições de pressão arterial e consultar o histórico na mesma tela. As medições ficam em um arquivo CSV, que pode ser copiado para levar à consulta. A interface prioriza leitura confortável, orientações simples e navegação pelo teclado.

## Como executar

Requisito: Java **17 ou superior**, com ambiente gráfico disponível.

1. Descompacte todo o ZIP em uma pasta do computador.
2. No Windows, abra `executar.bat`. Você também pode usar `java -jar PressaoEmDia.jar` no terminal.
3. Confira a data e a hora. Elas vêm preenchidas com o horário local do computador e podem ser alteradas para registrar uma medição anterior.
4. Informe a pressão sistólica e a diastólica em **mmHg**, usando números inteiros, como `120` e `80`.
5. Marque a opção de estresse se corresponder ao momento da medição.
6. Clique em **Salvar medição**. Aguarde a confirmação. O registro aparecerá no histórico.

Ao salvar, o programa limpa as duas pressões, desmarca o estresse e atualiza data e hora para preparar uma nova entrada. Se ocorrer uma falha, mantém os dados digitados para que seja possível conferir e tentar novamente. O histórico é somente para consulta: não há edição ou exclusão de registros, funcionalidades que não foram solicitadas no enunciado.

## Abrir no NetBeans

1. Selecione **File > Open Project** e abra esta pasta, que contém `pom.xml`.
2. Configure o JDK do projeto para 17 ou superior.
3. Execute `src/br/com/senac/pressao/Aplicacao.java` com **Shift+F6**, ou execute o projeto.

O projeto é Maven, reconhecido pelo NetBeans. A interface é construída em Java com gerenciadores de layout; seus componentes são editados no código de `TelaMedicoes.java`. Não há dependências externas de execução. Na primeira compilação pelo Maven, pode ser necessário acesso à internet para obter os plugins de compilação. O JAR pronto dispensa essa etapa.

## Onde os dados são salvos

O caminho padrão é a pasta `PressaoEmDia` dentro da pasta pessoal do usuário, com o arquivo `medicoes.csv`. No Windows, normalmente:

```text
C:\Users\SeuUsuario\PressaoEmDia\medicoes.csv
```

O caminho real aparece na própria tela e pode ser selecionado e copiado. A pasta e o CSV são criados no primeiro salvamento. **Para levar o histórico ao médico, copie o arquivo `medicoes.csv`**, por exemplo, para um pen drive. Fechar a aplicação não apaga os registros.

O arquivo utiliza UTF-8, ponto e vírgula como separador e uma linha por medição:

```csv
data;hora;sistolica;diastolica;estresse
27/10/2026;08:30;120;80;Não
```

Também é criado um arquivo auxiliar `medicoes.csv.lock`, usado para coordenar gravações entre instâncias do aplicativo. Ele não contém o histórico e não precisa ser levado à consulta.

Para executar com outro arquivo, use:

```powershell
java -jar PressaoEmDia.jar --arquivo "D:\MeuHistorico\medicoes.csv"
```

O arquivo `exemplos/medicoes_exemplo.csv` contém apenas dados fictícios e não é carregado automaticamente. Para demonstrar a leitura, copie esse exemplo para uma pasta de teste e informe seu caminho com `--arquivo`.

Se o CSV tiver uma linha inválida ou cabeçalho incompatível, o programa informa o problema e preserva o arquivo. A gravação relê o histórico antes de acrescentar o novo registro. **Recarregar histórico** permite tentar a leitura novamente depois que o problema for resolvido. Em caso de falha, a tabela mantém o último histórico carregado com sucesso; a mensagem informa que a operação não foi concluída.

## Teclado e acessibilidade

| Tecla | Ação |
|---|---|
| Tab / Shift+Tab | Avançar / voltar na ordem dos componentes |
| Alt+D | Data |
| Alt+H | Hora |
| Alt+S | Pressão sistólica |
| Alt+I | Pressão diastólica |
| Alt+E / Espaço | Acessar / alternar a marcação de estresse |
| Alt+V / Ctrl+S | Salvar medição |
| Alt+R | Recarregar histórico |
| Alt+T | Acessar o histórico |
| Setas na tabela | Consultar as células do histórico |
| Ctrl+C no caminho do arquivo | Copiar o trecho selecionado |

A sequência de foco é: data → hora → sistólica → diastólica → estresse → salvar → recarregar → histórico → caminho do arquivo. A navegação retorna ao início ao final da sequência. Enter também aciona o botão padrão de salvamento quando não é utilizado por outro componente.

As fontes têm tamanho de 18 pontos ou maior, as linhas da tabela têm altura ampliada e os campos possuem rótulos associados, dicas, nomes e descrições acessíveis. A compatibilidade prática com um leitor de tela depende também da configuração de acessibilidade do Java e do sistema operacional; não foi realizada uma sessão de teste com leitor de tela ou usuários idosos.

Em telas pequenas ou com escala ampliada, a janela respeita a área útil do monitor. Use as barras de rolagem externas para alcançar o restante do conteúdo; os componentes mantêm o tamanho de leitura. A revisão foi testada também com escala Java de 125%.

A versão revisada passou na compilação com `--release 17 -Xlint:all -Werror` usando os compiladores dos JDKs 17 e 23. Foram aprovadas 70 verificações de sistema e 50 de interface em cada ambiente. Consulte os logs e os limites dessas verificações em `testes/RESULTADOS.md`.

## Compilar e testar

Com o **JDK 17 ou superior** disponível, execute `compilar_e_testar.ps1` no PowerShell. O script compila o projeto, executa os dois verificadores e gera o JAR. Não exige JUnit ou outras bibliotecas. O teste de interface abre e fecha janelas automaticamente e usa um CSV temporário com dados fictícios. O script deve ser executado em sessão gráfica.

Também é possível compilar pelo NetBeans ou com `mvn package` e executar pelo comando `mvn compile exec:java`.

O comando Maven empacota o projeto; os verificadores independentes da pasta `testes` são executados pelo script PowerShell, não pelo ciclo de testes do Maven.

Consulte `RELATORIO.md` para a fundamentação da atividade e `testes/RESULTADOS.md` para o escopo das verificações executadas.
