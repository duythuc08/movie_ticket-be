package com.example.movie_ticket_be.user.mapper;



import com.example.movie_ticket_be.auth.dto.request.RegisterRequest;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.request.UsersCreationRequest;
import com.example.movie_ticket_be.user.dto.response.UserClientRespone;
import com.example.movie_ticket_be.user.dto.response.UserMenuRespone;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.example.movie_ticket_be.user.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUsers(UsersCreationRequest request);

    UsersRespone toUsersRespone(Users users);

    UserMenuRespone toUserMenuRespone(Users users);

    @Mapping(source = "membershipTier.name", target = "memberShipTierName")
    UserClientRespone toUserClientRespone(Users users);

    @Mapping(target = "role", ignore = true)
    void updateUser(@MappingTarget Users user, UserUpdateRequest request);

    Users toRegisterUser(RegisterRequest request);
}
