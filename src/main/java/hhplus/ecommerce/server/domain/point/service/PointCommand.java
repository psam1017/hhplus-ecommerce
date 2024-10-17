package hhplus.ecommerce.server.domain.point.service;

public class PointCommand {

    public record ChargePoint(
            Integer chargeAmount
    ) {
    }
}
