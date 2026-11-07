package com.eliascampos.usermanagement.user;

import com.eliascampos.usermanagement.exception.DuplicateEmailException;
import com.eliascampos.usermanagement.exception.ResourceNotFoundException;
import com.eliascampos.usermanagement.user.dto.PageResponse;
import com.eliascampos.usermanagement.user.dto.UserRequest;
import com.eliascampos.usermanagement.user.dto.UserResponse;
import com.eliascampos.usermanagement.user.dto.UserStatusRequest;
import com.eliascampos.usermanagement.user.dto.UserUpdateRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public UserService(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public UserResponse create(UserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = new User(request.name().trim(), email, request.role());
        try {
            return UserResponse.from(userRepository.save(user));
        } catch (DuplicateKeyException ex) {
            throw new DuplicateEmailException(email);
        }
    }

    public UserResponse findById(String id) {
        return UserResponse.from(findEntityById(id));
    }

    public PageResponse<UserResponse> findAll(String search, UserRole role, Boolean active, Pageable pageable) {
        List<Criteria> filters = new ArrayList<>();

        if (StringUtils.hasText(search)) {
            String regex = Pattern.quote(search.trim());
            filters.add(new Criteria().orOperator(
                    Criteria.where("name").regex(regex, "i"),
                    Criteria.where("email").regex(regex, "i")
            ));
        }
        if (role != null) {
            filters.add(Criteria.where("role").is(role.name()));
        }
        if (active != null) {
            filters.add(Criteria.where("active").is(active));
        }

        Criteria criteria = filters.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(filters.toArray(Criteria[]::new));

        long total = mongoTemplate.count(new Query(criteria), User.class);
        List<User> users = mongoTemplate.find(new Query(criteria).with(pageable), User.class);

        Page<UserResponse> page = new PageImpl<>(
                users.stream().map(UserResponse::from).toList(),
                pageable,
                total
        );
        return PageResponse.from(page);
    }

    public UserResponse update(String id, UserUpdateRequest request) {
        User user = findEntityById(id);
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }

        user.update(request.name().trim(), email, request.role());
        try {
            return UserResponse.from(userRepository.save(user));
        } catch (DuplicateKeyException ex) {
            throw new DuplicateEmailException(email);
        }
    }

    public UserResponse changeStatus(String id, UserStatusRequest request) {
        User user = findEntityById(id);
        user.changeStatus(request.active());
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(String id) {
        User user = findEntityById(id);
        userRepository.delete(user);
    }

    private User findEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado para o id " + id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
