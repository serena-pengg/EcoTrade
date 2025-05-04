package com.example.Secondhand.service;

import com.example.Secondhand.model.ShippingAddress;
import com.example.Secondhand.model.User;
import com.example.Secondhand.repository.ShippingAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShippingAddressService {

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    public List<ShippingAddress> getUserAddresses(User user) {
        return shippingAddressRepository.findByUser(user);
    }

    public ShippingAddress getDefaultAddress(User user) {
        List<ShippingAddress> defaultAddresses = shippingAddressRepository.findByUserAndIsDefaultTrue(user);
        return defaultAddresses.isEmpty() ? null : defaultAddresses.get(0);
    }

    @Transactional
    public ShippingAddress saveAddress(ShippingAddress address) {
        // 如果是默认地址，先将该用户的其他地址设为非默认
        if (address.isDefault()) {
            List<ShippingAddress> userAddresses = shippingAddressRepository.findByUser(address.getUser());
            userAddresses.forEach(addr -> {
                if (!addr.getId().equals(address.getId())) {
                    addr.setDefault(false);
                    shippingAddressRepository.save(addr);
                }
            });
        }
        return shippingAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        if (!shippingAddressRepository.existsById(addressId)) {
            throw new RuntimeException("地址不存在");
        }
        try {
            shippingAddressRepository.deleteById(addressId);
            shippingAddressRepository.flush(); // 确保立即执行删除操作
        } catch (Exception e) {
            throw new RuntimeException("删除地址失败: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteUserAddresses(User user) {
        shippingAddressRepository.deleteByUser(user);
    }
} 