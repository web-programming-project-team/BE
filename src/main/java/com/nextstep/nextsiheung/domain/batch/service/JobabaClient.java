package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.dto.JobabaRow;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 경기데이터드림 「경기도일자리재단 잡아바 채용정보」 호출
// 응답은 JSON인데 Content-Type이 text/html로 와서, 문자열로 받은 뒤 직접 파싱한다
@Component
public class JobabaClient {

    private static final String URL =
            "https://openapi.gg.go.kr/GGJOBABARECRUSTM?KEY={key}&Type=json&pIndex={page}&pSize={size}";
    private static final String SERVICE = "GGJOBABARECRUSTM";
    private static final String NO_DATA = "INFO-200";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String apiKey;

    public JobabaClient(JsonMapper jsonMapper, @Value("${app.external.gg-job-api-key:}") String apiKey) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
    }

    // page는 1부터. size는 최대 1000
    public Page fetch(int page, int size) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("app.external.gg-job-api-key가 비어 있습니다 (application-local.yml)");
        }
        String body = restClient.get()
                .uri(URL, apiKey, page, size)
                .retrieve()
                .body(String.class);
        return parse(body);
    }

    Page parse(String body) {
        JsonNode root = jsonMapper.readTree(body);
        JsonNode service = root.path(SERVICE);
        if (!service.isArray()) {
            // 오류·데이터 없음일 때는 {"RESULT":{"CODE":"...","MESSAGE":"..."}}만 온다
            String code = root.path("RESULT").path("CODE").asString("");
            if (NO_DATA.equals(code)) {
                return new Page(0, List.of());
            }
            throw new IllegalStateException("잡아바 API 오류: " + code + " " + root.path("RESULT").path("MESSAGE").asString(body));
        }

        int totalCount = 0;
        List<JobabaRow> rows = new ArrayList<>();
        for (JsonNode part : service) {
            for (JsonNode head : part.path("head")) {
                if (head.has("list_total_count")) {
                    totalCount = head.get("list_total_count").asInt();
                }
            }
            for (JsonNode row : part.path("row")) {
                rows.add(jsonMapper.treeToValue(row, JobabaRow.class));
            }
        }
        return new Page(totalCount, rows);
    }

    public record Page(int totalCount, List<JobabaRow> rows) {
    }
}
