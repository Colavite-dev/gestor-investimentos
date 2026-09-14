package com.colavite.gestor_investimento.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "carteiras", uniqueConstraints = @UniqueConstraint(name = "uk_carteiras_usuario_nome_normalizado", columnNames = {"usuario_id", "nome_normalizado"}))
public class Carteira {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 100) private String nomeNormalizado;
    @Column(length = 500) private String descricao;
    @Column(name = "data_cadastro", nullable = false) private Instant dataCadastro;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected Carteira() {}
    public Carteira(String nome, String nomeNormalizado, String descricao, Usuario usuario) {
        this.nome = nome; this.nomeNormalizado = nomeNormalizado; this.descricao = descricao; this.usuario = usuario; this.dataCadastro = Instant.now();
    }
    @PrePersist void prePersist() { if (dataCadastro == null) dataCadastro = Instant.now(); }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getNomeNormalizado() { return nomeNormalizado; }
    public String getDescricao() { return descricao; }
    public Instant getDataCadastro() { return dataCadastro; }
    public Usuario getUsuario() { return usuario; }
}
