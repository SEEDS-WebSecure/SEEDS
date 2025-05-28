package com.websecure.seeds.repository;

import com.websecure.seeds.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
