package com.loopers.domain.order.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceiverInfo {
    
    @Column(name = "receiver_name")
    private String name;
    
    @Column(name = "receiver_phone")
    private String phone;
    
    @Column(name = "receiver_zip_code")
    private String zipCode;
    
    @Column(name = "receiver_address")
    private String address;
    
    @Column(name = "receiver_address_detail")
    private String addressDetail;
    
    public ReceiverInfo(String name, String phone, String zipCode, String address, String addressDetail) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수령인 이름은 필수입니다.");
        }
        if (phone == null || phone.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수령인 전화번호는 필수입니다.");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "우편번호는 필수입니다.");
        }
        if (address == null || address.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주소는 필수입니다.");
        }
        
        this.name = name;
        this.phone = phone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }
}
