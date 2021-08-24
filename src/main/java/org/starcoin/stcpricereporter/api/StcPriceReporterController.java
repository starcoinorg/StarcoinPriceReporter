package org.starcoin.stcpricereporter.api;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.service.PriceFeedService;

import javax.annotation.Resource;

@Api(tags = {"STC-Price-Reporter RESTful API"})
@RestController
@RequestMapping("v1")
public class StcPriceReporterController {

    @Resource
    private PriceFeedService priceFeedService;

    @GetMapping("priceFeeds/{pairId}")
    public PriceFeed getPriceFeed(@PathVariable("pairId") String pairId) {
        return priceFeedService.getPriceFeed(pairId);
    }
}
