package com.nttdatabank.credit_service.controller;


import com.nttdatabank.credit_service.api.CreditsApi;
import com.nttdatabank.credit_service.exception.BusinessException;
import com.nttdatabank.credit_service.model.*;
import com.nttdatabank.credit_service.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * Controller for credit operations
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credits")
public class CreditController implements CreditsApi {
    private final CreditService creditService;

    // Create new credit
    @Override
    public Mono<ResponseEntity<CreditResponse>> createCredit(
            @Valid @RequestBody Mono<CreditRequest> creditRequest,
            final ServerWebExchange exchange) {
        return creditRequest
                .flatMap(creditService::create)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(this::handleError);
    }

    // Delete credit by ID
    @Override
    public Mono<ResponseEntity<Void>> deleteCreditById(
            @PathVariable("id") String id,
            final ServerWebExchange exchange) {
        return creditService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    // Get credit by ID
    @Override
    public Mono<ResponseEntity<CreditResponse>> getCreditById(
            @PathVariable("id") String id,
            final ServerWebExchange exchange) {
        return creditService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Get all credits
    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getAllCredits(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(creditService.findAll()));
    }

    // Update credit by ID
    @Override
    public Mono<ResponseEntity<CreditResponse>> updateCreditById(
            @PathVariable("id") String id,
            @Valid @RequestBody Mono<CreditUpdateRequest> creditUpdateRequest,
            final ServerWebExchange exchange) {
        return creditUpdateRequest
                .flatMap(request -> creditService.update(id, request))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().build()));
    }

    // Get credits by customer ID
    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getCreditsByCustomerId(
            @PathVariable("customerId") String customerId,
            final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(creditService.findByCustomerId(customerId)));
    }

    // Charge amount to credit
    @Override
    public Mono<ResponseEntity<CreditResponse>> chargeCredit(
            @PathVariable("creditId") String creditId,
            @Valid @RequestBody Mono<CreditChargeRequest> creditChargeRequest,
            final ServerWebExchange exchange) {
        return creditChargeRequest
                .flatMap(request -> creditService.chargeCredit(creditId, request))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // Make payment to credit
    @Override
    public Mono<ResponseEntity<CreditResponse>> makeCreditPayment(
            @PathVariable("creditId") String creditId,
            @Valid @RequestBody Mono<CreditPaymentRequest> creditPaymentRequest,
            final ServerWebExchange exchange) {
        return creditPaymentRequest
                .flatMap(request -> creditService.makePayment(creditId, request))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // Handle errors
    public <T> Mono<ResponseEntity<T>> handleError(Throwable e) {
        if (e instanceof BusinessException) {
            return Mono.just(ResponseEntity.badRequest().build());
        } else if (e instanceof IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    // Get available credit limit
    @GetMapping("/{creditId}/available-limit")
    public Mono<BigDecimal> getAvailableCreditLimit(@PathVariable String creditId) {
        return creditService.findById(creditId)
                .map(credit -> credit.getCreditLimit().subtract(credit.getUsedCredit()));
    }
}
