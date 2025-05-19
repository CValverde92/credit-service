package com.nttdatabank.credit_service.controller;


import com.nttdatabank.credit_service.api.CreditsApi;
import com.nttdatabank.credit_service.exception.BusinessException;
import com.nttdatabank.credit_service.model.*;
import com.nttdatabank.credit_service.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CreditController implements CreditsApi {
    private final CreditService creditService;

    @Override
    public Mono<ResponseEntity<CreditResponse>> createCredit(
            @Valid @RequestBody Mono<CreditRequest> creditRequest,
            final ServerWebExchange exchange) {
        return creditRequest
                .flatMap(creditService::create)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(this::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCreditById(
            @PathVariable("id") String id,
            final ServerWebExchange exchange) {
        return creditService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> getCreditById(
            @PathVariable("id") String id,
            final ServerWebExchange exchange) {
        return creditService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getAllCredits(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(creditService.findAll()));
    }

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

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getCreditsByCustomerId(
            @PathVariable("customerId") String customerId,
            final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(creditService.findByCustomerId(customerId)));
    }

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

    public <T> Mono<ResponseEntity<T>> handleError(Throwable e) {
        if (e instanceof BusinessException) {
            return Mono.just(ResponseEntity.badRequest().build());
        } else if (e instanceof IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
