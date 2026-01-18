package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;

public interface UserService {

     User signUp(String name,String email,String password);

     Token login(String email,String password);

     User validateToken(String tokenValue);

     void logout();


}
