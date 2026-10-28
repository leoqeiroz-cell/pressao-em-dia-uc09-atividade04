import br.com.senac.pressao.*;
import java.io.IOException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/** Testes independentes de bibliotecas externas e de dados pessoais. */
public final class TestesSistema {
    private static int total;
    private static void verificar(boolean ok, String motivo) {
        if (!ok) throw new AssertionError(motivo); total++;
    }
    private static void invalido(Runnable acao) {
        try { acao.run(); } catch (IllegalArgumentException ex) { total++; return; }
        throw new AssertionError("Dado inválido aceito");
    }
    private static Medicao medicao(boolean estresse) {
        return new Medicao(LocalDate.of(2026, 10, 27), LocalTime.of(8, 30), 120, 80, estresse);
    }
    public static void main(String[] args) throws Exception {
        verificar(Medicao.lerData("29/02/2024").equals(LocalDate.of(2024, 2, 29)), "Bissexto");
        verificar(Medicao.lerData(" 27/10/2026 ").getDayOfMonth() == 27, "Espaços externos");
        for (String t : new String[]{"", " ", "1/1/2026", "31/04/2026", "29/02/2025", "00/12/2026", "01/13/2026", "01/01/0000", "27/10/26", "2026-10-27", "texto"}) invalido(() -> Medicao.lerData(t));
        invalido(() -> Medicao.lerData(null));
        verificar(Medicao.lerHora("00:00").equals(LocalTime.MIDNIGHT), "Meia-noite");
        verificar(Medicao.lerHora("23:59").getHour() == 23, "Último minuto");
        for (String t : new String[]{"", "8:30", "24:00", "12:60", "-1:00", "08:30:00", "abc"}) invalido(() -> Medicao.lerHora(t));
        invalido(() -> Medicao.lerHora(null));
        verificar(Medicao.lerPressao("120") == 120, "Inteiro válido");
        verificar(Medicao.lerPressao(" 80 ") == 80, "Espaços em pressão");
        for (String t : new String[]{"", "0", "-80", "+80", "12,0", "12.0", "120/80", "abc", "2147483648"}) invalido(() -> Medicao.lerPressao(t));
        invalido(() -> Medicao.lerPressao(null));
        invalido(() -> new Medicao(null, LocalTime.NOON, 120, 80, false));
        invalido(() -> new Medicao(LocalDate.now(), null, 120, 80, false));
        invalido(() -> new Medicao(LocalDate.now(), LocalTime.NOON, 0, 80, false));
        invalido(() -> new Medicao(LocalDate.now(), LocalTime.NOON, 120, -1, false));
        Path pasta = Files.createTempDirectory("pressao-testes-");
        try {
            Path arquivo = pasta.resolve("dados/medicoes.csv");
            RepositorioCsv repo = new RepositorioCsv(arquivo);
            verificar(repo.carregar().isEmpty(), "Primeiro uso sem arquivo");
            verificar(!Files.exists(arquivo), "Leitura não cria CSV vazio");
            verificar(repo.salvar(medicao(false)).size() == 1, "Primeiro salvamento");
            verificar(new RepositorioCsv(arquivo).carregar().equals(List.of(medicao(false))), "Reabertura recupera dados");
            verificar(repo.salvar(medicao(true)).size() == 2, "Segundo registro mantém primeiro");
            verificar(repo.carregar().get(1).estresse(), "Estresse marcado preservado");
            String csv = Files.readString(arquivo, StandardCharsets.UTF_8);
            verificar(csv.contains("27/10/2026;08:30;120;80;Não") && csv.contains(";Sim"), "CSV UTF-8 legível");
            ModeloMedicoes modelo = new ModeloMedicoes(); modelo.atualizar(repo.carregar());
            verificar(modelo.getRowCount() == 2 && modelo.getColumnCount() == 5, "Tabela atualizada");
            verificar(modelo.getValueAt(0, 2).equals(120) && modelo.getValueAt(1, 4).equals("Sim"), "Valores da tabela");
            verificar(!modelo.isCellEditable(0, 0), "Histórico não editável");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream saida = new ObjectOutputStream(bytes)) { saida.writeObject(modelo); }
            try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                ModeloMedicoes copia = (ModeloMedicoes) entrada.readObject();
                verificar(copia.getRowCount() == 2, "Modelo pode ser serializado sem perder registros");
                verificar(copia.getValueAt(1, 4).equals("Sim"), "Medição serializável preserva estresse");
            }
            for (String conteudo : new String[]{"", "cabecalho errado\n", RepositorioCsv.CABECALHO + "\n31/02/2026;08:30;120;80;Não\n", RepositorioCsv.CABECALHO + "\n27/10/2026;08:30;120;80;talvez\n", RepositorioCsv.CABECALHO + "\n27/10/2026;08:30;120;80\n"}) {
                Files.writeString(arquivo, conteudo);
                try { repo.carregar(); throw new AssertionError("CSV inválido aceito"); } catch (IOException esperado) { total++; }
                try { repo.salvar(medicao(false)); throw new AssertionError("CSV inválido sobrescrito"); } catch (IOException esperado) { total++; }
                verificar(Files.readString(arquivo).equals(conteudo), "Arquivo inválido preservado");
            }
            Path bloqueio = pasta.resolve("nao-e-pasta"); Files.writeString(bloqueio, "original");
            RepositorioCsv falha = new RepositorioCsv(bloqueio.resolve("medicoes.csv"));
            try { falha.salvar(medicao(false)); throw new AssertionError("Falha de arquivo ignorada"); } catch (IOException esperado) { total++; }
            verificar(Files.readString(bloqueio).equals("original"), "Falha preserva arquivo existente");
            Path semNovaLinha = pasta.resolve("sem-nova-linha.csv");
            Files.writeString(semNovaLinha, RepositorioCsv.CABECALHO + "\n27/10/2026;08:30;120;80;Não");
            verificar(new RepositorioCsv(semNovaLinha).salvar(medicao(true)).size() == 2, "CSV sem quebra final");
        } finally {
            try (var caminhos = Files.walk(pasta)) {
                for (Path caminho : caminhos.sorted(Comparator.reverseOrder()).toList()) Files.delete(caminho);
            }
        }
        System.out.println("APROVADO: " + total + " verificações de validação, CSV e tabela.");
    }
}
