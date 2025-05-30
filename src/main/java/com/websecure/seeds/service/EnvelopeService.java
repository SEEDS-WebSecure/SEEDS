package com.websecure.seeds.service;

import com.websecure.seeds.domain.Envelope;
import com.websecure.seeds.dto.VerifySignDTO;
import com.websecure.seeds.dto.SendEnvelopeDTO;

import java.util.List;

public interface EnvelopeService {
    Envelope sendDigitalEnvelope(SendEnvelopeDTO request);
    List<Envelope> findEnvelopeList(String receiver);

    VerifySignDTO verifySign(Long envelopeId, String receiver);
}
