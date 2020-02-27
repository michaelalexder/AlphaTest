package ru.alphatest.user;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class User {

    private String id;

    private UserType userType;

    private Boolean blackList;
}
