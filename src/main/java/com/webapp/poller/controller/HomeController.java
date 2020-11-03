package com.webapp.poller.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @RequestMapping(value="/", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> home() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
