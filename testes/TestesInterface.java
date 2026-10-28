import br.com.senac.pressao.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.Comparator;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Teste gráfico real, com arquivo isolado e evidências de dados fictícios. */
public final class TestesInterface {
    private static int total;
    private static TelaMedicoes tela;
    private static final java.util.concurrent.atomic.AtomicReference<Throwable> falhaAssincrona = new java.util.concurrent.atomic.AtomicReference<>();
    private static void verificar(boolean ok, String motivo) {
        if (!ok) throw new AssertionError(motivo); total++;
    }
    private static Component buscar(Container raiz, String nome) {
        for (Component c : raiz.getComponents()) {
            if (nome.equals(c.getName())) return c;
            if (c instanceof Container filho) { Component achou = buscar(filho, nome); if (achou != null) return achou; }
        }
        return null;
    }
    private static void aguardar() throws Exception {
        long limite = System.currentTimeMillis() + 10000;
        while (tela.estaOcupado() && System.currentTimeMillis() < limite) Thread.sleep(40);
        SwingUtilities.invokeAndWait(() -> verificar(!tela.estaOcupado(), "Operação concluída"));
    }
    private static void captura(Path pasta, String nome) throws Exception {
        if (pasta == null) return;
        SwingUtilities.invokeAndWait(() -> {
            try {
                BufferedImage imagem = new BufferedImage(tela.getWidth(), tela.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = imagem.createGraphics(); tela.paint(g); g.dispose();
                ImageIO.write(imagem, "png", pasta.resolve(nome).toFile());
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
    private static int rotulos(Container raiz) {
        int n = 0;
        for (Component c : raiz.getComponents()) {
            if (c instanceof JLabel label && label.getLabelFor() instanceof JTextField
                    && label.getDisplayedMnemonic() != 0) n++;
            if (c instanceof Container filho) n += rotulos(filho);
        }
        return n;
    }
    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) -> {
            falhaAssincrona.compareAndSet(null, erro); erro.printStackTrace();
        });
        Path temporaria = Files.createTempDirectory("pressao-interface-");
        Path evidencias = args.length == 1 ? Path.of(args[0]) : null;
        if (evidencias != null) Files.createDirectories(evidencias);
        RepositorioCsv repo = new RepositorioCsv(temporaria.resolve("medicoes.csv"));
        try {
            SwingUtilities.invokeAndWait(() -> { Aplicacao.configurarAparencia(); tela = new TelaMedicoes(repo); tela.setVisible(true); });
            aguardar();
            SwingUtilities.invokeAndWait(() -> {
                verificar(tela.isShowing(), "Janela visível");
                JTable tabela = (JTable) buscar(tela, "historico");
                verificar(tabela.getRowCount() == 0, "Histórico inicial vazio");
                Component anterior = null;
                for (String id : new String[]{"data", "hora", "sistolica", "diastolica", "estresse", "salvar", "recarregar", "historico", "arquivo"}) {
                    JComponent c = (JComponent) buscar(tela, id);
                    verificar(c != null && c.getFont().getSize() >= 18, "Fonte ampliada em " + id);
                    verificar(c.getToolTipText() != null && c.getAccessibleContext().getAccessibleName() != null
                            && c.getAccessibleContext().getAccessibleDescription() != null, "Acessibilidade em " + id);
                    if (anterior == null) verificar(tela.getFocusTraversalPolicy().getDefaultComponent(tela) == c, "Foco inicial sem NPE");
                    else verificar(tela.getFocusTraversalPolicy().getComponentAfter(tela, anterior) == c, "Ordem de foco em " + id);
                    anterior = c;
                }
                verificar(tela.getFocusTraversalPolicy().getComponentAfter(tela, anterior) == buscar(tela, "data"), "Foco circular");
                verificar(tela.getFocusTraversalPolicy().getComponentBefore(tela, buscar(tela, "data")) == anterior, "Foco reverso");
                verificar(tela.getRootPane().getActionMap().get("salvar") != null, "Ação de salvar pelo teclado");
                verificar(rotulos(tela) == 4, "Quatro rótulos de entrada associados com atalhos");
            });
            captura(evidencias, "01_tela_inicial.png");
            // Aciona a validação pelo botão e fecha automaticamente apenas o diálogo do teste.
            SwingUtilities.invokeAndWait(() -> {
                ((JTextField) buscar(tela, "data")).setText("31/02/2026");
                // Dá tempo para o Windows concluir a ativação nativa do diálogo.
                Timer fechar = new Timer(1500, e -> {
                    for (Window w : Window.getWindows()) if (w instanceof JDialog d && d.isShowing()) { verificar(d.getTitle().equals("Confira o campo"), "Mensagem amigável"); d.dispose(); }
                });
                fechar.setRepeats(false); fechar.start();
                ((JButton) buscar(tela, "salvar")).doClick();
                verificar(((JTable) buscar(tela, "historico")).getRowCount() == 0, "Inválido não entra na tabela");
                verificar(((JTextField) buscar(tela, "data")).getText().equals("31/02/2026"), "Entrada inválida mantida para correção");
                ((JTextField) buscar(tela, "data")).setText("27/10/2026");
                ((JTextField) buscar(tela, "hora")).setText("08:30");
                ((JTextField) buscar(tela, "sistolica")).setText("120");
                ((JTextField) buscar(tela, "diastolica")).setText("80");
                ((JCheckBox) buscar(tela, "estresse")).setSelected(true);
                ((JButton) buscar(tela, "salvar")).doClick();
            });
            aguardar();
            verificar(repo.carregar().size() == 1 && repo.carregar().get(0).estresse(), "Botão gravou no CSV");
            SwingUtilities.invokeAndWait(() -> {
                verificar(((JTable) buscar(tela, "historico")).getRowCount() == 1, "Tabela atualizada após salvar");
                verificar(((JLabel) buscar(tela, "situacao")).getText().startsWith("Medição salva"), "Confirmação de sucesso");
                verificar(((JTextField) buscar(tela, "sistolica")).getText().isEmpty(), "Prepara próximo registro");
            });
            captura(evidencias, "02_medicao_salva.png");
            SwingUtilities.invokeAndWait(() -> { tela.dispose(); tela = new TelaMedicoes(new RepositorioCsv(repo.arquivo())); tela.setVisible(true); });
            aguardar();
            SwingUtilities.invokeAndWait(() -> verificar(((JTable) buscar(tela, "historico")).getRowCount() == 1, "Reabertura carrega histórico automaticamente"));
            captura(evidencias, "03_historico_reaberto.png");
            // Simula arquivo alterado externamente: nenhum registro deve ser perdido.
            String invalido = RepositorioCsv.CABECALHO + "\nlinha inválida\n";
            Files.writeString(repo.arquivo(), invalido);
            SwingUtilities.invokeAndWait(() -> {
                ((JTextField) buscar(tela, "sistolica")).setText("120");
                ((JTextField) buscar(tela, "diastolica")).setText("80");
                Timer fechar = new Timer(1500, e -> {
                    for (Window w : Window.getWindows()) if (w instanceof JDialog d && d.isShowing()) {
                        verificar(d.getTitle().equals("Não foi possível salvar"), "Falha de arquivo informada na tela");
                        d.dispose(); ((Timer) e.getSource()).stop();
                    }
                });
                fechar.start(); ((JButton) buscar(tela, "salvar")).doClick();
            });
            aguardar();
            verificar(Files.readString(repo.arquivo()).equals(invalido), "Falha na interface preserva CSV");
            SwingUtilities.invokeAndWait(() -> {
                verificar(((JTextField) buscar(tela, "sistolica")).getText().equals("120"), "Falha de gravação mantém campo digitado");
                verificar(((JTable) buscar(tela, "historico")).getRowCount() == 1, "Falha mantém último histórico confirmado");
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> { for (Window w : Window.getWindows()) w.dispose(); });
            try (var caminhos = Files.walk(temporaria)) {
                for (Path p : caminhos.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
        verificar(falhaAssincrona.get() == null, "Nenhuma exceção não tratada na thread de eventos: " + falhaAssincrona.get());
        System.out.println("APROVADO: " + total + " verificações da interface, acessibilidade e persistência.");
    }
}
