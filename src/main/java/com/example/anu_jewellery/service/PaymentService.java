package com.example.anu_jewellery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.anu_jewellery.dto.CreatePaymentOrderResponse;
import com.example.anu_jewellery.dto.VerifyPaymentRequest;
import com.example.anu_jewellery.entity.CartItem;
import com.example.anu_jewellery.entity.OrderItem;
import com.example.anu_jewellery.entity.PaymentTransaction;
import com.example.anu_jewellery.entity.Product;
import com.example.anu_jewellery.entity.PurchaseOrder;
import com.example.anu_jewellery.entity.User;
import com.example.anu_jewellery.repository.CartItemRepository;
import com.example.anu_jewellery.repository.OrderItemRepository;
import com.example.anu_jewellery.repository.PaymentTransactionRepository;
import com.example.anu_jewellery.repository.ProductRepository;
import com.example.anu_jewellery.repository.PurchaseOrderRepository;
import com.example.anu_jewellery.repository.UserRepository;

@Service
public class PaymentService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RazorpayService razorpayService;

    public PaymentService(CartItemRepository cartItemRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          PurchaseOrderRepository purchaseOrderRepository,
                          OrderItemRepository orderItemRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          RazorpayService razorpayService) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public CreatePaymentOrderResponse createPaymentOrder() {
        User user = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(subtotal);
        }

        PurchaseOrder order = new PurchaseOrder(user, totalAmount, razorpayService.getCurrency(), "CREATED");
        purchaseOrderRepository.save(order);

        long amountInPaise = toPaise(totalAmount);
        String receipt = "receipt_" + order.getId();

        RazorpayService.RazorpayOrderResponse razorpayOrder =
                razorpayService.createOrder(amountInPaise, receipt);

        order.setRazorpayOrderId(razorpayOrder.id());
        order.setStatus("PENDING_PAYMENT");
        purchaseOrderRepository.save(order);

        return new CreatePaymentOrderResponse(
                order.getId(),
                razorpayOrder.id(),
                amountInPaise,
                razorpayService.getCurrency(),
                razorpayService.getKeyId()
        );
    }

    @Transactional
    public String verifyPayment(VerifyPaymentRequest request) {
        User user = getCurrentUser();

        // 1. Find the order directly using Razorpay's Order ID sent back in the request
        PurchaseOrder order = purchaseOrderRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Ensure the order belongs to the currently logged-in user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized order access");
        }

        if ("PAID".equals(order.getStatus())) {
            return "Payment already verified and order already placed";
        }

        // 3. Verify the cryptographic signature
        boolean validSignature = razorpayService.verifySignature(
                order.getRazorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!validSignature) {
            paymentTransactionRepository.save(new PaymentTransaction(
                    order,
                    request.razorpayPaymentId(),
                    request.razorpayOrderId(),
                    request.razorpaySignature(),
                    "FAILED"
            ));
            throw new RuntimeException("Invalid payment signature");
        }

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 4. Move items from cart to order items and reduce stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    product.getPrice()
            );
            orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 5. Save success payment transaction record
        paymentTransactionRepository.save(new PaymentTransaction(
                order,
                request.razorpayPaymentId(),
                request.razorpayOrderId(),
                request.razorpaySignature(),
                "SUCCESS"
        ));

        // 6. Update order status and clear the cart
        order.setStatus("PAID");
        purchaseOrderRepository.save(order);

        cartItemRepository.deleteByUser(user);

        return "Payment verified and order placed successfully";
    }
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
    }

    private long toPaise(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}