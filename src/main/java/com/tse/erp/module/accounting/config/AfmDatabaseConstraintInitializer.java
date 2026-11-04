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
    }

    private void ensureForeignKeyConstraint(
            String tableName,
            String columnName,
            String referencedTable,
            String referencedColumn,
            String constraintName) {
        try {
            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "  AND LOWER(TABLE_NAME) = LOWER(?) " +
                    "  AND LOWER(COLUMN_NAME) = LOWER(?) " +
                    "  AND LOWER(REFERENCED_TABLE_NAME) = LOWER(?)",
                    Integer.class,
                    tableName,
                    columnName,
                    referencedTable
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
}
