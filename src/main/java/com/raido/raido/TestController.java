package com.raido.raido;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/")
public class TestController {

    private static final String HELLO = "Hello, %s";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    TestHello hello(@RequestParam(value = "name", required = false, defaultValue = "Rostik") String name) {
        return new TestHello(counter.incrementAndGet(), String.format(HELLO, name));
    }
}
