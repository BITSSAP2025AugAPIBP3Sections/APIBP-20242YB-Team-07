package com.cooknect.recipe_service.utils;
import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;
import org.springframework.data.domain.*;

public class PaginationUtils {
    public static Pageable toPageable(PageRequestDTO req) {

        Sort sort = req.getDirection().equalsIgnoreCase("asc")
                ? Sort.by(req.getSortBy()).ascending()
                : Sort.by(req.getSortBy()).descending();

        return PageRequest.of(req.getPage(), req.getSize(), sort);
    }

    public static <T> PageResponseDTO<T> toPageResponse(Page<T> page) {

        PageResponseDTO<T> dto = new PageResponseDTO<>();

        dto.setContent(page.getContent());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setSort(page.getSort().toString());

        return dto;
    }
}



