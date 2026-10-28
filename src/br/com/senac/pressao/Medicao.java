package br.com.senac.pressao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Uma medição registrada pelo usuário, sem interpretação clínica. */
public record Medicao(LocalDate data, LocalTime hora, int sistolica, int diastolica, boolean estresse)
        implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public Medicao {
        if (data == null || data.getYear() < 1 || data.getYear() > 9999) throw new IllegalArgumentException("Informe uma data válida, com ano de quatro dígitos.");
        if (hora == null || hora.getSecond() != 0 || hora.getNano() != 0) throw new IllegalArgumentException("Informe a hora em horas e minutos.");
        if (sistolica <= 0 || diastolica <= 0) throw new IllegalArgumentException("As pressões devem ser números inteiros maiores que zero, em mmHg.");
    }
    public static LocalDate lerData(String texto) {
        String valor = texto == null ? "" : texto.trim();
        try {
            if (!valor.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) throw new DateTimeParseException("Formato", valor, 0);
            LocalDate data = LocalDate.parse(valor, DATA);
            if (data.getYear() < 1) throw new DateTimeParseException("Ano", valor, 0);
            return data;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Confira a data. Use dia/mês/ano, como 27/10/2026, e uma data existente.", ex);
        }
    }
    public static LocalTime lerHora(String texto) {
        String valor = texto == null ? "" : texto.trim();
        try {
            if (!valor.matches("[0-9]{2}:[0-9]{2}")) throw new DateTimeParseException("Formato", valor, 0);
            return LocalTime.parse(valor, HORA);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Confira a hora. Use o formato de 24 horas, como 08:30 ou 19:45.", ex);
        }
    }
    public static int lerPressao(String texto) {
        String valor = texto == null ? "" : texto.trim();
        try {
            if (!valor.matches("[0-9]+")) throw new NumberFormatException();
            int numero = Integer.parseInt(valor);
            if (numero <= 0) throw new NumberFormatException();
            return numero;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Informe a pressão em mmHg usando um número inteiro maior que zero, como 120. Não use vírgula ou ponto.", ex);
        }
    }
}
