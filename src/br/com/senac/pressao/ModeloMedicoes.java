package br.com.senac.pressao;

import java.util.List;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/** Histórico somente para consulta: os valores exibidos são os gravados. */
public final class ModeloMedicoes extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private ArrayList<Medicao> medicoes = new ArrayList<>();
    private final String[] colunas = {"Data", "Hora", "Sistólica (mmHg)", "Diastólica (mmHg)", "Estresse"};
    public void atualizar(List<Medicao> novas) { medicoes = new ArrayList<>(List.copyOf(novas)); fireTableDataChanged(); }
    public int getRowCount() { return medicoes.size(); }
    public int getColumnCount() { return colunas.length; }
    public String getColumnName(int coluna) { return colunas[coluna]; }
    public Class<?> getColumnClass(int coluna) { return coluna == 2 || coluna == 3 ? Integer.class : String.class; }
    public Object getValueAt(int linha, int coluna) {
        Medicao m = medicoes.get(linha);
        return switch (coluna) {
            case 0 -> m.data().format(Medicao.DATA);
            case 1 -> m.hora().format(Medicao.HORA);
            case 2 -> m.sistolica();
            case 3 -> m.diastolica();
            case 4 -> m.estresse() ? "Sim" : "Não";
            default -> throw new IndexOutOfBoundsException();
        };
    }
}
