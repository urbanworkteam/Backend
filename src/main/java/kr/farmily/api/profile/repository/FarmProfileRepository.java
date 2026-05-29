package kr.farmily.api.profile.repository;

import kr.farmily.api.profile.domain.FarmProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmProfileRepository extends JpaRepository<FarmProfile, Long> {}
