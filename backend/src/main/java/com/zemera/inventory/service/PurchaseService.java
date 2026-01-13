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
    return purchaseRepo.getById(id)
        .compose(purchase -> {
            if (purchase == null) {
                return Future.failedFuture("Purchase not found");
            }

            return productRepo.increaseStock(
                    purchase.getProductId().intValue(),
                    purchase.getBranchId(),
                    purchase.getQuantity()
                )
                .compose(v -> purchaseRepo.approve(id, approvedBy));
        });
}

    public Future<Void> declinePurchase(Long id, Long approvedBy, String comment) {
        return purchaseRepo.decline(id, approvedBy, comment);
    }

}

