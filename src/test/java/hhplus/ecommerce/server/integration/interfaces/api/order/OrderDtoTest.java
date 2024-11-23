package hhplus.ecommerce.server.integration.interfaces.api.order;

import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import hhplus.ecommerce.server.interfaces.api.order.OrderDto;
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
public class OrderDtoTest extends TestContainerEnvironment {

    @Autowired
    Validator validator;

    @DisplayName("주문할 때 주문할 정보 배열을 보내지 않으면 유효성 검증에 실패한다.")
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

    @DisplayName("주문할 때 주문할 정보 배열이 비어있으면 유효성 검증에 실패한다.")
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

    @DisplayName("주문할 때 주문할 정보 배열의 값에 상품 아이디와 구매수량을 입력하지 않으면 유효성 검증에 실패한다.")
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

    @DisplayName("주문할 때 상품의 구매수량으로 양수를 입력하지 않으면 유효성 검증에 실패한다.")
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

    @DisplayName("주문 생성 정보를 검증할 때는 주문 정보 배열의 값도 검증할 수 있다.")
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
