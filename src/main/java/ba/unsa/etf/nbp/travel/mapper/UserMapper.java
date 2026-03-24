package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.request.CreateUserRequest;
import ba.unsa.etf.nbp.travel.dto.response.UserResponse;
import ba.unsa.etf.nbp.travel.model.entity.UserEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

    public static UserResponse toResponse(UserEntity user, String roleName) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getAddressId(),
                roleName
        );
    }

    public static UserEntity toEntity(CreateUserRequest request) {
        return UserEntity.builder()
                .id(null)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .birthDate(request.birthDate())
                .addressId(request.addressId())
                .roleId(request.roleId())
                .build();
    }
}
