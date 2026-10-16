package com.tse.erp.module.hr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OfficeCalendarDto {

    private Long id;
    private Long buId;
    private List<Long> buIds;
    private Integer year;
    private Long monthId;
    private Integer status;
    private List<OfficeCalendarDayDto> days;
}
