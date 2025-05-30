package com.websecure.seeds.repository;

import com.websecure.seeds.domain.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvelopeRepository extends JpaRepository<Envelope, Long> {
    List<Envelope> findByReceiver(String receiver);
}
