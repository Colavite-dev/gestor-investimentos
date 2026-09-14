package com.colavite.gestor_investimento.integration.stock.twelvedata;

import com.colavite.gestor_investimento.config.TwelveDataProperties;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import com.colavite.gestor_investimento.exception.StockVenueAmbiguityException;
import com.colavite.gestor_investimento.integration.stock.StockDataProvider;
import com.colavite.gestor_investimento.integration.stock.StockCatalogItemData;
import com.colavite.gestor_investimento.integration.stock.StockCatalogPageData;
import com.colavite.gestor_investimento.integration.stock.StockCatalogProvider;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import com.colavite.gestor_investimento.integration.stock.StockSuggestionData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TwelveDataStockAdapter implements StockDataProvider, StockCatalogProvider {

    private static final String UNITED_STATES = "United States";
    private static final String COMMON_STOCK = "Common Stock";
    private static final String USD = "USD";
    private static final Set<Venue> PRIMARY_VENUES = Set.of(
            new Venue("NASDAQ", "XNGS"),
            new Venue("NYSE", "XNYS"),
            new Venue("NYSE American", "XASE"));
    private static final Venue IEX_VENUE = new Venue("IEX", "IEXG");
    private static final Set<String> CATALOG_EXCHANGES = Set.of("NASDAQ", "NYSE", "NYSE American", "IEX");
    private static final Set<String> CATALOG_MIC_CODES = Set.of("XNAS", "XNGS", "XNYS", "XASE", "IEXG");
    private static final Pattern NORMAL_US_STOCK_SYMBOL = Pattern.compile("[A-Z]{1,5}(?:\\.[A-UV-Z])?");
    private static final Set<String> KNOWN_TICKER_ERROR_MESSAGES = Set.of(
            "INVALID SYMBOL", "SYMBOL NOT FOUND", "SYMBOL IS MISSING", "THE SYMBOL IS MISSING",
            "REQUESTED SYMBOL IS INVALID", "INVALID OR UNSUPPORTED SYMBOL");
    private static final Pattern STATUS_FIELD = Pattern.compile("\\\"status\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private static final Pattern CODE_FIELD = Pattern.compile("\\\"code\\\"\\s*:\\s*(\\d+)");
    private static final Pattern MESSAGE_FIELD = Pattern.compile("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");

    private final RestClient restClient;
    private final String apiKey;
    private final Clock clock;
    private final Duration catalogTtl;
    private volatile CatalogCache catalogCache;

    @Autowired
    public TwelveDataStockAdapter(
            @Qualifier("twelveDataRestClient") RestClient restClient,
            TwelveDataProperties properties
    ) {
        this(restClient, properties.apiKey(), Clock.systemUTC(), Duration.ofHours(24));
    }

    TwelveDataStockAdapter(RestClient restClient, String apiKey) {
        this(restClient, apiKey, Clock.systemUTC(), Duration.ofHours(24));
    }

    TwelveDataStockAdapter(RestClient restClient, String apiKey, Clock clock, Duration catalogTtl) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.clock = clock;
        this.catalogTtl = catalogTtl;
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
        } catch (StockTickerNotFoundException | StockVenueAmbiguityException | InvalidStockDataResponseException | StockProviderUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new StockProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidStockDataResponseException(exception);
        }
    }

    @Override
    public List<StockSuggestionData> pesquisar(String termo) {
        if (!StringUtils.hasText(apiKey)) throw new StockProviderUnavailableException();
        try {
            TwelveDataSymbolSearchResponse response = restClient.get().uri(uri -> uri.path("/symbol_search").queryParam("symbol", termo).build())
                    .header(HttpHeaders.AUTHORIZATION, authorization()).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, external) -> throwForHttpError(external))
                    .body(TwelveDataSymbolSearchResponse.class);
            throwForStructuredError(response == null ? null : response.status(), response == null ? null : response.code(), response == null ? null : response.message());
            if (response == null || response.data() == null) throw new InvalidStockDataResponseException();
            return response.data().stream().filter(java.util.Objects::nonNull).filter(this::isEligible)
                    .filter(item -> !blank(item.symbol()))
                    .map(item -> new StockSuggestionData(normalize(item.symbol()), item.instrument_name(), Mercado.ESTADOS_UNIDOS, Moeda.USD)).toList();
        } catch (StockTickerNotFoundException | InvalidStockDataResponseException | StockProviderUnavailableException exception) { throw exception;
        } catch (ResourceAccessException exception) { throw new StockProviderUnavailableException(exception);
        } catch (RestClientException exception) { throw new InvalidStockDataResponseException(exception); }
    }

    @Override
    public StockCatalogPageData catalogar(String termo, int page, int size) {
        if (!StringUtils.hasText(apiKey)) throw new StockProviderUnavailableException();
        List<StockCatalogItemData> catalog = loadCatalog();
        String normalizedTerm = normalize(termo);
        List<StockCatalogItemData> filtered = !StringUtils.hasText(normalizedTerm) ? catalog : catalog.stream()
                .filter(item -> item.ticker().contains(normalizedTerm)
                        || item.nomeEmpresa().toUpperCase(Locale.ROOT).contains(normalizedTerm))
                .toList();
        int from = Math.min(Math.multiplyExact(page, size), filtered.size());
        int to = Math.min(from + size, filtered.size());
        return new StockCatalogPageData(filtered.subList(from, to), page, size, to < filtered.size(), (long) filtered.size());
    }

    private List<StockCatalogItemData> loadCatalog() {
        CatalogCache current = catalogCache;
        Instant now = clock.instant();
        if (current != null && now.isBefore(current.expiresAt())) return current.items();
        synchronized (this) {
            current = catalogCache;
            now = clock.instant();
            if (current != null && now.isBefore(current.expiresAt())) return current.items();
            List<StockCatalogItemData> loaded = fetchCatalog();
            catalogCache = new CatalogCache(loaded, now.plus(catalogTtl));
            return loaded;
        }
    }

    private List<StockCatalogItemData> fetchCatalog() {
        try {
            TwelveDataStocksResponse response = restClient.get().uri(uri -> uri.path("/stocks")
                            .queryParam("country", UNITED_STATES)
                            .queryParam("type", COMMON_STOCK)
                            .queryParam("format", "JSON")
                            .queryParam("show_plan", false)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, authorization())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, external) -> throwForHttpError(external))
                    .body(TwelveDataStocksResponse.class);
            throwForStructuredError(response == null ? null : response.status(), response == null ? null : response.code(), response == null ? null : response.message());
            if (response == null || response.data() == null) throw new InvalidStockDataResponseException();
            Map<String, List<TwelveDataStocksResponse.Result>> byTicker = response.data().stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(this::isEligible)
                    .filter(item -> StringUtils.hasText(item.symbol()) && StringUtils.hasText(item.name()))
                    .collect(java.util.stream.Collectors.groupingBy(item -> normalize(item.symbol()), LinkedHashMap::new, java.util.stream.Collectors.toList()));
            return byTicker.values().stream()
                    .filter(matches -> matches.size() == 1)
                    .map(matches -> toCatalogItem(matches.get(0)))
                    .sorted(java.util.Comparator.comparing(StockCatalogItemData::ticker))
                    .toList();
        } catch (StockTickerNotFoundException | InvalidStockDataResponseException | StockProviderUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new StockProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidStockDataResponseException(exception);
        }
    }

    private StockCatalogItemData toCatalogItem(TwelveDataStocksResponse.Result item) {
        return new StockCatalogItemData(
                normalize(item.symbol()), item.name().trim(), Mercado.ESTADOS_UNIDOS, Moeda.USD,
                trimToNull(item.exchange()), trimToNull(item.mic_code()), null, null
        );
    }

    private boolean isEligible(TwelveDataStocksResponse.Result result) {
        return UNITED_STATES.equals(result.country())
                && COMMON_STOCK.equals(result.type())
                && USD.equals(result.currency())
                && isSupportedCatalogVenue(result)
                && isNormalUsStockSymbol(result.symbol());
    }

    private boolean isSupportedCatalogVenue(TwelveDataStocksResponse.Result result) {
        String exchange = trimToNull(result.exchange());
        String micCode = trimToNull(result.mic_code());
        return CATALOG_EXCHANGES.contains(exchange) || CATALOG_MIC_CODES.contains(micCode);
    }

    private boolean isNormalUsStockSymbol(String symbol) {
        return StringUtils.hasText(symbol) && NORMAL_US_STOCK_SYMBOL.matcher(symbol.trim()).matches();
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
                .onStatus(HttpStatusCode::isError, (request, external) -> throwForHttpError(external))
                .body(TwelveDataSymbolSearchResponse.class);

        throwForStructuredError(response == null ? null : response.status(), response == null ? null : response.code(), response == null ? null : response.message());
        if (response == null || response.data() == null) {
            throw new InvalidStockDataResponseException();
        }
        String normalizedTicker = normalize(ticker);
        List<TwelveDataSymbolSearchResponse.Result> matches = response.data().stream()
                .filter(result -> result != null && normalizedTicker.equals(normalize(result.symbol())))
                .filter(this::isEligible)
                .toList();
        if (matches.isEmpty()) {
            throw new StockTickerNotFoundException();
        }
        return selectPreferredInstrument(matches);
    }

    private TwelveDataSymbolSearchResponse.Result selectPreferredInstrument(
            List<TwelveDataSymbolSearchResponse.Result> eligibleMatches
    ) {
        List<TwelveDataSymbolSearchResponse.Result> primaryMatches = eligibleMatches.stream()
                .filter(result -> PRIMARY_VENUES.contains(venueOf(result)))
                .toList();
        if (primaryMatches.size() == 1) {
            return primaryMatches.get(0);
        }
        if (!primaryMatches.isEmpty()) {
            throw new StockVenueAmbiguityException();
        }

        List<TwelveDataSymbolSearchResponse.Result> iexMatches = eligibleMatches.stream()
                .filter(result -> IEX_VENUE.equals(venueOf(result)))
                .toList();
        if (iexMatches.size() == 1 && eligibleMatches.size() == 1) {
            return iexMatches.get(0);
        }
        throw new StockVenueAmbiguityException();
    }

    private Venue venueOf(TwelveDataSymbolSearchResponse.Result result) {
        return new Venue(trimToNull(result.exchange()), trimToNull(result.mic_code()));
    }

    private TwelveDataQuoteResponse quote(String ticker) {
        TwelveDataQuoteResponse response = restClient.get()
                .uri(uri -> uri.path("/quote").queryParam("symbol", ticker).build())
                .header(HttpHeaders.AUTHORIZATION, authorization())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, external) -> throwForHttpError(external))
                .body(TwelveDataQuoteResponse.class);
        throwForStructuredError(response == null ? null : response.status(), response == null ? null : response.code(), response == null ? null : response.message());
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

    private void throwForHttpError(ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        if (status == 401 || status == 403 || status == 429 || status >= 500) {
            throw new StockProviderUnavailableException();
        }
        if (status == 404) {
            throw new StockTickerNotFoundException();
        }
        if (status == 400) {
            try {
                TwelveDataErrorResponse error = parseError(response);
                throwForStructuredError(error.status(), error.code(), error.message());
            } catch (IOException exception) {
                throw new InvalidStockDataResponseException(exception);
            }
        }
        throw new InvalidStockDataResponseException();
    }

    private void throwForStructuredError(String status, Integer code, String message) {
        if (!"error".equalsIgnoreCase(status)) {
            return;
        }
        if (isKnownTickerError(code, message)) {
            throw new StockTickerNotFoundException();
        }
        if (code != null && (code == 401 || code == 403 || code == 429 || code >= 500)) {
            throw new StockProviderUnavailableException();
        }
        throw new InvalidStockDataResponseException();
    }

    private boolean isKnownTickerError(Integer code, String message) {
        return code != null && (code == 404 || (code == 400 && KNOWN_TICKER_ERROR_MESSAGES.contains(normalize(message))));
    }

    private TwelveDataErrorResponse parseError(ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return new TwelveDataErrorResponse(stringField(STATUS_FIELD, body), integerField(body), stringField(MESSAGE_FIELD, body));
    }

    private String stringField(Pattern pattern, String body) {
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Integer integerField(String body) {
        Matcher matcher = CODE_FIELD.matcher(body);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record CatalogCache(List<StockCatalogItemData> items, Instant expiresAt) {
        private CatalogCache {
            items = List.copyOf(items);
        }
    }

    private record Venue(String exchange, String micCode) {
    }
}
