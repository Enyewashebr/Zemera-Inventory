package com.zemera.inventory.repository;

import com.zemera.inventory.model.Branch;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;

import java.util.ArrayList;
import java.util.List;

public class BranchRepository {
    private final Pool client;

    public BranchRepository(Pool client) {
        this.client = client;
    }

    // create a new branch
    public Future<Branch> createBranch(Branch b) {
        String sql = """
            INSERT INTO branches (name, phone)
            VALUES ($1, $2)
            RETURNING id, name, phone, created_at
            """;

        return client
            .preparedQuery(sql)
            .execute(io.vertx.sqlclient.Tuple.of(b.getName(), b.getPhone()))
            .map(rs -> {
                Row row = rs.iterator().next();
                return new Branch(
                    row.getInteger("id"),
                    row.getString("name"),
                    row.getString("phone"),
                    row.getLocalDateTime("created_at").toString()
                );
            });
    }









    public Future<List<Branch>> getAllBranches() {
        String sql = "SELECT id, name, phone, created_at FROM branches ORDER BY id";
        return client.query(sql).execute()
            .map(rs -> {
                List<Branch> list = new ArrayList<>();
                for (Row row : rs) {
                    list.add(new Branch(
                        row.getInteger("id"),
                        row.getString("name"),
                        row.getString("phone"),
                        row.getLocalDateTime("created_at").toString()
                    ));
                }
                return list;
            });
    }
}
