package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.interfaces.dto.response.auth.AuthResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthWebMapper {
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "role", source = "roleId")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    AuthResponseDTO toResponse(User user);
}
