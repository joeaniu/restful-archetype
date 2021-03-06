package org.thiki.kanban.user;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thiki.kanban.foundation.aspect.ValidateParams;
import org.thiki.kanban.foundation.common.FileUtil;
import org.thiki.kanban.foundation.exception.BusinessException;
import org.thiki.kanban.user.profile.AvatarStorage;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class UsersService {
    public static final int AVATAR_MAX_SIZE = 102400;
    public static Logger logger = LoggerFactory.getLogger(UsersService.class);
    private static String avatarFileTempPath = "files/avatars/temp/";
    @Resource
    private UsersPersistence usersPersistence;
    @Resource
    private AvatarStorage avatarStorage;

    public User findByName(String userName) {
        User user = usersPersistence.findByName(userName);
        Profile profile = loadProfileByUserName(userName);
        if (profile != null) {
            user.setProfile(profile);
        }
        return user;
    }

    @ValidateParams
    public User findByIdentity(@NotEmpty(message = UsersCodes.identityIsRequired) String identity) {
        return usersPersistence.findByIdentity(identity);
    }

    @ValidateParams
    public User findByEmail(@NotEmpty(message = UsersCodes.emailIsRequired) String email) {
        return usersPersistence.findByIdentity(email);
    }

    @ValidateParams
    public User findByCredential(@NotEmpty(message = UsersCodes.identityIsRequired) String identity, @NotEmpty(message = UsersCodes.md5PasswordIsRequired) String md5Password) {
        return usersPersistence.findByCredential(identity, md5Password);
    }

    @ValidateParams
    public boolean isNameExist(@NotEmpty(message = UsersCodes.userNameIsRequired) String userName) {
        return usersPersistence.isNameExist(userName);
    }

    @ValidateParams
    public boolean isEmailExist(@NotEmpty(message = UsersCodes.emailIsRequired) String email) {
        return usersPersistence.isEmailExist(email);
    }

    @CacheEvict(value = "avatar", key = "contains(#userName)", allEntries = true)
    public String uploadAvatar(String userName, MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) {
            throw new BusinessException(UsersCodes.AVATAR_IS_EMPTY);
        }
        if (multipartFile.getSize() > AVATAR_MAX_SIZE) {
            throw new BusinessException(UsersCodes.AVATAR_IS_OUT_OF_MAX_SIZE);
        }
        File avatar = FileUtil.convert(avatarFileTempPath, multipartFile);
        String avatarName = avatarStorage.store(userName, avatar);
        Profile profile = usersPersistence.findProfile(userName);
        if (profile == null) {
            usersPersistence.initProfile(new Profile(userName));
        }
        usersPersistence.updateAvatar(userName, avatarName);
        return avatarName;
    }

    public InputStream loadAvatar(String userName) throws IOException {
        logger.info("load avatar by userName:{}", userName);
        Profile profile = loadProfileByUserName(userName);
        return avatarStorage.loadAvatar(profile.getAvatar());
    }

    @Cacheable(value = "profile", key = "'profile'+#userName")
    public Profile loadProfileByUserName(String userName) {
        Profile profile = usersPersistence.findProfile(userName);
        if (profile == null) {
            usersPersistence.initProfile(new Profile(userName));
        }
        return usersPersistence.findProfile(userName);
    }

    @CacheEvict(value = "profile", key = "contains(#userName)", allEntries = true)
    public Profile updateProfile(Profile profile, String userName) {
        usersPersistence.updateProfile(userName, profile);
        return usersPersistence.findProfile(userName);
    }
}
