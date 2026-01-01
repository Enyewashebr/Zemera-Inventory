package com.zemera.inventory.handler;

import com.zemera.inventory.service.AuthService;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

public class AuthHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    public void loginUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        if (body == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }
        authService.login(body.getString("username"), body.getString("password"))
                .onSuccess(user -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(user.encode()))
                .onFailure(err -> ctx.response().setStatusCode(401).end(err.getMessage()));
    }

    public void registerUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        if (body == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }
        authService.registerUser(body)
                .onSuccess(user -> ctx.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(user.encode()))
                .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
    }

    public void getAllUsers(RoutingContext ctx) {
        authService.getAllUsers()
                .onSuccess(users -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(users.encode()))
                .onFailure(err -> ctx.response().setStatusCode(500).end("Internal Server Error"));
    }

    public void updateUser(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        JsonObject body = ctx.getBodyAsJson();
        if (body == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }
        authService.updateUser(id, body)
                .onSuccess(user -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(user.encode()))
                .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
    }

    public void deleteUser(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        authService.deleteUser(id)
                .onSuccess(v -> ctx.response().setStatusCode(204).end())
                .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
    }
}
