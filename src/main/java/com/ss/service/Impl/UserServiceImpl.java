package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.request.UserRequest;
import com.ss.dto.response.UserResponse;
import com.ss.exception.CustomException;
import com.ss.model.PermissionGroupModel;
import com.ss.model.StoreModel;
import com.ss.model.UserModel;
import com.ss.repository.UserRepository;
import com.ss.security.JwtTokenProvider;
import com.ss.service.PermissionService;
import com.ss.service.StoreService;
import com.ss.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ss.util.StringUtil.convertSqlSearchText;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @Override
    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public UserResponse create(UserRequest request) {
        UserModel checkedUser = userRepository.findByUsername(request.getUserName());
        if (checkedUser != null)
            throw new CustomException("username is duplicated", HttpStatus.CONFLICT);

        UserModel user = new UserModel(request);

        PermissionGroupModel permissionGroup = permissionService.findById(request.getPermissionGroupId());
        if (permissionGroup == null)
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);

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
            throw new CustomException("user is not existed", HttpStatus.BAD_REQUEST);

        PermissionGroupModel permissionGroup = permissionService.findById(request.getPermissionGroupId());
        if (permissionGroup == null)
            throw new CustomException("permission group is not existed", HttpStatus.BAD_REQUEST);

        Set<StoreModel> stores = storeService.findByIds(request.getStoreIds());

        UserModel user = userOptional.get();
        String password = passwordEncoder.encode(request.getPassword());
        user.update(request, password, permissionGroup, stores);
        user = userRepository.save(user);
        return new UserResponse(user, permissionGroup, stores);
    }

    @Override
    @Transactional
    public PageResponse<UserResponse> search(String username, String store, String permissionGroup, String position, String email, String fullName, PageCriteria pageCriteria) {
        username = convertSqlSearchText(username);
        store = convertSqlSearchText(store);
        permissionGroup = convertSqlSearchText(permissionGroup);
        position = convertSqlSearchText(position);
        email = convertSqlSearchText(email);
        fullName = convertSqlSearchText(fullName);
        Page<UserModel> page = userRepository.search(username, store, permissionGroup, position, email, fullName, pageCriteriaPageableMapper.toPageable(pageCriteria));
        List<UserModel> users = page.getContent();
        List<UserResponse> responses = users.stream()
                .map(item -> new UserResponse(item, item.getPermissionGroupModel(), item.getStores()))
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .paging(Paging.builder().totalCount(page.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(responses)
                .build();
    }
}
