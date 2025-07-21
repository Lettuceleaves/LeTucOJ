package com.LetucOJ.auth.service;

import com.LetucOJ.auth.model.RegisterRequestDTO;
import com.LetucOJ.auth.model.ResultVO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    ResultVO register(RegisterRequestDTO registerRequestDTO);

    ResultVO authenticate(RegisterRequestDTO registerRequestDTO);

}
