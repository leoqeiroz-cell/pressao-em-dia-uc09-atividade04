package br.com.senac.pressao;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Leitura estrita e gravação por substituição atômica, sem apagar linhas inválidas. */
public final class RepositorioCsv {
    public static final String CABECALHO = "data;hora;sistolica;diastolica;estresse";
    private final Path arquivo;
    public RepositorioCsv(Path arquivo) { this.arquivo = Objects.requireNonNull(arquivo).toAbsolutePath().normalize(); }
    public Path arquivo() { return arquivo; }
    public java.util.List<Medicao> carregar() throws IOException {
        java.util.List<Medicao> medicoes = new ArrayList<>();
        // Só arquivo inexistente significa primeiro uso; demais falhas são informadas.
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String cabecalho = leitor.readLine();
            if (!CABECALHO.equals(cabecalho)) throw new IOException("O cabeçalho do CSV não é reconhecido. O arquivo foi preservado.");
            String linha;
            int numero = 1;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                try {
                    String[] partes = linha.split(";", -1);
                    if (partes.length != 5 || !(partes[4].equals("Sim") || partes[4].equals("Não"))) throw new IllegalArgumentException();
                    medicoes.add(new Medicao(Medicao.lerData(partes[0]), Medicao.lerHora(partes[1]),
                            Medicao.lerPressao(partes[2]), Medicao.lerPressao(partes[3]), partes[4].equals("Sim")));
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Confira a linha " + numero + " do CSV. Há um dado inválido; o arquivo foi preservado.", ex);
                }
            }
        } catch (NoSuchFileException ex) {
            if (Files.exists(arquivo, LinkOption.NOFOLLOW_LINKS)) throw ex;
            return java.util.List.of();
        }
        return java.util.List.copyOf(medicoes);
    }
    public java.util.List<Medicao> salvar(Medicao medicao) throws IOException {
        Objects.requireNonNull(medicao);
        Files.createDirectories(arquivo.getParent());
        Path trava = arquivo.resolveSibling(arquivo.getFileName() + ".lock");
        // Duas instâncias do aplicativo não devem sobrescrever uma à outra.
        try (FileChannel canal = FileChannel.open(trava, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock bloqueio = canal.lock()) {
            if (!bloqueio.isValid()) throw new IOException("Não foi possível reservar o arquivo para gravação.");
            java.util.List<Medicao> todas = new ArrayList<>(carregar());
            todas.add(medicao);
            Path temporario = Files.createTempFile(arquivo.getParent(), "medicoes-", ".tmp");
            try {
                try (BufferedWriter escritor = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                    escritor.write(CABECALHO); escritor.newLine();
                    for (Medicao item : todas) {
                        escritor.write(item.data().format(Medicao.DATA) + ";" + item.hora().format(Medicao.HORA)
                                + ";" + item.sistolica() + ";" + item.diastolica() + ";" + (item.estresse() ? "Sim" : "Não"));
                        escritor.newLine();
                    }
                }
                // Se o sistema de arquivos não suportar a operação, mantém o original e informa a falha.
                Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temporario); }
            return java.util.List.copyOf(todas);
        }
    }
}
