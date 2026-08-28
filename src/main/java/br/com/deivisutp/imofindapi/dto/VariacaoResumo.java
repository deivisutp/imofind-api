package br.com.deivisutp.imofindapi.dto;

/** Resumo de variações (eventos) num período recente. */
public interface VariacaoResumo {
    Long getNovos();

    Long getReducoes();

    Long getAumentos();

    Long getRemovidos();

    Long getReativados();
}
