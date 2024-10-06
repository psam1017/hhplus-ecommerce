package hhplus.ecommerce.server.spring.docs.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Product 환경에 영향을 주지 않도록 test 패키지에서 생성하십시오.
 */
@RequestMapping("/api/open")
@RestController
public class ExceptionController {

    @GetMapping("/api-ex")
    public Map<String, Map<String, String>> findApiExceptionInstances() {
        Map<String, String> apiExceptionInstances = ApiExceptionUtils.findApiExceptionInstances();
        return Map.of("ApiException", apiExceptionInstances);
    }
}
