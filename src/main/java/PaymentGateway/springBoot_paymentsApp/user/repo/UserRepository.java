package PaymentGateway.springBoot_paymentsApp.user.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import PaymentGateway.springBoot_paymentsApp.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
