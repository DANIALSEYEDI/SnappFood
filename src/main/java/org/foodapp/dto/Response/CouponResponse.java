package org.foodapp.dto.Response;
import org.foodapp.model.Coupon;
import java.math.BigDecimal;
public class CouponResponse {
    public Long id;
    public String coupon_code;
    public String type;
    public BigDecimal value;
    public Integer min_price;
    public Integer user_count;
    public String start_date;
    public String end_date;

    public static CouponResponse fromEntity(Coupon c) {
        CouponResponse dto = new CouponResponse();
        dto.id = c.getId();
        dto.coupon_code = c.getCouponCode();
        dto.type = c.getType().name().toLowerCase();
        dto.value = c.getValue();
        dto.min_price = c.getMinPrice();
        dto.user_count = c.getUserCount();
        dto.start_date = c.getStartDate().toString();
        dto.end_date = c.getEndDate().toString();
        return dto;
    }
}
