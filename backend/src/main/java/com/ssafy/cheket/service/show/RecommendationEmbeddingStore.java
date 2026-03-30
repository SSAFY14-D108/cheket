package com.ssafy.cheket.service.show;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendationEmbeddingStore {

    private static final String SHOW_EMBEDDINGS_TABLE = "show_embeddings";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public Map<Long, List<Double>> findEmbeddingsByShowIds(List<Long> showIds) {
        if (showIds == null || showIds.isEmpty() || !tableExists(SHOW_EMBEDDINGS_TABLE)) {
            return Map.of();
        }

        List<Long> uniqueIds = showIds.stream().distinct().toList();
        String placeholders = uniqueIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = """
            select show_id, embedding_json
            from show_embeddings
            where show_id in (%s)
            """.formatted(placeholders);

        Map<Long, List<Double>> embeddings = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            long showId = rs.getLong("show_id");
            String embeddingJson = rs.getString("embedding_json");
            List<Double> embedding = parseEmbeddingJson(embeddingJson);
            if (!embedding.isEmpty()) {
                embeddings.put(showId, embedding);
            }
        }, uniqueIds.toArray());
        return embeddings;
    }

    public List<Double> buildWeightedUserEmbedding(List<WeightedShowSignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> signalWeights = new LinkedHashMap<>();
        for (WeightedShowSignal signal : signals) {
            signalWeights.merge(signal.showId(), signal.weight(), Double::sum);
        }

        Map<Long, List<Double>> embeddings = findEmbeddingsByShowIds(new ArrayList<>(signalWeights.keySet()));
        if (embeddings.isEmpty()) {
            return List.of();
        }

        int dimension = embeddings.values().stream().findFirst().map(List::size).orElse(0);
        if (dimension == 0) {
            return List.of();
        }

        double[] accumulator = new double[dimension];
        double totalWeight = 0.0;

        for (Map.Entry<Long, Double> entry : signalWeights.entrySet()) {
            List<Double> embedding = embeddings.get(entry.getKey());
            if (embedding == null || embedding.size() != dimension) {
                continue;
            }

            double weight = entry.getValue();
            for (int i = 0; i < dimension; i++) {
                accumulator[i] += embedding.get(i) * weight;
            }
            totalWeight += weight;
        }

        if (totalWeight == 0.0) {
            return List.of();
        }

        List<Double> averaged = new ArrayList<>(dimension);
        for (double value : accumulator) {
            averaged.add(value / totalWeight);
        }
        return averaged;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
            select count(*)
            from information_schema.tables
            where table_schema = database()
              and table_name = ?
            """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private List<Double> parseEmbeddingJson(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(embeddingJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    public record WeightedShowSignal(Long showId, double weight) {
    }
}
