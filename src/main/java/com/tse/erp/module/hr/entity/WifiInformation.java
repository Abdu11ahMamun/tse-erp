package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wifi_information")
@Getter
@Setter
@NoArgsConstructor
public class WifiInformation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "wifi_name", nullable = false, length = 100)
    private String wifiName;

    @Column(name = "mac_address", nullable = false, length = 100)
    private String macAddress;
}
