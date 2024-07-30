package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.UserRequest;
import com.ss.dto.response.TokenResponse;
import com.ss.dto.response.UserResponse;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.DuplicatedError;
import com.ss.exception.http.InvalidInputError;
import com.ss.exception.http.NotFoundError;
import com.ss.model.PermissionGroupModel;
import com.ss.model.RefreshToken;
import com.ss.model.StoreModel;
import com.ss.model.UserModel;
import com.ss.repository.UserRepository;
import com.ss.repository.query.UserQuery;
import com.ss.security.JwtTokenProvider;
import com.ss.service.PermissionService;
import com.ss.service.RefreshTokenService;
import com.ss.service.StoreService;
import com.ss.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.ss.util.CommonUtil.convertSqlSearchText;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @Override
    public TokenResponse signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserModel user = userRepository.findByUsername(username);
            return generateToken(user);
        } catch (AuthenticationException e) {
            throw new ExceptionResponse(InvalidInputError.USER_PASSWORD_INVALID.getMessage(), InvalidInputError.USER_PASSWORD_INVALID);
        }
    }

    public TokenResponse generateToken(UserModel user) {
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new TokenResponse(token, refreshToken.getToken());
    }

    @Override
    public UserResponse create(UserRequest request) {
        UserModel checkedUser = userRepository.findByUsername(request.getUserName());
        long invalidUserByCode = userRepository.countByUserCode(request.getUserCode());
        if (invalidUserByCode > 0)
            throw new ExceptionResponse(DuplicatedError.USER_CODE_DUPLICATED.getMessage(), DuplicatedError.USER_CODE_DUPLICATED);

        if (checkedUser != null)
            throw new ExceptionResponse(DuplicatedError.USERNAME_DUPLICATED.getMessage(), DuplicatedError.USERNAME_DUPLICATED);

        UserModel user = new UserModel(request);

        PermissionGroupModel permissionGroup = permissionService.findById(request.getPermissionGroupId());
        if (permissionGroup == null)
            throw new ExceptionResponse(InvalidInputError.PERMISSION_GROUP_INVALID.getMessage(), InvalidInputError.PERMISSION_GROUP_INVALID);

        Set<StoreModel> stores = storeService.findByIds(request.getStoreIds());

        String password = passwordEncoder.encode(request.getPassword());
        user.update(request, password, permissionGroup, stores);
        user = userRepository.save(user);
        return new UserResponse(user, permissionGroup, stores);
    }

    @Override
    public UserResponse update(UUID id, UserRequest request) {
        Optional<UserModel> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty())
            throw new ExceptionResponse(NotFoundError.USER_NOT_FOUND.getMessage(), NotFoundError.USER_NOT_FOUND);

        PermissionGroupModel permissionGroup = permissionService.findById(request.getPermissionGroupId());
        if (permissionGroup == null)
            throw new ExceptionResponse(NotFoundError.PERMISSION_GROUP_NOT_FOUND.getMessage(), NotFoundError.PERMISSION_GROUP_NOT_FOUND);

        Set<StoreModel> stores = storeService.findByIds(request.getStoreIds());

        UserModel user = userOptional.get();
        if (!user.getUserCode().equals(request.getUserCode())) {
            long invalidUserByCode = userRepository.countByUserCode(request.getUserCode());
            if (invalidUserByCode > 0)
                throw new ExceptionResponse(DuplicatedError.USER_CODE_DUPLICATED.getMessage(), DuplicatedError.USER_CODE_DUPLICATED);
        }

        String password = (StringUtils.hasText(request.getPassword())) ? passwordEncoder.encode(request.getPassword()) : user.getPassword();
        user.update(request, passwordEncoder.encode(password), permissionGroup, stores);
        user = userRepository.save(user);
        return new UserResponse(user, permissionGroup, stores);
    }

    @Override
    @Transactional
    public PageResponse<UserResponse> search(String username, String store, String permissionGroup, String position, String email, String fullName, PageCriteria pageCriteria) {
        UserQuery userQuery = UserQuery.builder()
                .username(convertSqlSearchText(username))
                .store(convertSqlSearchText(store))
                .permissionGroup(convertSqlSearchText(permissionGroup))
                .position(convertSqlSearchText(position))
                .email(convertSqlSearchText(email))
                .fullName(convertSqlSearchText(fullName))
                .build();
        Page<UserModel> page = userRepository.search(userQuery, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<UserModel> users = page.getContent();
        List<UserResponse> responses = users.stream()
                .map(item -> new UserResponse(item, item.getPermissionGroup(), item.getStores()))
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .paging(Paging.builder().totalCount(page.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(responses)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
        refreshTokenService.verifyExpiration(refreshToken);
        UserModel user = refreshToken.getUser();
        return generateToken(user);
    }

    @Override
    @Transactional
    public void logout() {
        User auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserModel user = userRepository.findByUsername(auth.getUsername());
        refreshTokenService.deleteByUser(user);
    }

    @Override
    @Transactional
    public UserModel getUserInfo() {
        User auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserModel user = userRepository.findByUsername(auth.getUsername());
        return user;
    }

    @Override
    @Transactional
    public UserResponse getMe() {
        User auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserModel user = userRepository.findByUsername(auth.getUsername());
        return new UserResponse(user, user.getPermissionGroup(), user.getStores());
    }

    @Override
    @Transactional
    public List<UserModel> searchList(UserQuery userQuery) {
        List<UserModel> list = userRepository.searchList(userQuery);
        return list;
    }

    @Override
    public List<UserModel> findByUsernames(Collection<String> usernames) {
        List<UserModel> users = userRepository.findByUsernameIn(usernames);
        return users;
    }

    @Override
    public List<UserModel> findUsersByKeyword(String createdUser) {
        if (!StringUtils.hasText(createdUser))
            return new ArrayList<>();
        createdUser = convertSqlSearchText(createdUser);
        List<UserModel> users = userRepository.findByUsernameLikeOrFullNameLike(createdUser, createdUser);
        return users;
    }
}
