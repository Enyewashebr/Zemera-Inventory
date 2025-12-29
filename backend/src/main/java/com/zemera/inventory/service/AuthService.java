package com.zemera.inventory.service;

import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.model.AuthResponse;
import com.zemera.inventory.model.LoginRequest;
import com.zemera.inventory.util.JwtUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final AuthRepository repo;

    public AuthService(AuthRepository repo) {
        this.repo = repo;
    }

    public Future<AuthResponse> login(LoginRequest req) {
        return repo.findByUsername(req.username)
            .compose(user -> {
                if (user == null) {
                    return Future.failedFuture("Invalid credentials");
                }

                if (!BCrypt.checkpw(req.password, user.getString("password"))) {
                    return Future.failedFuture("Invalid credentials");
                }

                AuthResponse res = new AuthResponse();
                res.username = user.getString("username");
                res.role = user.getString("role");
                res.branchId = user.getInteger("branch_id");
                res.token = JwtUtil.generateToken(user);

                return Future.succeededFuture(res);
            });
    }
}
