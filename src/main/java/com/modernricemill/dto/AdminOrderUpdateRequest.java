package com.modernricemill.dto;

import com.modernricemill.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class AdminOrderUpdateRequest {

    @NotNull
    private OrderStatus status;

    private Boolean itemReceived;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Boolean getItemReceived() {
        return itemReceived;
    }

    public void setItemReceived(Boolean itemReceived) {
        this.itemReceived = itemReceived;
    }
}
