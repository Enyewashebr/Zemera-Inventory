package com.zemera.inventory.handler;

import com.zemera.inventory.model.Branch;
import com.zemera.inventory.repository.BranchRepository;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
// import com.zemera.inventory.model.Branch;

public class BranchHandler {
    private final BranchRepository repo;
    

    public BranchHandler(BranchRepository repo) {
        this.repo = repo;
    }

    // create branch
 public void createBranch(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();

    if (body == null) {
        ctx.response().setStatusCode(400).end("Invalid JSON body");
        return;
    }

    String branch_name = body.getString("name");
    String phone = body.getString("phone");

    if (branch_name == null || branch_name.isBlank()) {
        ctx.response().setStatusCode(400).end("Branch name is required");
        return;
    }

    Branch newBranch = new Branch();
    newBranch.setName(branch_name);
    newBranch.setPhone(phone);

    repo.createBranch(newBranch)
        .onSuccess(branch -> {
            ctx.response()
               .setStatusCode(201)
               .putHeader("Content-Type", "application/json")
               .end(Json.encodePrettily(branch));
        })
        .onFailure(err -> {
            err.printStackTrace();
            ctx.response()
               .setStatusCode(500)
               .end("Failed to create branch");
        });
}


    public void getAllBranches(RoutingContext ctx) {
        repo.getAllBranches().onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response()
                   .setStatusCode(500)
                   .end(ar.cause().getMessage());
            }
        });
    }
}
