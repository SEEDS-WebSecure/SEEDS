package com.websecure.seeds.service;

import com.websecure.seeds.domain.User;
import com.websecure.seeds.dto.CreateKeyDTO;

public interface KeyService {
    User createKey(CreateKeyDTO request);
}
