package hhplus.ecommerce.server.integration.interfaces.order;

import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import hhplus.ecommerce.server.interfaces.controller.order.OrderDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("DataFlowIssue")
public class OrderDtoTest extends SpringBootTestEnvironment {

    @Autowired
    Validator validator;

    @DisplayName("주문할 때 주문할 정보 배열을 보내야 한다.")
    @Test
    void OrderCreateNotNull() {
        // given
        OrderDto.OrderCreate target = new OrderDto.OrderCreate(null);

        // when
        Set<ConstraintViolation<OrderDto.OrderCreate>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("items", NotEmpty.class));
    }

    @DisplayName("주문할 때 주문할 정보 배열은 비어있으면 안된다.")
    @Test
    void OrderCreateNotEmpty() {
        // given
        OrderDto.OrderCreate target = new OrderDto.OrderCreate(List.of());

        // when
        Set<ConstraintViolation<OrderDto.OrderCreate>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("items", NotEmpty.class));
    }

    @DisplayName("주문할 때 주문할 상품의 식별자와 수량을 반드시 보내야 한다.")
    @Test
    void OrderCreateItemNotNull() {
        // given
        OrderDto.OrderCreateItem target = new OrderDto.OrderCreateItem(null, null);

        // when
        Set<ConstraintViolation<OrderDto.OrderCreateItem>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(2)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("itemId", NotNull.class), tuple("amount", NotNull.class));
    }

    @DisplayName("주문할 때 상품의 구매수량은 양수를 입력해야 한다.")
    @Test
    void OrderCreateItemPositive() {
        // given
        OrderDto.OrderCreateItem target = new OrderDto.OrderCreateItem(1L, -1);

        // when
        Set<ConstraintViolation<OrderDto.OrderCreateItem>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(1)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(tuple("amount", Positive.class));
    }

    @DisplayName("주문 생성 시 구매 정보 배열을 검증할 수 있다.")
    @Test
    void OrderCreateValid() {
        // given
        OrderDto.OrderCreate target = new OrderDto.OrderCreate(List.of(
                new OrderDto.OrderCreateItem(null, null)
        ));

        // when
        Set<ConstraintViolation<OrderDto.OrderCreate>> validations = validator.validate(target);

        // then
        assertThat(validations).hasSize(2)
                .extracting(cv -> tuple(cv.getPropertyPath().toString(), cv.getConstraintDescriptor().getAnnotation().annotationType()))
                .contains(
                        tuple("items[0].itemId", NotNull.class),
                        tuple("items[0].amount", NotNull.class)
                );
    }
}
