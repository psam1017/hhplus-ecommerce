package hhplus.ecommerce.server.integration.interfaces.exception;

import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import hhplus.ecommerce.server.interfaces.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiExceptionTest extends SpringBootTestEnvironment {

    private final String basePackagePath = "hhplus.ecommerce.server";

    /**
     * API 예외 코드들을 관리하는 정책으로, 클래스 이름 자체를 사용하고자 합니다.
     * 이때 클래스 이름이 중복되어선 안 되기 때문에, ApiException 을 상속한 모든 클래스를 찾고 중복을 검사해야 합니다.
     * 이에 reflections 라는 라이브러리를 사용하여 ApiException 을 상속한 클래스들을 찾고, 중복되는 클래스 이름이 없는지 검사합니다.
     * 또한 이를 활용하면 추후에 예외코드들을 별도의 문서로 정리할 때도 유용하게 사용할 수 있습니다.
     */
    @DisplayName("ApiException 을 상속한 클래스들은 이름이 중복이 없어야 한다.")
    @Test
    void check() {
        // given
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes, Scanners.Resources)
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().includePackage(basePackagePath))
        );
        Set<Class<? extends ApiException>> classes = reflections.getSubTypesOf(ApiException.class);
        Map<String, Object> errorCodes = new HashMap<>();

        // when
        // then
        for (Class<? extends ApiException> clazz : classes) {
            String code = clazz.getSimpleName();
            assertThat(errorCodes).doesNotContainKey(code);
            errorCodes.put(code, null);
        }
    }
}
