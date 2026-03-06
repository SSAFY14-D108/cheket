package com.ssafy.cheket.repository.host;

import com.ssafy.cheket.entity.host.Host;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, Long> {
    boolean existsByEmail(String email);
    boolean existsByBusinessNo(String businessNo);

}
