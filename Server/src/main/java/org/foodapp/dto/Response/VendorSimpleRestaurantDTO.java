package org.foodapp.dto.Response;
import org.foodapp.model.Restaurant;

public class VendorSimpleRestaurantDTO {
    public Long id;
    public String name;
    public String address;
    public String phone;
    public String logoBase64;
    public Integer taxFee;
    public Integer additionalFee;

    public static VendorSimpleRestaurantDTO from(Restaurant r) {
        VendorSimpleRestaurantDTO dto = new VendorSimpleRestaurantDTO();
        dto.id = r.getId();
        dto.name = r.getName();
        dto.address = r.getAddress();
        dto.phone = r.getPhone();
        dto.logoBase64 = r.getLogoBase64();
        dto.taxFee = r.getTaxFee();
        dto.additionalFee = r.getAdditionalFee();
        return dto;
    }
}