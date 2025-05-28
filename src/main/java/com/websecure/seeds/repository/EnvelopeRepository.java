package com.websecure.seeds.repository;

import com.websecure.seeds.domain.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvelopeRepository extends JpaRepository<Envelope, Long> {
}
