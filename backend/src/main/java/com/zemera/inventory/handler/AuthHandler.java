package com.zemera.inventory.handler;

import com.zemera.inventory.service.AuthService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.mindrot.jbcrypt.BCrypt;

public class AuthHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    // --- Existing login ---
    public void loginUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        if (body == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }

        String username = body.getString("username");
        String password = body.getString("password");

        if (username == null || password == null || password.isBlank()) {
            ctx.response().setStatusCode(400).end("Missing username or password");
            return;
        }

        authService.login(username, password)
            .onSuccess(result -> {
                ctx.response()
                   .putHeader("Content-Type", "application/json")
                   .end(result.encode());
            })
            .onFailure(err -> {
                ctx.response()
                   .setStatusCode(401)
                   .end(err.getMessage());
            });
    }

    // --- NEW: Register user ---
    public void registerUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        if (body == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }

        String fullName = body.getString("fullName");
        String username = body.getString("username");
        String password = body.getString("password");
        String email = body.getString("email");
        String phone = body.getString("phone");
        String role = body.getString("role");
        // Integer branchId = body.getInteger("branchId");
Integer branchId;
try {
    branchId = body.getInteger("branchId");
} catch (Exception e) {
    ctx.response().setStatusCode(400).end("Invalid branchId");
    return;
}
        // Validate input
        if (username == null || username.isBlank() ||
            password == null || password.isBlank() ||
            role == null || role.isBlank() ||
            branchId == null) {
            ctx.response().setStatusCode(400).end("Missing required fields");
            return;
        }

        // Build user JSON object to pass to service
        JsonObject newUser = new JsonObject()
                .put("fullName", fullName)
                .put("email", email)
                .put("username", username)
                .put("password", password)
                .put("phone", phone)
                .put("role", role)
                .put("branchId", branchId);

        authService.registerUser(newUser)
            .onSuccess(savedUser -> {
                ctx.response()
                   .setStatusCode(201)
                   .putHeader("Content-Type", "application/json")
                   .end(savedUser.encode());
            })
            .onFailure(err -> {
                ctx.response()
                 
                   .setStatusCode(500)
                   .end(err.getMessage());
            });
    }

    // --- NEW: Get all users ---
    public void getAllUsers(RoutingContext ctx) {
    authService.getAllUsers()
        .onSuccess(users -> {
            ctx.response()
               .putHeader("Content-Type", "application/json")
               .end(users.encode());
        })
        .onFailure(err -> {
            err.printStackTrace();
            ctx.response().setStatusCode(500).end("Internal Server Error");
        });
}



    // delete user
    public void deleteUser(RoutingContext ctx) {
    Integer id = Integer.valueOf(ctx.pathParam("id"));
    authService.deleteUser(id)
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> {
            err.printStackTrace();
            ctx.response().setStatusCode(500).end("Internal Server Error");
        });
}

}
