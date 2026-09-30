package com.portfolio.erp.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.audit.Audited;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderLine;
import com.portfolio.erp.domain.model.PurchaseOrderStatus;
import com.portfolio.erp.domain.ports.in.InventoryUseCase;
import com.portfolio.erp.domain.ports.in.PurchaseOrderUseCase;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;
import com.portfolio.erp.domain.ports.out.PurchaseOrderRepositoryPort;
import com.portfolio.erp.domain.ports.out.SupplierRepositoryPort;
import com.portfolio.erp.domain.ports.out.WarehouseRepositoryPort;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderService implements PurchaseOrderUseCase {

    private final PurchaseOrderRepositoryPort orders;
    private final SupplierRepositoryPort suppliers;
    private final ProductRepositoryPort products;
    private final WarehouseRepositoryPort warehouses;
    private final InventoryUseCase inventory;

    public PurchaseOrderService(PurchaseOrderRepositoryPort orders,
                                SupplierRepositoryPort suppliers,
                                ProductRepositoryPort products,
                                WarehouseRepositoryPort warehouses,
                                InventoryUseCase inventory) {
        this.orders = orders;
        this.suppliers = suppliers;
        this.products = products;
        this.warehouses = warehouses;
        this.inventory = inventory;
    }

    @Override
    public PageResult<PurchaseOrder> list(String search, PurchaseOrderStatus status, Long supplierId,
                                          int page, int size) {
        return orders.search(search, status, supplierId, page, size);
    }

    @Override
    public PurchaseOrder get(Long id) {
        return requireOrder(id);
    }

    @Override
    @Transactional
    public PurchaseOrder create(PurchaseOrderCommand command) {
        requireSupplier(command.supplierId());
        PurchaseOrder order = PurchaseOrder.draft(nextNumber(), command.supplierId(), command.orderDate(),
                command.notes(), buildLines(command.lines()));
        return orders.save(order);
    }

    @Override
    @Transactional
    public PurchaseOrder update(Long id, PurchaseOrderCommand command) {
        PurchaseOrder order = requireOrder(id);
        requireSupplier(command.supplierId());
        order.updateHeader(command.orderDate(), command.notes());
        order.replaceLines(buildLines(command.lines()));
        return orders.save(order);
    }

    @Override
    @Transactional
    public PurchaseOrder approve(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.approve();
        return orders.save(order);
    }

    @Override
    @Transactional
    @Audited(action = "PURCHASE_ORDER_RECEIVE", entityType = "PurchaseOrder")
    public PurchaseOrder receive(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.receive();
        order.getLines().forEach(line -> {
            inventory.receive(line.getProductId(), line.getWarehouseId(), line.getQuantity(),
                    "PURCHASE_ORDER", id);
            products.findById(line.getProductId()).ifPresent(product -> {
                product.changePrices(product.getSalePrice(), line.getUnitCost());
                products.save(product);
            });
        });
        return orders.save(order);
    }

    @Override
    @Transactional
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.cancel();
        return orders.save(order);
    }

    private List<PurchaseOrderLine> buildLines(List<LineCommand> commands) {
        List<PurchaseOrderLine> lines = new ArrayList<>();
        for (LineCommand command : commands) {
            Product product = products.findById(command.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.product.notFound"));
            warehouses.findById(command.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.warehouse.notFound"));
            lines.add(new PurchaseOrderLine(null, product.getId(), product.getSku(), product.getName(),
                    command.warehouseId(), command.quantity(), command.unitCost()));
        }
        return lines;
    }

    private PurchaseOrder requireOrder(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.purchaseOrder.notFound"));
    }

    private void requireSupplier(Long supplierId) {
        suppliers.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("error.supplier.notFound"));
    }

    private String nextNumber() {
        return "PO-%d-%05d".formatted(LocalDate.now().getYear(), orders.nextOrderNumber());
    }
}
