package com.footballstore.apigateway.presentationlayer.apparels;

import com.footballstore.apigateway.businesslayer.apparels.ApparelsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/apparels")
public class ApparelsController {

    private final ApparelsService service;

    public ApparelsController(ApparelsService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApparelResponseModel>> getAllApparels() {
        return ResponseEntity.ok(service.getAllApparels());
    }

    @GetMapping(value = "/{apparelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApparelResponseModel> getApparelById(@PathVariable String apparelId) {
        return ResponseEntity.ok(service.getApparelById(apparelId));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApparelResponseModel> createApparel(
            @RequestBody ApparelRequestModel requestModel
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createApparel(requestModel));
    }

    @PutMapping(
            value    = "/{apparelId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<ApparelResponseModel> updateApparel(
            @PathVariable String apparelId,
            @RequestBody  ApparelRequestModel requestModel
    ) {
        return ResponseEntity.ok(service.updateApparel(apparelId, requestModel));
    }

    @DeleteMapping("/{apparelId}")
    public ResponseEntity<Void> deleteApparel(@PathVariable String apparelId) {
        service.deleteApparel(apparelId);
        return ResponseEntity.noContent().build();
    }
}
