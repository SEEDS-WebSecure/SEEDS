package com.websecure.seeds.service;

import com.websecure.seeds.domain.Envelope;
import com.websecure.seeds.dto.SendEnvelopeDTO;

public interface EnvelopeService {
    Envelope sendDigitalEnvelope(SendEnvelopeDTO request);
}
