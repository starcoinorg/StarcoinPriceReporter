package org.starcoin.stcpricereporter.api;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.repo.PriceFeedRepository;

import javax.annotation.Resource;

@Api(tags = {"STC-Price-Reporter RESTful API"})
@RestController
@RequestMapping("v1")
public class StcPriceReporterController {

    @Resource
    private PriceFeedRepository priceFeedRepository;

    @GetMapping("priceFeeds/{pairId}")
    public PriceFeed getPriceFeed(@PathVariable("pairId") String pairId) {
        return priceFeedRepository.findById(pairId).orElse(null);
    }
}
