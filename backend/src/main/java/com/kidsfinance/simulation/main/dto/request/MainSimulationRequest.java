package com.kidsfinance.simulation.main.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MainSimulationRequest(

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal cash,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal saving,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal deposit,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal bond,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal investment,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal spending,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1000.00")
        BigDecimal business

) {

    public BigDecimal total() {

        if (cash == null
                || saving == null
                || deposit == null
                || bond == null
                || investment == null
                || spending == null
                || business == null) {

            return BigDecimal.ZERO;
        }

        return cash
                .add(saving)
                .add(deposit)
                .add(bond)
                .add(investment)
                .add(spending)
                .add(business);
    }

    @AssertTrue(
            message = "Allocations must total exactly 1000.00 AZN"
    )
    public boolean isAllocationTotalValid() {

        return total().compareTo(
                new BigDecimal("1000.00")
        ) == 0;
    }
}