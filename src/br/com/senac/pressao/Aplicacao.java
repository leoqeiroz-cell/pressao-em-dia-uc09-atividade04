package br.com.senac.pressao;

import java.awt.Font;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public final class Aplicacao {
    private Aplicacao() { }
    public static void configurarAparencia() {
        // Metal oferece contraste e foco visível consistentes entre máquinas.
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) { /* Mantém o tema disponível. */ }
        for (Object chave : java.util.Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(chave) instanceof Font) UIManager.put(chave, new FontUIResource("Dialog", Font.PLAIN, 18));
        }
        ToolTipManager.sharedInstance().setDismissDelay(20000);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            Path arquivo = args.length == 2 && args[0].equals("--arquivo") ? Path.of(args[1])
                    : Path.of(System.getProperty("user.home"), "PressaoEmDia", "medicoes.csv");
            new TelaMedicoes(new RepositorioCsv(arquivo)).setVisible(true);
        });
    }
}
