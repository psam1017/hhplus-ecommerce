package hhplus.ecommerce.server.unit.interfaces.exception;

import hhplus.ecommerce.server.interfaces.exception.ApiException;
import hhplus.ecommerce.server.interfaces.exception.ErrorCodeDuplicationChecker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ErrorCodeDuplicationCheckerTest {

    private final ErrorCodeDuplicationChecker sut = new ErrorCodeDuplicationChecker();

    private static class Exception1 extends ApiException {
        public Exception1(String message) {
            super(message);
        }
    }

    private static class Wrapper {
        private static class Exception1 extends ApiException {
            public Exception1(String message) {
                super(message);
            }
        }
    }

    @DisplayName("중복되는 ErrorCode 가 있으면 예외가 발생한다.")
    @Test
    void check() {
        // when
        // then
        Assertions.assertThatThrownBy(sut::check)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ErrorCodeDuplicationChecker: ErrorCode is duplicated: " + Exception1.class.getSimpleName());
    }
}
