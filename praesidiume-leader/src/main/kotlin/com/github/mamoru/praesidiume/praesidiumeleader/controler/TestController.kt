package com.github.mamoru.praesidiume.praesidiumeleader.controler

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test/")
class TestController {
    @GetMapping
    fun testGet() = "Hello world"
}
