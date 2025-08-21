package com.footballstore.apparels.presentationlayer;

import com.footballstore.apparels.businesslayer.ApparelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apparels")
@RequiredArgsConstructor
public class ApparelController {

    private final ApparelService apparelService;

    @GetMapping
    public ResponseEntity<List<ApparelResponseModel>> getAllApparels() {
        List<ApparelResponseModel> apparels = apparelService.getAllApparels();
        return ResponseEntity.ok(apparels);
    }

    @GetMapping("/{apparelId}")
    public ResponseEntity<ApparelResponseModel> getApparelById(@PathVariable String apparelId) {
        ApparelResponseModel apparel = apparelService.getApparelById(apparelId);
        return ResponseEntity.ok(apparel);
    }

    @PostMapping
    public ResponseEntity<ApparelResponseModel> createApparel(@RequestBody ApparelRequestModel requestModel) {
        ApparelResponseModel created = apparelService.createApparel(requestModel);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{apparelId}")
    public ResponseEntity<ApparelResponseModel> updateApparel(@PathVariable String apparelId,
                                                              @RequestBody ApparelRequestModel requestModel) {
        ApparelResponseModel updated = apparelService.updateApparel(apparelId, requestModel);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{apparelId}")
    public ResponseEntity<Void> deleteApparel(@PathVariable String apparelId) {
        apparelService.deleteApparel(apparelId);
        return ResponseEntity.noContent().build();
    }
}
