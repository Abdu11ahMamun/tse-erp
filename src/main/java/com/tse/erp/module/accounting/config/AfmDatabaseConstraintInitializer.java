package com.tse.erp.module.accounting.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures database-level FOREIGN KEY (ON DELETE RESTRICT) constraints exist
 * on AFM_P_TYPE_P_THROUGH_MAP and AFM_V_TYPE_P_TYPE_MAP referencing AFM_LOOKUP_VALUE(ID).
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

