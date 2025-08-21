package com.footballstore.apparels.dataaccesslayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.UUID;

@Embeddable
@Data
public class ApparelIdentifier {

    @Column(name = "APPAREL_ID")
    private String apparelId;

    protected ApparelIdentifier() {
    }

    public ApparelIdentifier(String apparelId) {
        this.apparelId = apparelId;
    }

    public static ApparelIdentifier generate() {
        return new ApparelIdentifier(UUID.randomUUID().toString());
    }
}
