package com.LetucOJ.user.repos;

import com.LetucOJ.common.anno.LanguageConfigDO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.model.UserManagerDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserMybatisRepos extends BaseMapper<LanguageConfigDO> {

    Integer saveUserInfo(UserManagerDTO userDTO);

    Integer updatePassword(@Param("userName") String userName, @Param("password") String password);

    UserManagerDTO getPasswordByUserName(@Param("userName") String userName);

    List<UserManagerDTO> getUserListByRole(@Param("role") String role);

    Integer setUserToManager(@Param("userName") String userName);

    Integer setManagerToUser(@Param("userName") String userName);

    Integer activateUser(@Param("userName") String userName);

    Integer deactivateUser(@Param("userName") String userName);

    // 返回 Map 时，resultType="java.util.Map"
    List<Map<String, Object>> listCorrect();

    List<Map<String, Object>> points();

    Integer updateUserInfo(UserInfoDTO userDTO);

    UserInfoDTO getUserFullInfo(@Param("userName") String userName);
}