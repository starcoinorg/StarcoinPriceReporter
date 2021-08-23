package org.starcoin.stcpricereporter.api;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"STC-Price-Reporter RESTful API"})
@RestController
@RequestMapping("v1")
public class StcPriceReporterController {

    @GetMapping("hello")
    public String hello() {
        return "world.";
    }
}
