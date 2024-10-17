package hhplus.ecommerce.server.integration.interfaces.cart;

import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import hhplus.ecommerce.server.interfaces.controller.cart.CartDto;
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
public class CartDtoTest extends SpringBootTestEnvironment {

    @Autowired
    Validator validator;

    @DisplayName("장바구니에 상품의 수량을 반드시 입력해야 한다.")
    @Test
    void CartItemPutValidation() {
        // given
        CartDto.CartItemPut target = new CartDto.CartItemPut(null);

        // when
        Set<ConstraintViolation<CartDto.CartItemPut>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("amount", NotNull.class));
    }

    @DisplayName("장바구니에 상품의 수량은 양수를 입력해야 한다.")
    @Test
    void CartItemPutPositive() {
        // given
        CartDto.CartItemPut target = new CartDto.CartItemPut(-1);

        // when
        Set<ConstraintViolation<CartDto.CartItemPut>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("amount", Positive.class));
    }
}
