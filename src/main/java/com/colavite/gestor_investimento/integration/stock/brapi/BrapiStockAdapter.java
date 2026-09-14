package com.colavite.gestor_investimento.integration.stock.brapi;

import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import com.colavite.gestor_investimento.integration.stock.StockDataProvider;
import com.colavite.gestor_investimento.integration.stock.StockCatalogItemData;
import com.colavite.gestor_investimento.integration.stock.StockCatalogPageData;
import com.colavite.gestor_investimento.integration.stock.StockCatalogProvider;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.colavite.gestor_investimento.integration.stock.StockSuggestionData;
import java.util.Locale;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class BrapiStockAdapter implements StockDataProvider, StockCatalogProvider {
    private final RestClient restClient;
    public BrapiStockAdapter(@Qualifier("brapiRestClient") RestClient restClient) { this.restClient = restClient; }
    @Override
    public Mercado mercado() {
        return Mercado.BRASIL;
    }

    @Override public StockRegistrationData consultar(String ticker) {
        String normalizedTicker = normalize(ticker);
        BrapiStockQuoteResponse.Result result = consultarResultado(normalizedTicker);
        if (blank(result.data().shortName())) throw new InvalidStockDataResponseException();
        StockQuoteData quote = mapCotacao(normalizedTicker, result);
        return new StockRegistrationData(quote.ticker(), result.data().shortName().trim(), quote.moeda(), quote.cotacaoAtual(), quote.dataHoraCotacao());
    }

    @Override
    public List<StockSuggestionData> pesquisar(String termo) {
        try {
            BrapiStockSearchResponse response = restClient.get().uri(uri -> uri.path("/api/quote/list")
                    .queryParam("search", termo).queryParam("type", "stock").queryParam("limit", 20).build())
                    .retrieve().onStatus(HttpStatusCode::isError, (request, external) -> throwForStatus(external.getStatusCode().value()))
                    .body(BrapiStockSearchResponse.class);
            if (response == null || response.stocks() == null) throw new InvalidStockDataResponseException();
            return response.stocks().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(this::toSuggestion)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (StockTickerNotFoundException | StockProviderUnavailableException | InvalidStockDataResponseException e) { throw e;
        } catch (ResourceAccessException e) { throw new StockProviderUnavailableException(e);
        } catch (RestClientException e) { throw new InvalidStockDataResponseException(e); }
    }

    @Override
    public StockCatalogPageData catalogar(String termo, int page, int size) {
        try {
            BrapiStockSearchResponse response = restClient.get().uri(uri -> {
                        var builder = uri.path("/api/quote/list")
                                .queryParam("type", "stock")
                                .queryParam("page", page + 1)
                                .queryParam("limit", size);
                        if (termo != null && !termo.isBlank()) builder.queryParam("search", termo.trim());
                        return builder.build();
                    }).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, external) -> throwForStatus(external.getStatusCode().value()))
                    .body(BrapiStockSearchResponse.class);
            if (response == null || response.stocks() == null || response.currentPage() == null
                    || response.currentPage() != page + 1 || response.hasNextPage() == null
                    || response.totalCount() == null || response.totalCount() < 0) {
                throw new InvalidStockDataResponseException();
            }
            List<StockCatalogItemData> items = response.stocks().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(this::toCatalogItem)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            return new StockCatalogPageData(items, page, size, response.hasNextPage(), response.totalCount());
        } catch (StockTickerNotFoundException | StockProviderUnavailableException | InvalidStockDataResponseException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new StockProviderUnavailableException(e);
        } catch (RestClientException e) {
            throw new InvalidStockDataResponseException(e);
        }
    }

    private StockSuggestionData toSuggestion(BrapiStockSearchResponse.Result stock) {
        String ticker = firstNonBlank(stock.stock(), stock.symbol());
        String type = firstNonBlank(stock.type(), stock.assetType());
        if (blank(ticker) || (type != null && !"stock".equalsIgnoreCase(type))
                || (stock.currency() != null && !"BRL".equalsIgnoreCase(stock.currency()))) return null;
        return new StockSuggestionData(normalize(ticker), firstNonBlank(stock.name(), stock.shortName()), Mercado.BRASIL, Moeda.BRL);
    }

    private StockCatalogItemData toCatalogItem(BrapiStockSearchResponse.Result stock) {
        String ticker = firstNonBlank(stock.stock(), stock.symbol());
        String type = firstNonBlank(stock.type(), stock.assetType());
        if (blank(ticker) || !"stock".equalsIgnoreCase(type)
                || (stock.currency() != null && !"BRL".equalsIgnoreCase(stock.currency()))) {
            return null;
        }
        java.math.BigDecimal quote = stock.close() != null && stock.close().signum() > 0 ? stock.close() : null;
        return new StockCatalogItemData(
                normalize(ticker),
                firstNonBlank(stock.name(), stock.shortName(), normalize(ticker)),
                Mercado.BRASIL,
                Moeda.BRL,
                null,
                null,
                quote,
                validHttpsUrl(stock.logo())
        );
    }

    @Override
    public StockQuoteData consultarCotacao(String ticker) {
        String normalizedTicker = normalize(ticker);
        return mapCotacao(normalizedTicker, consultarResultado(normalizedTicker));
    }

    private BrapiStockQuoteResponse.Result consultarResultado(String ticker) {
        try {
            BrapiStockQuoteResponse response = restClient.get().uri(uri -> uri.path("/api/v2/stocks/quote").queryParam("symbols", ticker).build()).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, external) -> {
                        int status = external.getStatusCode().value();
                        if (status == 404) throw new StockTickerNotFoundException();
                        if (status == 401 || status == 403 || status == 429 || status >= 500) throw new StockProviderUnavailableException();
                        throw new InvalidStockDataResponseException();
                    })
                    .body(BrapiStockQuoteResponse.class);
            if (response == null || response.results() == null || response.results().isEmpty()) throw new StockTickerNotFoundException();
            if (response.results().size() != 1 || response.results().get(0) == null) throw new InvalidStockDataResponseException();
            return response.results().get(0);
        } catch (StockTickerNotFoundException | StockProviderUnavailableException | InvalidStockDataResponseException e) { throw e;
        } catch (ResourceAccessException e) { throw new StockProviderUnavailableException(e);
        } catch (RestClientException e) { throw new InvalidStockDataResponseException(e); }
    }

    private void throwForStatus(int status) {
        if (status == 404) throw new StockTickerNotFoundException();
        if (status == 401 || status == 403 || status == 429 || status >= 500) throw new StockProviderUnavailableException();
        throw new InvalidStockDataResponseException();
    }

    private StockQuoteData mapCotacao(String ticker, BrapiStockQuoteResponse.Result result) {
        String requestedTicker = normalize(ticker);
        if (blank(result.requestedSymbol()) || blank(result.symbol()) || !Boolean.FALSE.equals(result.changed())
                || !requestedTicker.equals(normalize(result.requestedSymbol())) || !requestedTicker.equals(normalize(result.symbol()))
                || result.data() == null || !"BRL".equals(result.data().currency()) || result.data().regularMarketPrice() == null
                || result.data().regularMarketPrice().signum() <= 0 || result.data().regularMarketTime() == null) {
            throw new InvalidStockDataResponseException();
        }
        return new StockQuoteData(requestedTicker, Moeda.BRL, result.data().regularMarketPrice(), result.data().regularMarketTime());
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String firstNonBlank(String... values) {
        for (String value : values) if (!blank(value)) return value.trim();
        return null;
    }
    private String validHttpsUrl(String value) {
        if (blank(value)) return null;
        try {
            URI uri = new URI(value.trim());
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null
                    ? uri.toASCIIString() : null;
        } catch (URISyntaxException exception) {
            return null;
        }
    }
    private String normalize(String value) { return value == null ? null : value.trim().toUpperCase(Locale.ROOT); }
}
