package io.cinc.springbootsecurity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDTO {

    private Long id;

    private String username;

    private String password;

    private String firstname;

    private String lastname;

    private  String email;

}