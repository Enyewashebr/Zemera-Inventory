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
            INSERT INTO branches (branch_name, phone)
            VALUES ($1, $2)
            RETURNING id, branch_name, phone, created_at
            """;

        return client
            .preparedQuery(sql)
            .execute(io.vertx.sqlclient.Tuple.of(b.getName(), b.getPhone()))
            .map(rs -> {
                Row row = rs.iterator().next();
                return new Branch(
                    row.getInteger("id"),
                    row.getString("branch_name"),
                    row.getString("phone"),
                    row.getLocalDateTime("created_at").toString()
                );
            });
    }
    
    

    // get all branches
    public Future<List<Branch>> getAllBranches() {
        String sql = "SELECT id, branch_name, phone, created_at FROM branches ORDER BY id";
        return client.query(sql).execute()
            .map(rs -> {
                List<Branch> list = new ArrayList<>();
                for (Row row : rs) {
                    list.add(new Branch(
                        row.getInteger("id"),
                        row.getString("branch_name"),
                        row.getString("phone"),
                        row.getLocalDateTime("created_at").toString()
                    ));
                }
                return list;
            });
    }

    // update branch
    public Future<Branch> updateBranch(Long id, Branch b) {

    String sql = """
        UPDATE branches
        SET branch_name = $1,
            phone = $2
        WHERE id = $3
        RETURNING id, branch_name, phone, created_at
        """;

    return client
        .preparedQuery(sql)
        .execute(io.vertx.sqlclient.Tuple.of(
                b.getName(),
                b.getPhone(),
                id
        ))
        .map(rs -> {

            // VERY IMPORTANT → handle branch not found
            if (!rs.iterator().hasNext()) {
                throw new RuntimeException("Branch not found");
            }

            Row row = rs.iterator().next();

            return new Branch(
                row.getInteger("id"),
                row.getString("branch_name"),
                row.getString("phone"),
                row.getLocalDateTime("created_at").toString()
            );
        });
}


// delete branch
    public Future<Void> deleteBranch(Long id) {

    String sql = "DELETE FROM branches WHERE id = $1";

    return client
        .preparedQuery(sql)
        .execute(io.vertx.sqlclient.Tuple.of(id))
        .map(rs -> {

            if (rs.rowCount() == 0) {
                throw new RuntimeException("Branch not found");
            }

            return null;
        });
}
}
