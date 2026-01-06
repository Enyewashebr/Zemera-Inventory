package com.zemera.inventory.service;

import com.zemera.inventory.model.Purchase;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.repository.PurchaseRepository;
import io.vertx.core.Future;

import java.util.List;

public class PurchaseService {

    private final PurchaseRepository purchaseRepo;
    private final ProductRepository productRepo;

    public PurchaseService(PurchaseRepository purchaseRepo, ProductRepository productRepo) {
        this.purchaseRepo = purchaseRepo;
        this.productRepo = productRepo;
    }

    public Future<Purchase> createPurchase(Purchase p) {
        return purchaseRepo.create(p);
    }

    public Future<List<Purchase>> getAllPurchases() {
        return purchaseRepo.getAll();
    }

    public Future<Purchase> getPurchaseById(Long id) {
        return purchaseRepo.getById(id);
    }

    public Future<Purchase> updatePurchase(Long id, Purchase p) {
        return purchaseRepo.update(id, p);
    }

    public Future<List<Purchase>> getPurchasesByBranch(Integer branchId) {
        return purchaseRepo.getByBranchId(branchId);
    }

    public Future<Void> deletePurchase(Long id) {
        return purchaseRepo.delete(id);
    }

    public Future<Void> approvePurchase(Long id, Long approvedBy) {
        // First get the purchase to check stock availability
        return purchaseRepo.getById(id)
            .compose(purchase -> {
                if (purchase == null) {
                    return Future.failedFuture("Purchase not found");
                }

                // Check if there's sufficient stock
                return productRepo.hasSufficientStock(purchase.getProductId().intValue(), purchase.getQuantity())
                    .compose(hasStock -> {
                        if (!hasStock) {
                            return Future.failedFuture("Insufficient stock for product ID: " + purchase.getProductId());
                        }

                        // Decrease stock and approve purchase
                        return productRepo.decreaseStock(purchase.getProductId().intValue(), purchase.getQuantity())
                            .compose(v -> purchaseRepo.approve(id, approvedBy));
                    });
            });
    }

    public Future<Void> declinePurchase(Long id, Long approvedBy, String comment) {
        return purchaseRepo.decline(id, approvedBy, comment);
    }

}

