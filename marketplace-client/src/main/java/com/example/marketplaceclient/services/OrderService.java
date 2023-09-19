package com.example.marketplaceclient.services;

import com.example.marketplaceclient.model.DeliveryDate;
import com.example.marketplaceclient.model.UserInformation;
import com.example.marketplaceclient.model.dto.CreateOrderDto;
import com.example.marketplaceclient.model.dto.CreateUnregisteredUserOrder;
import com.example.marketplaceclient.model.dto.OrderDto;

import java.util.List;

public interface OrderService {

    List<OrderDto> getAllOrders();

    List<OrderDto> getCartList(String userId);

    List<OrderDto> getConfirmedOrdersByLocationId(String locationId);

    void sendCommentToSeller(UserInformation userInformation);

    OrderDto addProductToCartItems(CreateOrderDto orderDto);

    void updateOrder(int id);

    void cancelOrder(int id);

    List<OrderDto> getConfirmedOrders(String userId);

    void increaseQuantity(int id, String sku);

    void decreaseQuantity(int id, String sku);

    void deleteItemFromOrder(int id, String sku);

    OrderDto sendUnregisteredUserOrder(CreateUnregisteredUserOrder order);

    long getOrderTotalCount(String locationId);

    long getOrderSoldCount(String locationId);

    long getOrderCanceledCount(String locationId);

    void updateDeliveryDate(int id, DeliveryDate newDate);

    List<OrderDto> getOrdersByLocationId(String locationId, String status, Integer size);
    OrderDto getSingleOrder(int id);
}
