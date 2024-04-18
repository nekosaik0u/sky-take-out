package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 根据 用户id 获得地址簿对象
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        // 获取当前 用户id
        Long userId = BaseContext.getCurrentId();
        // 获取当前用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());

        // 订单对象
        Orders orders = new Orders();
        // 订单明细集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        // 返回结果
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();

        // 1.处理各种业务异常(地址簿为空，购物车为空)
        if(addressBook == null){
            // 抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 2.向订单表插入 1 条数据
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setAddress(addressBook.toString()); // 设置地址
        orders.setOrderTime(LocalDateTime.now()); // 设置订单时间
        orders.setUserId(BaseContext.getCurrentId()); // 设置用户id
        orders.setConsignee(addressBook.getConsignee()); // 设置收货人姓名
        orders.setPayStatus(Orders.UN_PAID); // 设置支付状态
        orders.setStatus(Orders.PENDING_PAYMENT); // 设置订单状态
        orders.setNumber(UUID.randomUUID().toString()); // 设置订单号
        orders.setPhone(addressBook.getPhone()); // 设置电话

        orderMapper.insert(orders);

        // 3.向订单明细表插入 n 条数据
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail); // 向列表中加入数据
        }
        orderDetailMapper.insertBatch(orderDetailList); // 批量加入订单明细表

        // 4.清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        // 5.封装VO返回结果
        orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }
}
