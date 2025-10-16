package com.sisinnov.pms.mapper;

import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.dto.response.UserResponse;
import com.sisinnov.pms.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);
}