package com.LetucOJ.auth.service;

import com.LetucOJ.auth.model.user;
import org.springframework.stereotype.Service;

@Service
public interface authService {

    user register(String username, String password);

    user authenticate(String username, String password);

}
