package hhplus.ecommerce.server.integration.interfaces.point;

import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import hhplus.ecommerce.server.interfaces.controller.cart.CartDto;
import hhplus.ecommerce.server.interfaces.controller.point.PointDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("DataFlowIssue")
public class PointDtoTest extends SpringBootTestEnvironment {

    @Autowired
    Validator validator;

    @DisplayName("장바구니에 상품의 수량을 반드시 입력해야 한다.")
    @Test
    void PointCreateNotNull() {
        // given
        PointDto.PointCreate target = new PointDto.PointCreate(null);

        // when
        Set<ConstraintViolation<PointDto.PointCreate>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("chargeAmount", NotNull.class));
    }

    @DisplayName("포인트 수량은 양수를 입력해야 한다.")
    @Test
    void PointCreatePositive() {
        // given
        PointDto.PointCreate target = new PointDto.PointCreate(-1);

        // when
        Set<ConstraintViolation<PointDto.PointCreate>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("chargeAmount", Positive.class));
    }

    @DisplayName("삭제할 장바구니 아이디는 필수로 입력해야 한다.")
    @Test
    void CartItemDeleteResponseNotNull() {
        // given
        CartDto.CartItemDeleteResponse target = new CartDto.CartItemDeleteResponse(null);

        // when
        Set<ConstraintViolation<CartDto.CartItemDeleteResponse>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("id", NotNull.class));
    }
}
