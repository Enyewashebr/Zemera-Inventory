package com.zemera.inventory.service;



import java.util.List;


import com.zemera.inventory.model.Stock;
import com.zemera.inventory.model.StockView;
import com.zemera.inventory.repository.StockRepository;
import io.vertx.core.Future;

public class StockService {

    private final StockRepository stockRepo;

    public StockService(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    public Future<Double> getStock(int branchId, long productId) {
        return stockRepo.getStock(branchId, productId);
    }

    public Future<Void> increaseStock(int branchId, int productId, double qty) {
        return stockRepo.increaseStock(branchId, productId, qty);
    }

    public Future<Void> decreaseStock(int branchId, int productId, double qty) {
        return stockRepo.decreaseStock(branchId, productId, qty);
    }
    // public Future<List<Stock>> getMyStock(int branchId) {
    //     return stockRepo.getStockByBranch(branchId);
    // }
    //  public Future<List<Stock>> getStockByBranch(Integer branchId) {
    //     return stockRepo.getByBranchId(branchId);
    //}
    public Future<List<StockView>> getStockViewByBranch(int branchId) {
    return stockRepo.getStockViewByBranch(branchId);
}
}
