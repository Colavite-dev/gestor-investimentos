package com.colavite.gestor_investimento.integration.cvm;

import com.colavite.gestor_investimento.config.CvmProperties;
import com.colavite.gestor_investimento.exception.CvmProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCvmResponseException;
import com.colavite.gestor_investimento.validation.CnpjUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class CvmParticipantAdapter implements CvmParticipantProvider {

    private static final String BASIC_REGISTRY_FILE = "cad_intermed.csv";
    private final RestClient restClient;
    private final CvmProperties properties;
    private final Clock clock;
    private volatile Snapshot snapshot;

    public CvmParticipantAdapter(
            @Qualifier("cvmRestClient") RestClient restClient,
            CvmProperties properties,
            Clock clock
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public Optional<CvmParticipantData> consultar(String cnpj) {
        String normalizedCnpj = CnpjUtils.somenteDigitos(cnpj);
        return Optional.ofNullable(currentSnapshot().participants().get(normalizedCnpj));
    }

    private Snapshot currentSnapshot() {
        Snapshot current = snapshot;
        Instant now = clock.instant();
        if (current != null && now.isBefore(current.loadedAt().plus(properties.refreshInterval()))) {
            return current;
        }
        synchronized (this) {
            current = snapshot;
            now = clock.instant();
            if (current != null && now.isBefore(current.loadedAt().plus(properties.refreshInterval()))) {
                return current;
            }
            Snapshot refreshed = new Snapshot(loadParticipants(), now);
            snapshot = refreshed;
            return refreshed;
        }
    }

    private Map<String, CvmParticipantData> loadParticipants() {
        try {
            byte[] zip = restClient.get()
                    .uri(properties.datasetUrl())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new CvmProviderUnavailableException();
                    })
                    .body(byte[].class);
            if (zip == null || zip.length == 0) {
                throw new InvalidCvmResponseException();
            }
            return parseZip(zip);
        } catch (CvmProviderUnavailableException | InvalidCvmResponseException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new CvmProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidCvmResponseException(exception);
        }
    }

    private Map<String, CvmParticipantData> parseZip(byte[] bytes) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (BASIC_REGISTRY_FILE.equalsIgnoreCase(entry.getName())) {
                    return parseCsv(zip);
                }
            }
            throw new InvalidCvmResponseException();
        } catch (IOException exception) {
            throw new InvalidCvmResponseException(exception);
        }
    }

    private Map<String, CvmParticipantData> parseCsv(ZipInputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.ISO_8859_1))) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new InvalidCvmResponseException();
            Map<String, Integer> headers = headers(parseCsvLine(headerLine));
            int cnpjColumn = requiredHeader(headers, "CNPJ");
            int statusColumn = requiredHeader(headers, "SIT", "SITUACAO_REGISTRO", "SIT_REGISTRO");
            int categoryColumn = requiredHeader(headers, "TP_PARTIC", "TP_REGISTRO", "CATEGORIA", "CATEGORIA_REGISTRO");
            Map<String, List<CvmParticipantData>> participants = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> values = parseCsvLine(line);
                String cnpj = valueAt(values, cnpjColumn);
                String status = valueAt(values, statusColumn);
                String category = valueAt(values, categoryColumn);
                if (cnpj == null || status == null || category == null) {
                    throw new InvalidCvmResponseException();
                }
                String normalizedCnpj = CnpjUtils.somenteDigitos(cnpj);
                if (normalizedCnpj.length() != 14) throw new InvalidCvmResponseException();
                participants.computeIfAbsent(normalizedCnpj, ignored -> new ArrayList<>())
                        .add(new CvmParticipantData(normalizedCnpj, status.trim(), category.trim()));
            }
            return selectRepresentatives(participants);
        }
    }

    private Map<String, Integer> headers(List<String> values) {
        Map<String, Integer> result = new HashMap<>();
        for (int index = 0; index < values.size(); index++) {
            result.put(normalizeHeader(values.get(index)), index);
        }
        return result;
    }

    private int requiredHeader(Map<String, Integer> headers, String... acceptedNames) {
        for (String acceptedName : acceptedNames) {
            Integer index = headers.get(acceptedName);
            if (index != null) return index;
        }
        throw new InvalidCvmResponseException();
    }

    private String normalizeHeader(String value) {
        return CvmParticipantEligibility.normalize(value).replace(' ', '_');
    }

    private String valueAt(List<String> values, int index) {
        return index < values.size() && !values.get(index).isBlank() ? values.get(index) : null;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append(character);
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ';' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (quoted) throw new InvalidCvmResponseException();
        values.add(value.toString());
        return values;
    }

    private Map<String, CvmParticipantData> selectRepresentatives(Map<String, List<CvmParticipantData>> grouped) {
        Map<String, CvmParticipantData> representatives = new HashMap<>();
        Comparator<CvmParticipantData> stableOrder = Comparator
                .comparing((CvmParticipantData participant) -> !CvmParticipantEligibility.isEligible(participant))
                .thenComparing(participant -> CvmParticipantEligibility.normalize(participant.situacaoRegistro()))
                .thenComparing(participant -> CvmParticipantEligibility.normalize(participant.categoria()));
        grouped.forEach((cnpj, records) -> representatives.put(cnpj, records.stream().min(stableOrder)
                .orElseThrow(InvalidCvmResponseException::new)));
        return Map.copyOf(representatives);
    }

    private record Snapshot(Map<String, CvmParticipantData> participants, Instant loadedAt) {
    }
}
