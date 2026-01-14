package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;

public interface UserService {

     User signUp();

     Token login();

     User validateToken();

     void logout();


}
