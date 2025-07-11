package org.foodapp.dto;

import java.util.List;

public class OrderRequest {
    public List<Long> itemIds; // شناسه‌ی آیتم‌های سفارش داده شده
    public String note;
}
