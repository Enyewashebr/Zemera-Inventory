package com.zemera.inventory.service;

import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.util.JwtUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
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

    // register new user
    public Future<JsonObject> registerUser(JsonObject newUser) {
    // Hash the password before saving
    String hashedPassword = BCrypt.hashpw(newUser.getString("password"), BCrypt.gensalt());
    newUser.put("password", hashedPassword);

    String sql = "INSERT INTO users(full_name, username, password, email, phone, role, branch_id, created_at) " +
                 "VALUES ($1, $2, $3, $4, $5, $6, $7, NOW()) " +
                 "RETURNING id, username, full_name, email, phone, role, branch_id";

    return repo.getClient().preparedQuery(sql)
            .execute(Tuple.of(
                    newUser.getString("fullName"),
                    newUser.getString("username"),
                    newUser.getString("password"),
                    newUser.getString("email"),
                    newUser.getString("phone"),
                    newUser.getString("role"),
                    newUser.getInteger("branchId")
            ))
            .map(rows -> {
                var row = rows.iterator().next();
                return new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("username", row.getString("username"))
                        .put("fullName", row.getString("full_name"))
                        .put("email", row.getString("email"))
                        .put("phone", row.getString("phone"))
                        .put("role", row.getString("role"))
                        .put("branchId", row.getInteger("branch_id"));
            });
}

    // user login
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
        Integer branchId = user.getInteger("branch_id");

        String token = jwtUtil.generateToken(userId, username, role, branchId);

        JsonObject response = new JsonObject()
                .put("id", userId)
                .put("username", username)
                .put("role", role)
                .put("branchId", branchId)
                .put("token", token);

        return Future.succeededFuture(response);
    });

    }

    // get all users
    public Future<JsonArray> getAllUsers() {
    String sql = "SELECT id, full_name, username, email, phone, role, branch_id FROM users";

    return repo.getClient().query(sql)
        .execute()
        .map(rows -> {
            JsonArray array = new JsonArray();
            for (var row : rows) {
                JsonObject user = new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("fullName", row.getString("full_name"))
                        .put("username", row.getString("username"))
                        .put("email", row.getString("email"))
                        .put("phone", row.getString("phone"))
                        .put("role", row.getString("role"))
                        .put("branchId", row.getInteger("branch_id"));
                array.add(user);
            }
            return array;
        });
}

    // delete user by id
    public Future<Void> deleteUser(Integer id) {
    String sql = "DELETE FROM users WHERE id = $1";
    return repo.getClient().preparedQuery(sql)
            .execute(Tuple.of(id))
            .mapEmpty();
}

}
