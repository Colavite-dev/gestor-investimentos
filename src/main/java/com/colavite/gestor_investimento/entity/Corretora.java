package com.colavite.gestor_investimento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "corretoras")
public class Corretora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(length = 254)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(name = "situacao_cadastral", nullable = false, length = 30)
    private String situacaoCadastral;

    @Column(name = "validada_na_cvm", nullable = false)
    private boolean validadaNaCvm;

    @Column(name = "data_cadastro", nullable = false)
    private Instant dataCadastro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected Corretora() {
    }

    public Corretora(
            String cnpj,
            String razaoSocial,
            String nomeFantasia,
            String email,
            String telefone,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            String situacaoCadastral,
            Usuario usuario
    ) {
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.email = email;
        this.telefone = telefone;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.situacaoCadastral = situacaoCadastral;
        this.usuario = usuario;
        this.validadaNaCvm = false;
        this.dataCadastro = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = Instant.now();
        }
    }

    public void marcarValidadaNaCvm() {
        validadaNaCvm = true;
    }

    public Long getId() {
        return id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getSituacaoCadastral() {
        return situacaoCadastral;
    }

    public boolean isValidadaNaCvm() {
        return validadaNaCvm;
    }

    public Instant getDataCadastro() {
        return dataCadastro;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
