package com.luontd.authservice.application.mapper;

import com.luontd.authservice.application.services.dto.RegisterRequest;
import com.luontd.authservice.application.services.dto.RegisterResponse;
import com.luontd.authservice.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface IUserMapper {
    RegisterResponse toResponse(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true) // Ví dụ List roles sẽ được add logic riêng
    User toEntity(RegisterResponse dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(RegisterRequest dto);

//    @Mapping(target = "id", ignore = true) // Tuyệt đối không được update ID
//    void updateEntityFromDto(RegisterResponse dto, @MappingTarget User entity);
}
