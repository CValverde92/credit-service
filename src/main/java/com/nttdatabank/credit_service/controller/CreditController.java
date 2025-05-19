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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class CreditController implements CreditsApi {

    private final CreditService creditService;

    @Override
    public Mono<ResponseEntity<CreditResponse>> creditsPost(
            @Valid @RequestBody Mono<CreditRequest> creditRequest,
            ServerWebExchange exchange) {
        return creditRequest
                .flatMap(creditService::create)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response))
                .onErrorResume(BusinessException.class, e ->
                        Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.internalServerError().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> creditsIdDelete(
            @PathVariable("id") String id,
            ServerWebExchange exchange) {
        return creditService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> creditsIdGet(
            @PathVariable("id") String id,
            ServerWebExchange exchange) {
        return creditService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> creditsGet(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(creditService.findAll()));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> creditsIdPut(
            @PathVariable("id") String id,
            @Valid @RequestBody Mono<CreditUpdateRequest> creditUpdateRequest,
            ServerWebExchange exchange) {
        return creditUpdateRequest
                .flatMap(request -> creditService.update(id, request))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> creditsCustomerCustomerIdGet(
            @PathVariable("customerId") String customerId,
            ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(creditService.findByCustomerId(customerId)));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> creditsCreditIdChargePost(
            @PathVariable("creditId") String creditId,
            @Valid @RequestBody Mono<CreditChargeRequest> creditChargeRequest,
            ServerWebExchange exchange) {
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
    public Mono<ResponseEntity<CreditResponse>> creditsCreditIdPaymentPost(
            @PathVariable("creditId") String creditId,
            @Valid @RequestBody Mono<CreditPaymentRequest> creditPaymentRequest,
            ServerWebExchange exchange) {
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

}
