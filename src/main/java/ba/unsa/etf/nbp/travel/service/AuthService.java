package ba.unsa.etf.nbp.travel.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ba.unsa.etf.nbp.travel.dto.request.LoginRequest;
import ba.unsa.etf.nbp.travel.dto.request.RegisterRequest;
import ba.unsa.etf.nbp.travel.dto.response.AuthResponse;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.UnauthorizedException;
import ba.unsa.etf.nbp.travel.model.entity.UserEntity;
import ba.unsa.etf.nbp.travel.repository.RoleRepository;
import ba.unsa.etf.nbp.travel.repository.UserRepository;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already taken");
        }

        var role = roleRepository.findByName("USER")
                .orElseThrow(() -> new ConflictException("Default role USER not found"));

        var hashedPassword = BCrypt.withDefaults().hashToString(12, request.password().toCharArray());

        var user = UserEntity.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(hashedPassword)
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .birthDate(request.birthDate())
                .roleId(role.getId())
                .build();

        var userId = userRepository.save(user);
        var token = jwtProvider.generateToken(userId, user.getUsername(), role.getName());

        return new AuthResponse(token, userId, user.getUsername(), user.getEmail(), role.getName());
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        var result = BCrypt.verifyer().verify(request.password().toCharArray(), user.getPassword());

        if (!result.verified) {
            throw new UnauthorizedException("Invalid username or password");
        }

        var role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new UnauthorizedException("User role not found"));

        var token = jwtProvider.generateToken(user.getId(), user.getUsername(), role.getName());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), role.getName());
    }
}
