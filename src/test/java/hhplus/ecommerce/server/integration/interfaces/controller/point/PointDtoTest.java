package hhplus.ecommerce.server.integration.interfaces.controller.point;

import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
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

    @DisplayName("포인트를 충전할 때 충전할 포인트 수량을 입력하지 않으면 유효성 검증에 실패한다.")
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

    @DisplayName("포인트를 충전할 때 충전할 포인트 수량이 양수가 아니면 유효성 검증에 실패한다.")
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
}
