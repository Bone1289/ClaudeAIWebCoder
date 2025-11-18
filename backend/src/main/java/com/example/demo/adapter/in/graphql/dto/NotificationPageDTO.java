package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.notification.Notification;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public record NotificationPageDTO(
        List<NotificationDTO> notifications,
        int currentPage,
        int totalPages,
        long totalItems,
        boolean hasNext,
        boolean hasPrevious
) {
    public static NotificationPageDTO fromPage(Page<Notification> page) {
        return new NotificationPageDTO(
                page.getContent().stream()
                        .map(NotificationDTO::fromDomain)
                        .collect(Collectors.toList()),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
