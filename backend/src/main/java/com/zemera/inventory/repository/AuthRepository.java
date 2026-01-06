package com.zemera.inventory.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class AuthRepository {
    private JsonObject mapUser(Row row) {
    return new JsonObject()
        .put("id", row.getLong("id"))
        .put("fullName", row.getString("full_name"))
        .put("username", row.getString("username"))
        .put("email", row.getString("email"))
        .put("phone", row.getString("phone"))
        .put("role", row.getString("role"))
        .put("branchName", row.getString("branch_name"))
        .put("branchId", row.getLong("branch_id"))
        .put("createdAt", row.getLocalDateTime("created_at").toString());
}


    private final Pool client;

    public AuthRepository(Pool client) {
        this.client = client;
    }

    // ---------------- Find user by username ----------------
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

    // ---------------- Get all users ----------------
    public Future<JsonObject[]> getAllUsers() {
        String sql = """
            SELECT id, full_name, username, email, phone, role, branch_name, branch_id, created_at
            FROM users
        """;

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
                                .put("branchName", row.getString("branch_name"))
                                .put("branchId", row.getLong("branch_id"))
                                .put("createdAt", row.getLocalDateTime("created_at").toString());
                    }
                    return result;
                });
    }

    // ---------------- Create user ----------------
  public Future<JsonObject> createUser(JsonObject data) {

    String role = data.getString("role");
    Integer branchId = data.getInteger("branchId");

    // SUPER MANAGER → no branch
    if ("SUPER_MANAGER".equals(role)) {

        String sql = """
            INSERT INTO users(
                full_name, username, password, email, phone,
                role, branch_name, branch_id, created_at
            )
            VALUES ($1,$2,$3,$4,$5,$6,'ALL',NULL,NOW())
            RETURNING id, full_name, username, email, phone,
                      role, branch_name, branch_id, created_at
        """;

        return client.preparedQuery(sql)
            .execute(Tuple.of(
                data.getString("fullName"),
                data.getString("username"),
                data.getString("password"),
                data.getString("email"),
                data.getString("phone"),
                role
            ))
            .map(rows -> mapUser(rows.iterator().next()));
            
    }
    

    // BRANCH MANAGER → must have branch
    if (branchId == null) {
        return Future.failedFuture("Branch is required for Branch Manager");
    }

    return client
        .preparedQuery("SELECT branch_name FROM branches WHERE id = $1")
        .execute(Tuple.of(branchId))
        .compose(rows -> {

            if (rows.rowCount() == 0) {
                return Future.failedFuture("Branch not found");
            }

            String branchName = rows.iterator().next().getString("branch_name");

            String sql = """
                INSERT INTO users(
                    full_name, username, password, email, phone,
                    role, branch_name, branch_id, created_at
                )
                VALUES ($1,$2,$3,$4,$5,$6,$7,$8,NOW())
                RETURNING id, full_name, username, email, phone,
                          role, branch_name, branch_id, created_at
            """;

            return client.preparedQuery(sql)
                .execute(Tuple.of(
                    data.getString("fullName"),
                    data.getString("username"),
                    data.getString("password"),
                    data.getString("email"),
                    data.getString("phone"),
                    role,
                    branchName,
                    branchId
                ));
        })
        .map(rows -> mapUser(rows.iterator().next()));
}

    // ---------------- Update user ----------------
   public Future<JsonObject> updateUser(Long id, JsonObject data) {

    Integer incomingBranchId = data.getInteger("branchId");

    // 1️⃣ If branchId not provided → keep existing branch
    Future<Row> currentUserFuture =
        client.preparedQuery("SELECT branch_id, branch_name FROM users WHERE id = $1")
            .execute(Tuple.of(id))
            .map(rows -> {
                if (rows.rowCount() == 0) {
                    throw new RuntimeException("User not found");
                }
                return rows.iterator().next();
            });

    return currentUserFuture.compose(currentRow -> {

        Integer finalBranchId = incomingBranchId != null
            ? incomingBranchId
            : currentRow.getInteger("branch_id");

        // 2️⃣ Fetch branch name only if branch changed
        Future<String> branchNameFuture;

        if (incomingBranchId != null) {
            branchNameFuture =
                client.preparedQuery("SELECT branch_name FROM branches WHERE id = $1")
                    .execute(Tuple.of(finalBranchId))
                    .map(rows -> rows.iterator().next().getString("branch_name"));
        } else {
            branchNameFuture = Future.succeededFuture(
                currentRow.getString("branch_name")
            );
        }

        return branchNameFuture.compose(branchName -> {

            String sql = """
                UPDATE users SET
                    full_name = $1,
                    username = $2,
                    email = $3,
                    phone = $4,
                    role = $5,
                    branch_id = $6,
                    branch_name = $7
                WHERE id = $8
                RETURNING *
            """;

            return client.preparedQuery(sql)
                .execute(Tuple.of(
                    data.getString("fullName"),
                    data.getString("username"),
                    data.getString("email"),
                    data.getString("phone"),
                    data.getString("role"),
                    finalBranchId,
                    branchName,
                    id
                ))
                .map(rows -> rows.iterator().next().toJson());
        });
    });
}

    // ---------------- Delete user ----------------
    public Future<Void> deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = $1";
        return client.preparedQuery(sql)
                .execute(Tuple.of(id))
                .mapEmpty();
    }
}
