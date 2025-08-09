package com.loopers.interfaces.api.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PointDto {
    public static class V1 {
        public static class Charge {
            public record Request(
                @NotNull String userId,
                @NotNull @Min(1) Long amount
            ) {}

            public record Response(
                String userId,
                Long totalPoint
            ) {
                public static Response from(com.loopers.application.point.PointResult.ChargeResult result) {
                    return new Response(
                        result.userId(),
                        result.balance()
                    );
                }
            }
        }
        
        public static class GetPoint {
            public record Response(
                String userId,
                Long point
            ) {
                public static Response from(com.loopers.application.point.PointResult.Detail result) {
                    return new Response(
                        result.userId(),
                        result.balance()
                    );
                }
            }
        }
    }
}
