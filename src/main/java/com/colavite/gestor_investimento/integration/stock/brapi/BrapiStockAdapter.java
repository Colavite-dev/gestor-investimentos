package com.colavite.gestor_investimento.integration.stock.brapi;

import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import com.colavite.gestor_investimento.integration.stock.StockDataProvider;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Locale;

@Component
public class BrapiStockAdapter implements StockDataProvider {
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
    private String normalize(String value) { return value == null ? null : value.trim().toUpperCase(Locale.ROOT); }
}
