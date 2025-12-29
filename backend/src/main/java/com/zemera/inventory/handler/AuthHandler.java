package com.zemera.inventory.handler;

import com.zemera.inventory.model.LoginRequest;
import com.zemera.inventory.service.AuthService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AuthHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    public void login(RoutingContext ctx) {
        LoginRequest req = ctx.getBodyAsJson().mapTo(LoginRequest.class);

        authService.login(req).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject res = JsonObject.mapFrom(ar.result());
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(200)
                    .end(res.encode());
            } else {
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(401)
                    .end(new JsonObject().put("error", ar.cause().getMessage()).encode());
            }
            System.out.println("Login request: " + req.username);
        });
    }
}
