package hhplus.ecommerce.server.unit.infrastructure;

import hhplus.ecommerce.server.infrastructure.lock.CustomSpringELParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CustomSpringELParserTest {

    @DisplayName("Spring EL 을 이용하여 동적으로 값을 가져올 수 있다.")
    @Test
    void getDynamicValueWithSpringEL() {
        // given
        String[] parameterNames = new String[]{"users"};
        Object[] args = new Object[]{1};
        String key = "'users:' + #users";

        // when
        String value = (String) CustomSpringELParser.getDynamicValue(parameterNames, args, key);

        // then
        Assertions.assertThat(value).isEqualTo("users:1");
    }
}