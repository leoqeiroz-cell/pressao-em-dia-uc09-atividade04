package br.com.senac.pressao;

import java.awt.*;
import java.util.List;

/** Ordem explícita sem pesquisar null em uma lista imutável. */
public final class OrdemFoco extends FocusTraversalPolicy {
    private final List<Component> componentes;
    public OrdemFoco(Component... componentes) {
        this.componentes = List.of(componentes);
        if (this.componentes.isEmpty()) throw new IllegalArgumentException("Informe os componentes.");
    }
    private Component procurar(Component atual, int passo) {
        int indice = atual == null ? (passo > 0 ? -1 : 0) : componentes.indexOf(atual);
        for (int i = 1; i <= componentes.size(); i++) {
            Component candidato = componentes.get(Math.floorMod(indice + passo * i, componentes.size()));
            if (candidato.isVisible() && candidato.isEnabled() && candidato.isFocusable()) return candidato;
        }
        return null;
    }
    public Component getComponentAfter(Container raiz, Component atual) { return procurar(atual, 1); }
    public Component getComponentBefore(Container raiz, Component atual) { return procurar(atual, -1); }
    public Component getFirstComponent(Container raiz) { return procurar(null, 1); }
    public Component getLastComponent(Container raiz) { return procurar(null, -1); }
    public Component getDefaultComponent(Container raiz) { return getFirstComponent(raiz); }
}
