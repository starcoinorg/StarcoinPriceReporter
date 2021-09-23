package org.starcoin.stcpricereporter.api;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.model.PriceGrowth;
import org.starcoin.stcpricereporter.vo.PriceAverage;
import org.starcoin.stcpricereporter.vo.PriceRoundView;
import org.starcoin.stcpricereporter.service.PriceFeedService;
import org.starcoin.stcpricereporter.service.PriceGrowthService;
import org.starcoin.stcpricereporter.service.PriceRoundService;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = {"Starcoin-Price-Reporter RESTful API"})
@RestController
@RequestMapping("v1")
public class StcPriceReporterController {

    @Resource
    private PriceFeedService priceFeedService;

    @Resource
    private PriceRoundService priceRoundService;

    @Resource
    private PriceGrowthService priceGrowthService;

    @GetMapping("priceFeeds")
    public List<PriceFeed> getPriceFeeds() {
        return priceFeedService.getPriceFeeds();
    }

    @GetMapping("priceFeeds/{pairId}")
    public PriceFeed getPriceFeed(@PathVariable("pairId") String pairId) {
        return priceFeedService.getPriceFeed(pairId);
    }

    @GetMapping("toUsdPriceFeeds")
    public List<PriceFeed> getToUsdPriceFeeds(@RequestParam("t") List<String> tokenIds) {
        return priceFeedService.findToUsdPriceFeedsByTokenIdIn(tokenIds);
    }

    @GetMapping("getProximatePriceRound")
    public PriceRoundView getProximatePriceRound(@RequestParam("pairId") String pairId, @RequestParam("timestamp") Long timestamp) {
        return priceRoundService.getProximatePriceRound(pairId, timestamp);
    }

    @GetMapping("getPriceAverages")
    public List<PriceAverage> getPriceAverages(@RequestParam("p") List<String> pairIds,
                                               @RequestParam("after") Long afterTimestamp,
                                               @RequestParam("before") Long beforeTimestamp) {
        return priceRoundService.getPriceAverages(pairIds, afterTimestamp, beforeTimestamp);
    }

    @GetMapping("priceGrowths")
    public List<PriceGrowth> getPriceGrowths(@RequestParam("p") List<String> pairIds) {
        return priceGrowthService.findByPairIdIn(pairIds);
    }

    @GetMapping("exchangeRates/ETH_STC")
    public @ResponseBody
    String getEthToStcExchangeRate() {
        return priceFeedService.getEthToStcExchangeRate().toString();
    }

    @GetMapping("exchangeRates/WEI_NANOSTC")
    public @ResponseBody
    String getWeiToNanoStcExchangeRate() {
        return priceFeedService.getWeiToNanoStcExchangeRate().toString();
    }

}
