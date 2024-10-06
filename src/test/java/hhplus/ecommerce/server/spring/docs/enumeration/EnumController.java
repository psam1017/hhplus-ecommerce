package hhplus.ecommerce.server.spring.docs.enumeration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Product 환경에 영향을 주지 않도록 test 패키지에서 생성하십시오.
 */
@RequestMapping("/api/open")
@RestController
public class EnumController {

    @GetMapping("/enums")
    public Map<String, Map<String, String>> findDocumentableInstances() {
        return DocumentableUtils.findDocumentableInstances();
    }
}
