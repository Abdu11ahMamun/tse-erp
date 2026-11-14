package com.tse.erp.module.accounting.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures database-level FOREIGN KEY (ON DELETE RESTRICT) constraints exist
 * on AFM accounting tables.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AfmDatabaseConstraintInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureForeignKeyConstraint(
                "AFM_P_TYPE_P_THROUGH_MAP",
                "PAYMENT_TYPE_ID",
                "AFM_LOOKUP_VALUE",
                "ID",
                "FK_AFM_PT_PTH_MAP_PAYMENT_TYPE"
        );
        ensureForeignKeyConstraint(
                "AFM_P_TYPE_P_THROUGH_MAP",
                "PAYMENT_THROUGH_ID",
                "AFM_LOOKUP_VALUE",
                "ID",
                "FK_AFM_PT_PTH_MAP_PAYMENT_THROUGH"
        );
        ensureForeignKeyConstraint(
                "AFM_V_TYPE_P_TYPE_MAP",
                "VOUCHER_TYPE_ID",
                "AFM_LOOKUP_VALUE",
                "ID",
                "FK_AFM_VT_PT_MAP_VOUCHER_TYPE"
        );
        ensureForeignKeyConstraint(
                "AFM_V_TYPE_P_TYPE_MAP",
                "PAYMENT_TYPE_ID",
                "AFM_LOOKUP_VALUE",
                "ID",
                "FK_AFM_VT_PT_MAP_PAYMENT_TYPE"
        );
        ensureForeignKeyConstraint(
                "AFM_BANK_CHEQUE_MAP",
                "BANK_ID",
                "AFM_BANK",
                "ID",
                "FK_AFM_BC_MAP_BANK"
        );
        ensureForeignKeyConstraint(
                "AFM_BANK_CHEQUE_MAP",
                "BANK_ACCOUNT_ID",
                "AFM_BANK_DTLS",
                "ID",
                "FK_AFM_BC_MAP_BANK_ACCOUNT"
        );
        ensureForeignKeyConstraint(
                "AFM_BANK_CHEQUE_MAP",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_BC_MAP_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_BANK_CHEQUE_MAP",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_BC_MAP_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_CALENDAR",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_CAL_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_CALENDAR",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_CAL_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_CALENDAR_MONTH",
                "AFM_CALENDAR_ID",
                "AFM_CALENDAR",
                "ID",
                "FK_AFM_CAL_MONTH_CAL"
        );
        ensureForeignKeyConstraint(
                "AFM_PERIOD",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_PERIOD_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_PERIOD",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_PERIOD_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_VOUCHER",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_VOUCHER_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_VOUCHER",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_VOUCHER_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_VOUCHER_DETAIL",
                "VOUCHER_ID",
                "AFM_VOUCHER",
                "ID",
                "FK_AFM_VOUCHER_DTL_VOUCHER"
        );
        ensureForeignKeyConstraint(
                "AFM_VOUCHER_ATTACH",
                "VOUCHER_ID",
                "AFM_VOUCHER",
                "ID",
                "FK_AFM_VOUCHER_ATT_VOUCHER"
        );
        ensureForeignKeyConstraint(
                "AFM_USER_LOCATION",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_USER_LOCATION_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_USER_LOCATION",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_USER_LOCATION_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_USER_SETUP",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_USER_SETUP_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_USER_SETUP",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_USER_SETUP_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_USER_SETUP",
                "USER_ID",
                "ADMIN_USERS",
                "ID",
                "FK_AFM_USER_SETUP_USER"
        );
        ensureForeignKeyConstraint(
                "AFM_SUPPLIER_INFO",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_SUPPLIER_INFO_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_SUPPLIER_INFO",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_SUPPLIER_INFO_BU"
        );
        ensureForeignKeyConstraint(
                "AFM_CUSTOMER_INFO",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_AFM_CUSTOMER_INFO_BG"
        );
        ensureForeignKeyConstraint(
                "AFM_CUSTOMER_INFO",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_AFM_CUSTOMER_INFO_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_CATEGORY",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_CAT_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_CATEGORY",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_CAT_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_BRAND",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_BRAND_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_BRAND",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_BRAND_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_STORE",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_STORE_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_STORE",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_STORE_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_WAREHOUSE",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_WH_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_WAREHOUSE",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_WH_BU"
        );
        ensureWarehouseStoreColumnIsVarchar();
        ensureForeignKeyConstraint(
                "INV_ITEM_ATTRIBUTE",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_ATTR_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_ATTRIBUTE",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_ATTR_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM",
                "CATEGORY_ID",
                "INV_ITEM_CATEGORY",
                "ID",
                "FK_INV_ITEM_CAT"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM",
                "BRAND_ID",
                "INV_ITEM_BRAND",
                "ID",
                "FK_INV_ITEM_BRAND"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM",
                "UOM_ID",
                "asset_uom",
                "id",
                "FK_INV_ITEM_UOM"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM",
                "STORE_ID",
                "INV_ITEM_STORE",
                "ID",
                "FK_INV_ITEM_STORE"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_ATTRIBUTE_DTLS",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_ITEM_ATTR_DTLS_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_ATTRIBUTE_DTLS",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_ITEM_ATTR_DTLS_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_ATTRIBUTE_DTLS",
                "ITEM_ID",
                "INV_ITEM",
                "ID",
                "FK_INV_ITEM_ATTR_DTLS_ITEM"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_ATTRIBUTE_DTLS",
                "ATTRIBUTE_ID",
                "INV_ITEM_ATTRIBUTE",
                "ID",
                "FK_INV_ITEM_ATTR_DTLS_ATTR"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_WAREHS_EMPL_MAP",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_WH_EMP_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_WAREHS_EMPL_MAP",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_WH_EMP_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_WAREHS_EMPL_MAP",
                "WAREHOUSE_ID",
                "INV_ITEM_WAREHOUSE",
                "ID",
                "FK_INV_WH_EMP_WH"
        );
        ensureWarehouseEmployeeColumnIsBigint();
        ensureForeignKeyConstraint(
                "INV_ITEM_WAREHS_EMPL_MAP",
                "EMPLOYEE_ID",
                "hr_employee",
                "id",
                "FK_INV_WH_EMP_EMP"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_STORE_EMPL_MAP",
                "BG_ID",
                "ADMIN_BG",
                "ID",
                "FK_INV_STR_EMP_BG"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_STORE_EMPL_MAP",
                "BU_ID",
                "ADMIN_BU",
                "ID",
                "FK_INV_STR_EMP_BU"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_STORE_EMPL_MAP",
                "STORE_ID",
                "INV_ITEM_STORE",
                "ID",
                "FK_INV_STR_EMP_STR"
        );
        ensureForeignKeyConstraint(
                "INV_ITEM_STORE_EMPL_MAP",
                "EMPLOYEE_ID",
                "hr_employee",
                "id",
                "FK_INV_STR_EMP_EMP"
        );
    }

    private void ensureForeignKeyConstraint(
            String tableName,
            String columnName,
            String referencedTable,
            String referencedColumn,
            String constraintName) {
        try {
            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "  AND TABLE_NAME = ? " +
                    "  AND CONSTRAINT_NAME = ?",
                    Integer.class,
                    tableName,
                    constraintName
            );

            if (existingCount != null && existingCount > 0) {
                return;
            }

            String alterSql = String.format(
                    "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE RESTRICT ON UPDATE RESTRICT",
                    tableName,
                    constraintName,
                    columnName,
                    referencedTable,
                    referencedColumn
            );
            jdbcTemplate.execute(alterSql);
            log.info("Added foreign key constraint {} on {}.{}", constraintName, tableName, columnName);
        } catch (Exception ex) {
            log.warn("Could not automatically verify/add FK constraint {} on {}.{}: {}",
                    constraintName, tableName, columnName, ex.getMessage());
        }
    }

    private void ensureWarehouseStoreColumnIsVarchar() {
        try {
            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "  AND TABLE_NAME = 'INV_ITEM_WAREHOUSE' " +
                    "  AND CONSTRAINT_NAME = 'FK_INV_ITEM_WH_STORE'",
                    Integer.class
            );
            if (existingCount != null && existingCount > 0) {
                jdbcTemplate.execute("ALTER TABLE INV_ITEM_WAREHOUSE DROP FOREIGN KEY FK_INV_ITEM_WH_STORE");
                log.info("Dropped FK constraint FK_INV_ITEM_WH_STORE on INV_ITEM_WAREHOUSE.STORE_ID");
            }
        } catch (Exception ex) {
            log.warn("Could not check/drop FK constraint FK_INV_ITEM_WH_STORE: {}", ex.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE INV_ITEM_WAREHOUSE MODIFY COLUMN STORE_ID VARCHAR(500) NULL");
            log.info("Ensured INV_ITEM_WAREHOUSE.STORE_ID is VARCHAR(500)");
        } catch (Exception ex) {
            log.warn("Could not alter INV_ITEM_WAREHOUSE.STORE_ID to VARCHAR(500): {}", ex.getMessage());
        }
    }

    private void ensureWarehouseEmployeeColumnIsBigint() {
        try {
            jdbcTemplate.execute("ALTER TABLE INV_ITEM_WAREHS_EMPL_MAP MODIFY COLUMN EMPLOYEE_ID BIGINT(20) NOT NULL");
            log.info("Ensured INV_ITEM_WAREHS_EMPL_MAP.EMPLOYEE_ID is BIGINT(20)");
        } catch (Exception ex) {
            log.warn("Could not alter INV_ITEM_WAREHS_EMPL_MAP.EMPLOYEE_ID to BIGINT(20): {}", ex.getMessage());
        }
    }
}
