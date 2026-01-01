package com.zemera.inventory.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class AuthRepository {

    private final Pool client;

    public AuthRepository(Pool client) {
        this.client = client;
    }

    public Pool getClient() {
        return client;
    }

    // Find user by username
    public Future<JsonObject> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = $1";
        return client.preparedQuery(sql)
                .execute(Tuple.of(username))
                .map(rows -> {
                    if (!rows.iterator().hasNext()) return null;
                    Row row = rows.iterator().next();
                    return row.toJson();
                });
    }

    // Get all users
    public Future<JsonObject[]> getAllUsers() {
        String sql = "SELECT id, full_name, username, email, phone, role, branch_name, created_at FROM users";
        return client.preparedQuery(sql)
                .execute()
                .map(rows -> {
                    JsonObject[] result = new JsonObject[rows.rowCount()];
                    int i = 0;
                    for (Row row : rows) {
                        result[i++] = new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("fullName", row.getString("full_name"))
                                .put("username", row.getString("username"))
                                .put("email", row.getString("email"))
                                .put("phone", row.getString("phone"))
                                .put("role", row.getString("role"))
                                .put("name", row.getString("branch_name"))
                                .put("createdAt", row.getLocalDateTime("created_at").toString());
                    }
                    return result;
                });
    }

    // Create user
    public Future<JsonObject> createUser(JsonObject data) {
        String sql = """
            INSERT INTO users(full_name, username, password, email, phone, role, branch_name, created_at)
            VALUES ($1,$2,$3,$4,$5,$6,$7,NOW())
            RETURNING id, full_name, username, email, phone, role, branch_name, created_at
        """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(
                        data.getString("fullName"),
                        data.getString("username"),
                        data.getString("password"),
                        data.getString("email"),
                        data.getString("phone"),
                        data.getString("role"),
                        data.getString("name")
                ))
                .map(rows -> {
                    Row row = rows.iterator().next();
                    return new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("fullName", row.getString("full_name"))
                            .put("username", row.getString("username"))
                            .put("email", row.getString("email"))
                            .put("phone", row.getString("phone"))
                            .put("role", row.getString("role"))
                            .put("name", row.getString("branch_name"))
                            .put("createdAt", row.getLocalDateTime("created_at").toString());
                });
    }

    // Update user
    public Future<JsonObject> updateUser(Long id, JsonObject data) {
        String sql = """
            UPDATE users SET
                full_name = $1,
                username = $2,
                email = $3,
                phone = $4,
                role = $5,
                branch_name = $6
            WHERE id = $7
            RETURNING id, full_name, username, email, phone, role, branch_name, created_at
        """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(
                        data.getString("fullName"),
                        data.getString("username"),
                        data.getString("email"),
                        data.getString("phone"),
                        data.getString("role"),
                        data.getString("name"),
                        id
                ))
                .map(rows -> {
                    if (!rows.iterator().hasNext()) {
                        throw new RuntimeException("User not found");
                    }
                    Row row = rows.iterator().next();
                    return new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("fullName", row.getString("full_name"))
                            .put("username", row.getString("username"))
                            .put("email", row.getString("email"))
                            .put("phone", row.getString("phone"))
                            .put("role", row.getString("role"))
                            .put("name", row.getString("branch_name"))
                            .put("createdAt", row.getLocalDateTime("created_at").toString());
                });
    }

    // Delete user
    public Future<Void> deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = $1";
        return client.preparedQuery(sql)
                .execute(Tuple.of(id))
                .mapEmpty();
    }
}
