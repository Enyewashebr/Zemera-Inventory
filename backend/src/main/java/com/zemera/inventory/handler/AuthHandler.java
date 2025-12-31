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

        String username = body.getString("username");
        String password = body.getString("password");
        String role = body.getString("role");
        Integer branchId = body.getInteger("branchId");

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
                .put("username", username)
                .put("password", password)
                .put("role", role)
                .put("branchId", branchId);

        authService.register(newUser)
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
}
