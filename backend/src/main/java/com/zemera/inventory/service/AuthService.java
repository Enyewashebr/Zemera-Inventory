package com.zemera.inventory.service;

import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.util.JwtUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;

import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final AuthRepository repo;
    private final JwtUtil jwtUtil;

    public AuthService(AuthRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

     public Future<JsonObject> register(JsonObject newUser) {
        // Hash the password before saving
        String hashedPassword = BCrypt.hashpw(newUser.getString("password"), BCrypt.gensalt());
        newUser.put("password", hashedPassword);

        String sql = "INSERT INTO users(username, password, role, branch_id) " +
                     "VALUES ($1, $2, $3, $4) RETURNING id, username, role, branch_id";

        return repo.getClient().preparedQuery(sql)
                .execute(Tuple.of(
                        newUser.getString("username"),
                        newUser.getString("password"),
                        newUser.getString("role"),
                        newUser.getInteger("branchId")
                ))
                .map(rows -> {
                    var row = rows.iterator().next();
                    return new JsonObject()
                            .put("id", row.getInteger("id"))
                            .put("username", row.getString("username"))
                            .put("role", row.getString("role"))
                            .put("branchId", row.getInteger("branch_id"));
                });
    }

    public Future<JsonObject> login(String username, String password) {

        return repo.findByUsername(username)
    .compose(user -> {
        if (user == null) {
            System.out.println("User not found: " + username);
            return Future.failedFuture("User not found");
        }

        String hashedPassword = user.getString("password");
        System.out.println("Hashed password from DB: " + hashedPassword);

        if (!BCrypt.checkpw(password, hashedPassword)) {
            System.out.println("Password mismatch for user: " + username);
            return Future.failedFuture("Invalid credentials");
        }

        Integer userId = user.getInteger("id");
        String role = user.getString("role");

        String token = jwtUtil.generateToken(userId, username, role);

        JsonObject response = new JsonObject()
                .put("id", userId)
                .put("username", username)
                .put("role", role)
                .put("token", token);

        return Future.succeededFuture(response);
    });

    }
}
