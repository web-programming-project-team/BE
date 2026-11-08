package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.dto.OntongPolicyRow;
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

// 온통청년 「청년정책 API」 호출. zipCd(법정동 시군구 코드)를 주면 그 지역 청년이 신청할 수 있는 정책만 온다
// (전국 공통·광역 단위 정책은 소속 시군구 코드를 모두 담고 있어서 함께 걸린다)
@Component
public class OntongClient {

    private static final String URL = "https://www.youthcenter.go.kr/go/ythip/getPlcy"
            + "?apiKeyNm={key}&pageNum={page}&pageSize={size}&rtnType=json&zipCd={zipCd}";
    private static final int OK = 200;

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String apiKey;

    public OntongClient(JsonMapper jsonMapper, @Value("${app.external.ontong-api-key:}") String apiKey) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
    }

    // page는 1부터
    public Page fetch(int page, int size, String zipCd) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("app.external.ontong-api-key가 비어 있습니다 (application-local.yml)");
        }
        String body = restClient.get()
                .uri(URL, apiKey, page, size, zipCd)
                .retrieve()
                .body(String.class);
        return parse(body);
    }

    Page parse(String body) {
        JsonNode root = jsonMapper.readTree(body);
        int code = root.path("resultCode").asInt(-1);
        if (code != OK) {
            throw new IllegalStateException("온통청년 API 오류: " + code + " " + root.path("resultMessage").asString(""));
        }
        JsonNode result = root.path("result");
        int totalCount = result.path("pagging").path("totCount").asInt(0);
        List<OntongPolicyRow> rows = new ArrayList<>();
        for (JsonNode row : result.path("youthPolicyList")) {
            rows.add(jsonMapper.treeToValue(row, OntongPolicyRow.class));
        }
        return new Page(totalCount, rows);
    }

    public record Page(int totalCount, List<OntongPolicyRow> rows) {
    }
}
