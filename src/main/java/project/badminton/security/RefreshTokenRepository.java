package project.badminton.security;

import org.springframework.data.jpa.repository.JpaRepository;
import project.badminton.user.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
