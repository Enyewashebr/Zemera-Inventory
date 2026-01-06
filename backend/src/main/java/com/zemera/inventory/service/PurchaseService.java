package com.zemera.inventory.service;

import com.zemera.inventory.model.Purchase;
import com.zemera.inventory.repository.PurchaseRepository;
import io.vertx.core.Future;

import java.util.List;

public class PurchaseService {

    private final PurchaseRepository repo;

    public PurchaseService(PurchaseRepository repo) {
        this.repo = repo;
    }

    public Future<Purchase> createPurchase(Purchase p) {
        return repo.create(p);
    }

    public Future<List<Purchase>> getAllPurchases() {
        return repo.getAll();
    }

    public Future<Purchase> getPurchaseById(Long id) {
        return repo.getById(id);
    }

    public Future<Purchase> updatePurchase(Long id, Purchase p) {
        return repo.update(id, p);
    }
    public Future<List<Purchase>> getPurchasesByBranch(Integer branchId) {
    return repo.getByBranchId(branchId);
}
    public Future<Void> deletePurchase(Long id) {
        return repo.delete(id);
    }
    public Future<Void> approvePurchase(Long id, Long approvedBy) {
    return repo.approve(id, approvedBy);
}

public Future<Void> declinePurchase(Long id, Long approvedBy, String comment) {
    return repo.decline(id, approvedBy, comment);
}

}

