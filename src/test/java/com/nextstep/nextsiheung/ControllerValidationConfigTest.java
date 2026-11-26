package com.nextstep.nextsiheung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

// 검증 어노테이션(@Valid, @Min 등)을 ApiDocs 인터페이스가 아니라 컨트롤러 구현 메서드에 달면
// 요청 시점에 HV000151로 500이 난다. DB 없이 모든 컨트롤러의 검증 메타데이터를 미리 만들어 본다.
class ControllerValidationConfigTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 컨트롤러는_인터페이스의_검증_설정을_바꾸지_않는다() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> controllers = scanner.findCandidateComponents("com.nextstep.nextsiheung").stream()
                .map(bd -> bd.getBeanClassName())
                .toList();

        assertThat(controllers).isNotEmpty();
        for (String name : controllers) {
            Class<?> type = Class.forName(name);
            assertThatCode(() -> validator.getConstraintsForClass(type))
                    .as(name)
                    .doesNotThrowAnyException();
        }
    }
}
