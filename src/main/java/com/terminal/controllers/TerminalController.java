package com.terminal.controllers;

import com.terminal.domain.BuyResponseDto;
import com.terminal.domain.CardDto;
import com.terminal.exceptions.CardNotFoundException;
import com.terminal.exceptions.IncorrectPinException;
import com.terminal.exceptions.NoEnouthMoneyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping
@Api(tags = {"Терминал"})
@SwaggerDefinition(tags = {
        @Tag(name = "Терминал", description = "Позволяет осуществлять оплату")
})
@CrossOrigin(origins = "*")
@Slf4j
public class TerminalController {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${url}")
    private String url = "";

    @GetMapping("/pay")
    @ApiOperation(value = "Оплата", notes = "Оплата картой")
    public void pay(@RequestParam String card,
                    @RequestParam Double sum,
                    @RequestParam(required = false) String pin,
                    HttpServletResponse response) {
        log.info("pay() card: {}, sum:{}, pin:{}", card, sum, pin);
        final String infoUrl = url + "/info";
        final String buyUrl = url + "/buy";
        final String infoUri = UriComponentsBuilder.fromHttpUrl(infoUrl)
                .queryParam("card", card)
                .build().toString();
        log.info("Request url: {}", infoUri);
        ResponseEntity<CardDto> responseEntity = restTemplate.getForEntity(infoUri, CardDto.class);
        log.info("response: {}", responseEntity.getStatusCode());

        if (responseEntity.getStatusCodeValue() != HttpStatus.OK.value() || responseEntity.getBody() == null) {
            log.info("card not found: {}", card);
            throw new CardNotFoundException();
        }
        final CardDto cardDto = restTemplate.getForEntity(infoUri, CardDto.class).getBody();
        if (pin != null && !pin.isEmpty() && !pin.equals(cardDto.getPin())) {
            log.info("Pin error. Expected: {}, real: {}, card: {}", cardDto.getPin(), pin, card);
            throw new IncorrectPinException();
        }
        if (cardDto.getBalance() < sum) {
            log.info("Not enough money. Needed: {}, balance: {}, card: {}", sum, cardDto.getBalance(), card);
            throw new NoEnouthMoneyException();
        }
        final String buyUri = UriComponentsBuilder.fromHttpUrl(buyUrl)
                .queryParam("card", card)
                .queryParam("amount", sum)
                .build().toString();
        log.info("Request url: {}", buyUri);
        final BuyResponseDto responseDto = restTemplate.postForEntity(buyUri, new HttpEntity<>(new HttpHeaders()), BuyResponseDto.class).getBody();
        log.info("Response: {}", responseDto.toString());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setOrigin("*");
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
}
