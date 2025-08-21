package com.footballstore.apparels.dataaccesslayer;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ApparelRepositoryTest {

    @Autowired
    private ApparelRepository apparelRepository;

    @BeforeEach
    public void setup() {
        apparelRepository.deleteAll();
    }

    @Test
    public void whenSaveApparel_thenApparelIsPersisted() {
        Apparel apparel = Apparel.builder()
                .apparelIdentifier(new ApparelIdentifier("APP001"))
                .itemName("Test Jersey")
                .description("A test jersey")
                .brand("TestBrand")
                .price(new BigDecimal("59.99"))
                .cost(new BigDecimal("30.00"))
                .stock(100)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        Apparel savedApparel = apparelRepository.save(apparel);

        assertNotNull(savedApparel);
        assertNotNull(savedApparel.getId(), "Saved apparel should have a generated id");
        assertEquals("APP001", savedApparel.getApparelIdentifier().getApparelId());
        assertEquals("Test Jersey", savedApparel.getItemName());
        assertEquals(new BigDecimal("59.99"), savedApparel.getPrice());
    }

    @Test
    public void whenFindByApparelId_thenReturnApparel() {
        Apparel apparel = Apparel.builder()
                .apparelIdentifier(new ApparelIdentifier("APP002"))
                .itemName("Test Shorts")
                .description("Test shorts description")
                .brand("TestBrand")
                .price(new BigDecimal("39.99"))
                .cost(new BigDecimal("20.00"))
                .stock(50)
                .apparelType(ApparelType.SHORTS)
                .sizeOption(SizeOption.S)
                .build();
        apparelRepository.save(apparel);

        Optional<Apparel> found = apparelRepository.findByApparelIdentifier_ApparelId("APP002");
        assertTrue(found.isPresent());
        assertEquals("Test Shorts", found.get().getItemName());
    }

    @Test
    public void whenFindByNonExistentApparelId_thenReturnEmptyOptional() {
        Optional<Apparel> found = apparelRepository.findByApparelIdentifier_ApparelId("NON_EXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    public void whenDeleteApparel_thenRepositoryIsEmpty() {
        Apparel apparel = Apparel.builder()
                .apparelIdentifier(new ApparelIdentifier("APP003"))
                .itemName("Delete Jersey")
                .description("To be deleted")
                .brand("TestBrand")
                .price(new BigDecimal("50.00"))
                .cost(new BigDecimal("25.00"))
                .stock(80)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.L)
                .build();
        Apparel saved = apparelRepository.save(apparel);
        apparelRepository.delete(saved);
        List<Apparel> all = apparelRepository.findAll();
        assertEquals(0, all.size());
    }
}
