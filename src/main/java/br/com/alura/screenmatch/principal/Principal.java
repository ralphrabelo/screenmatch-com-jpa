package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private SerieRepository serieRepository;

    private List<Serie> series;

    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        Integer opcao = -1;
        while (opcao != 0) {
            var menu = """
                    --------------------------------------------------------------------      
                    1 - Carregar séries do OMDB
                    2 - Carregar episódios do OMDB
                    3 - Listar séries carregadas
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Buscar Top 5 séries
                    7 - Buscar séries por categoria
                    8 - Filtrar séries (número máximo de temporadas e avaliação mínima)
                    9 - Buscar espisódios
                    10 - Buscar Top 5 episódios
                    11 - Buscar episódios de uma serie por ano
                    --------------------------------------------------------------------
                    0 - Sair
                    --------------------------------------------------------------------
                    """;
            System.out.println(menu);
            System.out.printf("Digite o número do menu: ");
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSerieBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    filtrarSeries();
                    break;
                case 9:
                    buscarEpisodios();
                    break;
                case 10:
                    buscarTopEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosPosterioresAData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        this.listarSerieBuscadas();
        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();

        // Optional<Serie> serie = series.stream()
        // .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
        // .findFirst();
        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            Serie serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(
                        ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }

    }

    private void listarSerieBuscadas() {
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.printf("Nome da série: ");
        String nomeSerie = leitura.nextLine();
        serieBuscada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Nome do ator: ");
        String nomeAtor = leitura.nextLine();
        System.out.println("Entre com a avaliação mínima (número entre 0-10)");
        Double avaliacaoMinima = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> seriesEncontradas = serieRepository
                .findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacaoMinima);
        System.out.println("Series em que " + nomeAtor + " trabalhou: ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + ", " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> seriesTop5 = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        seriesTop5.forEach(s -> System.out.println(s.getTitulo() + ", " + s.getAvaliacao()));
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Categorias disponíveis: ");
        List<String> enumNames = EnumSet.allOf(Categoria.class).stream().map(Categoria::getCategoriaPtBr)
                .collect(Collectors.toList());
        enumNames.forEach(System.out::println);

        System.out.printf("Digite a categoria escolhida: ");
        String categoria = leitura.nextLine();
        Categoria categoriaPesquisa = Categoria.fromStringPtBr(categoria);
        List<Serie> seriesEncontradas = serieRepository.findByGenero(categoriaPesquisa);
        System.out.println("Séries encontradas de " + categoria + ":");

        if (seriesEncontradas.size() > 0) {
            seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + ", " + s.getGenero()));
        } else {
            System.out.printf("Nenhuma série encontrada.");
        }
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + ", " + s.getGenero()));
    }

    private void filtrarSeries() {
        System.out.printf("Quantidade máxima de temporadas: ");
        Integer totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.printf("Entre com a avaliação mínima (número entre 0-10): ");
        Double avaliacaoMinima = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> seriesEncontradas = serieRepository.buscaSeriesPorTemporadaEAvaliacao(totalTemporadas, avaliacaoMinima);
                // .findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas, avaliacaoMinima);
        System.out.println("Series encontradas:");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + ", avaliação: " + s.getAvaliacao() + 
            ", temporadas: " + s.getTotalTemporadas()));
    }
    private void buscarEpisodios() {
        System.out.printf("Digite um trecho do nome do episódio: ");
        String trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = serieRepository.buscaEpisodios(trechoEpisodio);
        System.out.println("Episódios encontrados:");
        
        if (episodiosEncontrados.size() > 0) {
            episodiosEncontrados.forEach(e -> 
                System.out.printf("Série: %s - Temporada %s - Episodio %s - %s\n", 
                    e.getSerie().getTitulo(), e.getTemporada(), 
                    e.getNumeroEpisodio(), e.getTitulo()));
        } else {
            System.out.printf("Nenhum episódio encontrado.");
        }
    }
    
    private void buscarTopEpisodiosPorSerie() {
        if (serieBuscada == null || !serieBuscada.isPresent())
        {
            buscarSeriePorTitulo();
        }
        if (serieBuscada.isPresent()) {

            Serie serie = serieBuscada.get();

            List<Episodio> topEpisodios = serieRepository.buscaTopEpisodiosPorSerie(serie);

            System.out.printf("Os Top 5 episódios da série %s são: ", serie.getTitulo());
            System.out.println();
            topEpisodios.forEach(e -> 
                System.out.printf("Temporada %s - Episódio  %s - %s - Avaliação %s\n", 
                    e.getTemporada(), 
                    e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosPosterioresAData() {
        if (serieBuscada == null || !serieBuscada.isPresent())
        {
            buscarSeriePorTitulo();
        }
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            System.out.printf("Digite o ano de lançamento mínimo dos episódios da série ", serie.getTitulo());
            Integer anoLancamentoMinimo = leitura.nextInt();
            leitura.nextLine();
            List<Episodio> episodiosAno = serieRepository.buscaEpisodios(serie, anoLancamentoMinimo);

            System.out.printf("Digite o ano de lançamento mínimo para série ", serie.getTitulo());
            System.out.println();
            episodiosAno.forEach(e -> 
                System.out.printf("Temporada %s - Episódio  %s - %s - Avaliação %s\n", 
                    e.getTemporada(), 
                    e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
    }
}
