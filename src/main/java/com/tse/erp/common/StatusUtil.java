package com.tse.erp.common;

import com.tse.erp.exception.BadRequestException;

public final class StatusUtil {

    private StatusUtil() {}

    // null hole null-i return kore (caller decide korbe), 0/1 chara hole 400
    public static Integer validate(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BadRequestException(
                    "Status must be 0 (Inactive) or 1 (Active)");
        }
        return status;
    }
}