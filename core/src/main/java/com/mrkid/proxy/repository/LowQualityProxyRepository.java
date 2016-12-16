package com.mrkid.proxy.repository;

import com.mrkid.proxy.model.LowQualityProxy;
import com.mrkid.proxy.model.Proxy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * User: xudong
 * Date: 21/10/2016
 * Time: 11:43 AM
 */
public interface LowQualityProxyRepository extends JpaRepository<LowQualityProxy, String> {
}
