package br.com.alura.screenmatch.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import br.com.alura.screenmatch.service.ConsultaChatGPT;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity()
@Table(name = "series")
public class Serie {
    
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String titulo;
    
    private Integer totalTemporadas;
    private Double avaliacao;
    private Double totalVotos;
    
    @Enumerated(EnumType.STRING)
    private Categoria genero;
    
    private String sinopse;
    private String atores;
    private String poster;

    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.EAGER) 
    private List<Episodio> episodios = new ArrayList();

    public Serie(){}

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();
        this.avaliacao = OptionalDouble.of(Double.valueOf(dadosSerie.avaliacao())).orElse(0);
        this.genero =  Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
        this.sinopse = dadosSerie.sinopse();
        this.atores = dadosSerie.atores();
        //this.poster = ConsultaChatGPT.obterTraducao(dadosSer3ie.poster()).trim();
        this.poster = dadosSerie.poster();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }

    public void setTotalTemporadas(Integer totalTemporadas) {
        this.totalTemporadas = totalTemporadas;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Double getTotalVotos() {
        return totalVotos;
    }

    public void setTotalVotos(Double totalVotos) {
        this.totalVotos = totalVotos;
    }

    public Categoria getGenero() {
        return genero;
    }

    public void setGenero(Categoria genero) {
        this.genero = genero;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getAtores() {
        return atores;
    }

    public void setAtores(String atores) {
        this.atores = atores;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Episodio> getEpisodios() {
        return episodios;
    }

    public void setEpisodios(List<Episodio> episodios) {
        episodios.forEach(e -> e.setSerie(this));
        this.episodios = episodios;
    }
    @Override
    public String toString() {
        return "genero=" + genero + ", titulo=" + titulo + ", totalTemporadas=" + totalTemporadas + ", avaliacao=" + avaliacao
                + ", totalVotos=" + totalVotos + ", sinopse=" + sinopse + ", atores=" + atores
                + ", poster=" + poster + ", episodios=" + episodios;
    }
}
