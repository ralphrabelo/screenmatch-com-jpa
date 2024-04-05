package br.com.alura.screenmatch.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;

@Service()
public class SerieService {
    
    @Autowired()
    private SerieRepository serieRepositorio;
    
    public List<SerieDTO> obterSeries() {
        return converteDadosSerie(serieRepositorio.findAll());
    }

    public List<SerieDTO> obterTop5() {
        return converteDadosSerie(serieRepositorio.findTop5ByOrderByAvaliacaoDesc());
    }

    public List<SerieDTO> obterLancamentos() {
        return converteDadosSerie(serieRepositorio.lancamentosMaisRecentes());
    }

    private List<SerieDTO> converteDadosSerie(List<Serie> series) {
        return series.stream()
        .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
        .collect(Collectors.toList()); 
    }

    private List<EpisodioDTO> converteDadosEpisodio(List<Episodio> episodios) {
        return episodios.stream()
        .map(e-> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
        .collect(Collectors.toList()); 
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> serie =  serieRepositorio.findById(id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<EpisodioDTO> obterTodasTemporadas(Long id) {
        Optional<Serie> serie =  serieRepositorio.findById(id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return s.getEpisodios().stream()
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                .collect(Collectors.toList());
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporadasPorNumero(Long id, Long numeroTemporada) {
        return converteDadosEpisodio(serieRepositorio.obterEpisodiosPorTemporada(id, numeroTemporada));
    }

    public List<SerieDTO> obterSeriesPorCategoria(String nomeGenero) {
        Categoria categoria = Categoria.fromStringPtBr(nomeGenero);
        return converteDadosSerie(serieRepositorio.findByGenero(categoria));
    }
}
