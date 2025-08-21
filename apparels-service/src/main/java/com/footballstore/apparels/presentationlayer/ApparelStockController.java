package com.footballstore.apparels.presentationlayer;

import com.footballstore.apparels.businesslayer.ApparelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apparels/{apparelId}/stock")
@RequiredArgsConstructor
public class ApparelStockController {

    private final ApparelService apparelService;

    @GetMapping
    public Integer getStock(@PathVariable String apparelId) {
        return apparelService.getStock(apparelId);
    }

    @PatchMapping("/decrease")
    public void decreaseStock(@PathVariable String apparelId,
                              @RequestParam("quantity") int quantity) {
        apparelService.decreaseStock(apparelId, quantity);
    }

    @PatchMapping("/increase")
    public void increaseStock(@PathVariable String apparelId,
                              @RequestParam("quantity") int quantity) {
        apparelService.increaseStock(apparelId, quantity);
    }
}
