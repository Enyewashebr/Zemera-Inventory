package com.zemera.inventory.service;

import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.util.JwtUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final AuthRepository repo;
    private final JwtUtil jwtUtil;

    public AuthService(AuthRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

    // Register user
    public Future<JsonObject> registerUser(JsonObject newUser) {
        String hashedPassword = BCrypt.hashpw(newUser.getString("password"), BCrypt.gensalt());
        newUser.put("password", hashedPassword);
        return repo.createUser(newUser);
    }

    // Login
    public Future<JsonObject> login(String username, String password) {

    return repo.findByUsername(username)
        .compose(user -> {

            if (user == null)
                return Future.failedFuture("User not found");

            String hashedPassword = user.getString("password");
            if (!BCrypt.checkpw(password, hashedPassword))
                return Future.failedFuture("Invalid credentials");

            Integer userId = user.getInteger("id");
            String role = user.getString("role");
            String name = user.getString("branch_name");
            Integer branchId = user.getInteger("branch_id"); // ✅ FIXED

            String token = jwtUtil.generateToken(
                userId,
                username,
                role,
                name,
                branchId
            );

            return Future.succeededFuture(
                new JsonObject()
                    .put("id", userId)
                    .put("username", username)
                    .put("role", role)
                    .put("branch_name", name)
                    .put("branch_id", branchId)
                    .put("token", token)
            );
        });
}

    // Get all users
    public Future<JsonArray> getAllUsers() {
        return repo.getAllUsers()
                .map(users -> {
                    JsonArray array = new JsonArray();
                    for (JsonObject u : users) array.add(u);
                    return array;
                });
    }

    // Update user
    public Future<JsonObject> updateUser(Long id, JsonObject body) {
        return repo.updateUser(id, body);
    }

    // Delete user
    public Future<Void> deleteUser(Long id) {
        return repo.deleteUser(id);
    }
}
