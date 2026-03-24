package ba.unsa.etf.nbp.travel.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ba.unsa.etf.nbp.travel.dto.request.CreateUserRequest;
import ba.unsa.etf.nbp.travel.dto.request.UpdateUserRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.UserResponse;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.entity.UserEntity;
import ba.unsa.etf.nbp.travel.repository.AddressRepository;
import ba.unsa.etf.nbp.travel.repository.RoleRepository;
import ba.unsa.etf.nbp.travel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ba.unsa.etf.nbp.travel.mapper.UserMapper.toEntity;
import static ba.unsa.etf.nbp.travel.mapper.UserMapper.toResponse;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;

    public PageResponse<UserResponse> findAll(int page, int size) {
        var users = userRepository.findAll(page, size);
        var total = userRepository.count();
        var content = users.stream()
                .map(this::mapToResponse)
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public UserResponse findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already taken");
        }

        var entity = toEntity(request);
        var hashedPassword = BCrypt.withDefaults().hashToString(12, request.password().toCharArray());
        entity.setPassword(hashedPassword);

        var id = userRepository.save(entity);
        entity.setId(id);

        return mapToResponse(entity);
    }

    public UserResponse update(Long id, UpdateUserRequest request) {
        var existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (nonNull(request.firstName())) {
            existing.setFirstName(request.firstName());
        }
        if (nonNull(request.lastName())) {
            existing.setLastName(request.lastName());
        }
        if (nonNull(request.email())) {
            existing.setEmail(request.email());
        }
        if (nonNull(request.phoneNumber())) {
            existing.setPhoneNumber(request.phoneNumber());
        }
        if (nonNull(request.birthDate())) {
            existing.setBirthDate(request.birthDate());
        }
        if (nonNull(request.addressId())) {
            existing.setAddressId(request.addressId());
        }

        userRepository.update(existing);
        return mapToResponse(existing);
    }

    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.deleteById(id);
    }

    public UserResponse assignRole(Long userId, Long roleId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        user.setRoleId(role.getId());
        userRepository.update(user);

        return toResponse(user, role.getName());
    }

    private UserResponse mapToResponse(UserEntity user) {
        var roleName = roleRepository.findById(user.getRoleId())
                .map(r -> r.getName())
                .orElse(null);
        return toResponse(user, roleName);
    }
}
