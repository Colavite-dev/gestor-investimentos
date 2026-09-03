package com.colavite.gestor_investimento.integration.stock.twelvedata;

import com.colavite.gestor_investimento.config.TwelveDataProperties;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import com.colavite.gestor_investimento.integration.stock.StockDataProvider;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Locale;

@Component
public class TwelveDataStockAdapter implements StockDataProvider {

    private static final String UNITED_STATES = "United States";
    private static final String COMMON_STOCK = "Common Stock";
    private static final String USD = "USD";

    private final RestClient restClient;
    private final String apiKey;

    @Autowired
    public TwelveDataStockAdapter(
            @Qualifier("twelveDataRestClient") RestClient restClient,
            TwelveDataProperties properties
    ) {
        this(restClient, properties.apiKey());
    }

    TwelveDataStockAdapter(RestClient restClient, String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public Mercado mercado() {
        return Mercado.ESTADOS_UNIDOS;
    }

    @Override
    public StockRegistrationData consultar(String ticker) {
        if (!StringUtils.hasText(apiKey)) {
            throw new StockProviderUnavailableException();
        }

        try {
            String normalizedTicker = normalize(ticker);
            TwelveDataSymbolSearchResponse.Result instrument = findEligibleInstrument(normalizedTicker);
            TwelveDataQuoteResponse quote = quote(normalizedTicker);
            return map(instrument, quote);
        } catch (StockTickerNotFoundException | InvalidStockDataResponseException | StockProviderUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new StockProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidStockDataResponseException(exception);
        }
    }

    @Override
    public StockQuoteData consultarCotacao(String ticker) {
        if (!StringUtils.hasText(apiKey)) {
            throw new StockProviderUnavailableException();
        }
        try {
            String normalizedTicker = normalize(ticker);
            TwelveDataQuoteResponse quote = quote(normalizedTicker);
            return mapCotacao(normalizedTicker, quote);
        } catch (StockTickerNotFoundException | InvalidStockDataResponseException | StockProviderUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new StockProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidStockDataResponseException(exception);
        }
    }

    private TwelveDataSymbolSearchResponse.Result findEligibleInstrument(String ticker) {
        TwelveDataSymbolSearchResponse response = restClient.get()
                .uri(uri -> uri.path("/symbol_search").queryParam("symbol", ticker).build())
                .header(HttpHeaders.AUTHORIZATION, authorization())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, external) -> throwForStatus(external.getStatusCode()))
                .body(TwelveDataSymbolSearchResponse.class);

        throwForStructuredError(response == null ? null : response.status(), response == null ? null : response.code());
        if (response == null || response.data() == null) {
            throw new InvalidStockDataResponseException();
        }
        String normalizedTicker = normalize(ticker);
        return response.data().stream()
                .filter(result -> result != null && normalizedTicker.equals(normalize(result.symbol())))
                .filter(this::isEligible)
                .findFirst()
                .orElseThrow(StockTickerNotFoundException::new);
    }

    private TwelveDataQuoteResponse quote(String ticker) {
        TwelveDataQuoteResponse response = restClient.get()
                .uri(uri -> uri.path("/quote").queryParam("symbol", ticker).build())
                .header(HttpHeaders.AUTHORIZATION, authorization())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, external) -> throwForStatus(external.getStatusCode()))
                .body(TwelveDataQuoteResponse.class);
        throwForStructuredError(response == null ? null : response.status(), response == null ? null : response.code());
        return response;
    }

    private StockRegistrationData map(TwelveDataSymbolSearchResponse.Result instrument, TwelveDataQuoteResponse quote) {
        if (quote == null || blank(quote.symbol()) || !normalize(instrument.symbol()).equals(normalize(quote.symbol()))
                || blank(quote.name()) || blank(quote.close()) || !USD.equals(quote.currency())) {
            throw new InvalidStockDataResponseException();
        }
        try {
            BigDecimal price = new BigDecimal(quote.close());
            if (price.signum() <= 0 || quote.timestamp() == null || quote.timestamp() <= 0) {
                throw new InvalidStockDataResponseException();
            }
            return new StockRegistrationData(normalize(quote.symbol()), quote.name().trim(), Moeda.USD, price, Instant.ofEpochSecond(quote.timestamp()));
        } catch (NumberFormatException | DateTimeException exception) {
            throw new InvalidStockDataResponseException(exception);
        }
    }

    private StockQuoteData mapCotacao(String expectedTicker, TwelveDataQuoteResponse quote) {
        if (quote == null || blank(quote.symbol()) || !normalize(expectedTicker).equals(normalize(quote.symbol()))
                || blank(quote.close()) || !USD.equals(quote.currency())) {
            throw new InvalidStockDataResponseException();
        }
        try {
            BigDecimal price = new BigDecimal(quote.close());
            if (price.signum() <= 0 || quote.timestamp() == null || quote.timestamp() <= 0) {
                throw new InvalidStockDataResponseException();
            }
            return new StockQuoteData(normalize(quote.symbol()), Moeda.USD, price, Instant.ofEpochSecond(quote.timestamp()));
        } catch (NumberFormatException | DateTimeException exception) {
            throw new InvalidStockDataResponseException(exception);
        }
    }

    private boolean isEligible(TwelveDataSymbolSearchResponse.Result result) {
        return UNITED_STATES.equals(result.country())
                && COMMON_STOCK.equals(result.instrument_type())
                && USD.equals(result.currency());
    }

    private void throwForStatus(HttpStatusCode statusCode) {
        int status = statusCode.value();
        if (status == 404) {
            throw new StockTickerNotFoundException();
        }
        throw new StockProviderUnavailableException();
    }

    private void throwForStructuredError(String status, Integer code) {
        if (!"error".equalsIgnoreCase(status)) {
            return;
        }
        if (code != null && (code == 400 || code == 404)) {
            throw new StockTickerNotFoundException();
        }
        throw new StockProviderUnavailableException();
    }

    private String authorization() {
        return "apikey " + apiKey.trim();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean blank(String value) {
        return !StringUtils.hasText(value);
    }
}
