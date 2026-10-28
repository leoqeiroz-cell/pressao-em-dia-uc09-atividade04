package br.com.senac.pressao;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/** Tela única de registro e consulta. Operações de arquivo ocorrem fora da EDT. */
public final class TelaMedicoes extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color AZUL = new Color(20, 68, 91);
    // Serviço de arquivo da sessão; não faz parte do estado serializado do Swing.
    // A persistência da aplicação é o CSV, e a janela é reconstruída ao iniciar.
    private final transient RepositorioCsv repositorio;
    private final JTextField data = campo("data", "Data da medição", "Digite a data no formato dia/mês/ano. Atalho: Alt+D.");
    private final JTextField hora = campo("hora", "Hora da medição", "Digite a hora de 00:00 a 23:59. Atalho: Alt+H.");
    private final JTextField sistolica = campo("sistolica", "Pressão sistólica em mmHg", "Digite o valor inteiro da pressão sistólica, como 120. Atalho: Alt+S.");
    private final JTextField diastolica = campo("diastolica", "Pressão diastólica em mmHg", "Digite o valor inteiro da pressão diastólica, como 80. Atalho: Alt+I.");
    private final JCheckBox estresse = new JCheckBox("Estou em situação de estresse nesta medição");
    private final JButton salvar = new JButton("Salvar medição");
    private final JButton recarregar = new JButton("Recarregar histórico");
    private final ModeloMedicoes modelo = new ModeloMedicoes();
    private final JTable tabela = new JTable(modelo);
    private final JLabel situacao = new JLabel("Carregando histórico…");
    private final JLabel quantidade = new JLabel("Seu histórico");
    private final JTextField local;
    private volatile boolean ocupado;

    public TelaMedicoes(RepositorioCsv repositorio) {
        super("Pressão em Dia | Registro de medições");
        this.repositorio = repositorio;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (ocupado) { Toolkit.getDefaultToolkit().beep(); return; }
                dispose();
            }
        });
        getAccessibleContext().setAccessibleName("Pressão em Dia");
        getAccessibleContext().setAccessibleDescription("Registro de pressão arterial e histórico em arquivo CSV.");
        JPanel painel = new JPanel(new BorderLayout(0, 18));
        painel.setBackground(new Color(245, 248, 250));
        painel.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        // Em telas pequenas ou com escala ampliada, o conteúdo permanece acessível
        // por rolagem, em vez de comprimir a tabela ou cortar os controles.
        painel.setPreferredSize(new Dimension(1000, 760));
        JScrollPane areaTela = new JScrollPane(painel);
        areaTela.setBorder(BorderFactory.createEmptyBorder());
        areaTela.getVerticalScrollBar().setUnitIncrement(28);
        acessivel(areaTela, "Área da tela", "Em telas pequenas ou ampliadas, use as barras de rolagem para acessar todo o conteúdo.");
        setContentPane(areaTela);

        JPanel topo = pilha();
        JLabel titulo = new JLabel("Pressão em Dia");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 30f)); titulo.setForeground(AZUL);
        acessivel(titulo, "Pressão em Dia", "Registro pessoal de medições de pressão arterial.");
        topo.add(titulo);
        JLabel introducao = new JLabel("Anote os valores do aparelho e leve seu histórico à consulta.");
        acessivel(introducao, "Orientação inicial", "Preencha os campos abaixo com os valores medidos no aparelho.");
        topo.add(introducao);
        topo.add(Box.createVerticalStrut(15));
        JLabel instrucao = new JLabel("Nova medição  •  Preencha data, hora e pressões. Marque estresse se for o caso.");
        acessivel(instrucao, "Nova medição", "Data, hora e as duas pressões são obrigatórias.");
        topo.add(instrucao);
        topo.add(Box.createVerticalStrut(10));

        JPanel formulario = new JPanel(new GridLayout(2, 4, 14, 6)); formulario.setOpaque(false);
        formulario.add(rotulo("Data (dd/mm/aaaa)", data, KeyEvent.VK_D));
        formulario.add(rotulo("Hora (hh:mm)", hora, KeyEvent.VK_H));
        formulario.add(rotulo("Sistólica (mmHg)", sistolica, KeyEvent.VK_S));
        formulario.add(rotulo("Diastólica (mmHg)", diastolica, KeyEvent.VK_I));
        formulario.add(data); formulario.add(hora); formulario.add(sistolica); formulario.add(diastolica);
        formulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        topo.add(formulario);
        topo.add(Box.createVerticalStrut(8));
        estresse.setName("estresse"); estresse.setMnemonic(KeyEvent.VK_E); estresse.setOpaque(false);
        acessivel(estresse, "Situação de estresse", "Marque se estava em situação de estresse no momento da medição. Espaço marca ou desmarca. Alt+E.");
        topo.add(estresse);
        topo.add(Box.createVerticalStrut(10));
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); acoes.setOpaque(false);
        salvar.setName("salvar"); salvar.setMnemonic(KeyEvent.VK_V);
        salvar.setBackground(AZUL); salvar.setForeground(Color.WHITE); salvar.setOpaque(true);
        salvar.setMargin(new Insets(10, 22, 10, 22));
        acessivel(salvar, "Salvar medição", "Grava a medição no CSV e atualiza o histórico. Alt+V ou Ctrl+S.");
        salvar.addActionListener(e -> salvar()); acoes.add(salvar);
        acoes.add(Box.createHorizontalStrut(16));
        recarregar.setName("recarregar"); recarregar.setMnemonic(KeyEvent.VK_R);
        recarregar.setMargin(new Insets(10, 16, 10, 16));
        acessivel(recarregar, "Recarregar histórico", "Lê novamente o arquivo de medições. Os campos digitados são mantidos. Alt+R.");
        recarregar.addActionListener(e -> carregar()); acoes.add(recarregar); topo.add(acoes);
        topo.add(Box.createVerticalStrut(10));
        situacao.setName("situacao"); acessivel(situacao, "Situação da operação", "Mensagens de confirmação, orientação ou erro.");
        topo.add(situacao); painel.add(topo, BorderLayout.NORTH);

        JPanel historico = new JPanel(new BorderLayout(0, 8)); historico.setOpaque(false);
        quantidade.setFont(quantidade.getFont().deriveFont(Font.BOLD, 21f)); quantidade.setForeground(AZUL);
        quantidade.setLabelFor(tabela); quantidade.setDisplayedMnemonic(KeyEvent.VK_T);
        acessivel(quantidade, "Histórico de medições", "Alt+T leva à tabela do histórico.");
        historico.add(quantidade, BorderLayout.NORTH);
        tabela.setName("historico"); tabela.setRowHeight(36); tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false); tabela.getTableHeader().setFont(tabela.getFont().deriveFont(Font.BOLD, 18f));
        tabela.getTableHeader().setPreferredSize(new Dimension(100, 38));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setSelectionBackground(AZUL); tabela.setSelectionForeground(Color.WHITE);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabela.setDefaultRenderer(Object.class, new CelulaHistorico(SwingConstants.LEFT));
        tabela.setDefaultRenderer(Number.class, new CelulaHistorico(SwingConstants.RIGHT));
        tabela.getColumnModel().getColumn(0).setPreferredWidth(165);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110);
        acessivel(tabela, "Histórico de pressão arterial", "Medições salvas, na ordem do registro. Use as setas para consultar. Tabela somente para leitura. Alt+T.");
        acessivel(tabela.getTableHeader(), "Colunas do histórico", "Data, hora, pressão sistólica, pressão diastólica e situação de estresse.");
        JScrollPane rolagem = new JScrollPane(tabela); rolagem.setPreferredSize(new Dimension(900, 210));
        acessivel(rolagem, "Área do histórico", "Use a barra de rolagem para consultar todas as medições.");
        historico.add(rolagem, BorderLayout.CENTER); painel.add(historico, BorderLayout.CENTER);

        JPanel rodape = pilha();
        JLabel arquivo = new JLabel("Arquivo do histórico — copie este CSV para levar à consulta:");
        local = new JTextField(repositorio.arquivo().toString()); local.setName("arquivo"); local.setEditable(false);
        local.enableInputMethods(false);
        arquivo.setLabelFor(local); acessivel(arquivo, "Local do histórico", "O caminho abaixo indica onde as medições ficam salvas.");
        acessivel(local, "Caminho do arquivo CSV", "Selecione e copie o caminho com Ctrl+C. O arquivo é criado ao salvar a primeira medição.");
        rodape.add(arquivo); rodape.add(local); rodape.add(Box.createVerticalStrut(8));
        JLabel atalhos = new JLabel("Tab: próximo campo  |  Shift+Tab: voltar  |  Ctrl+S: salvar  |  Alt+T: histórico");
        acessivel(atalhos, "Atalhos de teclado", "Tab avança, Shift+Tab volta, Control+S salva e Alt+T acessa o histórico.");
        rodape.add(atalhos); painel.add(rodape, BorderLayout.SOUTH);
        setFocusTraversalPolicy(new OrdemFoco(data, hora, sistolica, diastolica, estresse, salvar, recarregar, tabela, local));
        // JTable usa Tab para células por padrão; nesta tela Tab segue a sequência do formulário.
        tabela.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        tabela.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        getRootPane().setDefaultButton(salvar);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "salvar");
        getRootPane().getActionMap().put("salvar", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) { if (salvar.isEnabled()) salvar.doClick(); }
        });
        data.setText(LocalDate.now().format(Medicao.DATA)); hora.setText(LocalTime.now().format(Medicao.HORA));
        for (Component c : topo.getComponents()) if (c instanceof JComponent j) j.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Component c : rodape.getComponents()) if (c instanceof JComponent j) j.setAlignmentX(Component.LEFT_ALIGNMENT);
        pack(); setMinimumSize(new Dimension(600, 450));
        Rectangle disponivel = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1060, disponivel.width), Math.min(810, disponivel.height));
        setLocationRelativeTo(null);
        carregar();
    }
    private static JPanel pilha() {
        JPanel painel = new JPanel(); painel.setOpaque(false); painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS)); return painel;
    }
    private static final class CelulaHistorico extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        CelulaHistorico(int alinhamento) { setHorizontalAlignment(alinhamento); }
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionado, foco, linha, coluna);
            // Espaçamento sem retirar a borda que indica o foco na célula.
            setBorder(BorderFactory.createCompoundBorder(getBorder(), BorderFactory.createEmptyBorder(0, 7, 0, 7)));
            return this;
        }
    }
    private static JTextField campo(String id, String nome, String descricao) {
        JTextField campo = new JTextField(12); campo.setName(id); campo.setMargin(new Insets(8, 8, 8, 8));
        // Entradas numéricas usam digitação direta, sem composição de caracteres.
        // Evita callbacks nativos do IME do Windows após descarte de janelas.
        campo.enableInputMethods(false);
        acessivel(campo, nome, descricao); return campo;
    }
    private static void acessivel(JComponent componente, String nome, String descricao) {
        componente.setToolTipText(descricao);
        componente.getAccessibleContext().setAccessibleName(nome);
        componente.getAccessibleContext().setAccessibleDescription(descricao);
    }
    private static JLabel rotulo(String texto, JComponent campo, int tecla) {
        JLabel label = new JLabel(texto); label.setLabelFor(campo); label.setDisplayedMnemonic(tecla);
        acessivel(label, texto, campo.getToolTipText()); return label;
    }
    private <T> T validar(JTextField campo, Function<String, T> regra) {
        try { return regra.apply(campo.getText()); }
        catch (IllegalArgumentException ex) {
            campo.requestFocusInWindow(); campo.selectAll();
            mensagem("Confira o campo", campo.getAccessibleContext().getAccessibleName() + ": " + ex.getMessage(), true);
            throw ex;
        }
    }
    private void salvar() {
        if (ocupado) return;
        final Medicao medicao;
        try {
            medicao = new Medicao(validar(data, Medicao::lerData), validar(hora, Medicao::lerHora),
                    validar(sistolica, Medicao::lerPressao), validar(diastolica, Medicao::lerPressao), estresse.isSelected());
        } catch (IllegalArgumentException ex) { return; }
        executar(true, medicao);
    }
    private void carregar() { if (!ocupado) executar(false, null); }
    private void executar(boolean gravar, Medicao medicao) {
        ocupado = true; salvar.setEnabled(false); recarregar.setEnabled(false);
        for (JTextField campo : new JTextField[]{data, hora, sistolica, diastolica}) campo.setEditable(false);
        estresse.setEnabled(false);
        status(gravar ? "Salvando medição… Aguarde antes de fechar." : "Carregando histórico…");
        new SwingWorker<List<Medicao>, Void>() {
            protected List<Medicao> doInBackground() throws Exception {
                return gravar ? repositorio.salvar(medicao) : repositorio.carregar();
            }
            protected void done() {
                boolean sucesso = false;
                try {
                    List<Medicao> medicoes = get(); modelo.atualizar(medicoes);
                    quantidade.setText("Seu histórico  •  " + medicoes.size() + (medicoes.size() == 1 ? " medição salva" : " medições salvas"));
                    status(gravar ? "Medição salva. Seu histórico foi atualizado." : medicoes.isEmpty()
                            ? "Nenhuma medição salva. Preencha os campos para começar." : "Histórico carregado. Você já pode registrar uma nova medição.");
                    if (gravar) {
                        sistolica.setText(""); diastolica.setText(""); estresse.setSelected(false);
                        data.setText(LocalDate.now().format(Medicao.DATA)); hora.setText(LocalTime.now().format(Medicao.HORA));
                        int ultima = modelo.getRowCount() - 1;
                        tabela.setRowSelectionInterval(ultima, ultima); tabela.scrollRectToVisible(tabela.getCellRect(ultima, 0, true));
                    }
                    sucesso = true;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); mensagem("Operação interrompida", "Não foi possível confirmar a operação. Recarregue o histórico antes de tentar novamente.", true);
                } catch (ExecutionException ex) {
                    String detalhe = ex.getCause() instanceof java.io.IOException ? ex.getCause().getMessage() : "O arquivo está indisponível no momento.";
                    mensagem(gravar ? "Não foi possível salvar" : "Não foi possível abrir o histórico",
                            (gravar ? "A gravação não foi confirmada. Os campos foram mantidos. " : "O histórico não pôde ser carregado. ")
                            + "Confira o acesso ao arquivo indicado na tela e tente Recarregar histórico.\n" + detalhe, true);
                } finally {
                    ocupado = false; salvar.setEnabled(true); recarregar.setEnabled(true); estresse.setEnabled(true);
                    for (JTextField campo : new JTextField[]{data, hora, sistolica, diastolica}) campo.setEditable(true);
                    if (sucesso && gravar) data.requestFocusInWindow();
                }
            }
        }.execute();
    }
    private void status(String texto) { situacao.setText(texto); situacao.getAccessibleContext().setAccessibleDescription(texto); }
    private void mensagem(String titulo, String texto, boolean erro) {
        status(titulo + ". Confira a orientação na mensagem.");
        JTextArea mensagem = new JTextArea(texto, 5, 48); mensagem.setEditable(false); mensagem.setLineWrap(true); mensagem.setWrapStyleWord(true);
        mensagem.enableInputMethods(false);
        mensagem.setOpaque(false); mensagem.setFont(UIManager.getFont("Label.font"));
        acessivel(mensagem, titulo, texto);
        JOptionPane.showMessageDialog(this, mensagem, titulo, erro ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
    public boolean estaOcupado() { return ocupado; }
}
