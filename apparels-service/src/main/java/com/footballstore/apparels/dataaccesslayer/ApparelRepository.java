package com.footballstore.apparels.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApparelRepository extends JpaRepository<Apparel, Integer> {
    Optional<Apparel> findByApparelIdentifier_ApparelId(String apparelId);
}
