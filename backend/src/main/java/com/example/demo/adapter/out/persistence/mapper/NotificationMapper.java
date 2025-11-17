package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.NotificationJpaEntity;
import com.example.demo.domain.notification.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for converting between Notification domain model and JPA entity
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface NotificationMapper {

    /**
     * Convert domain Notification to JPA entity
     */
    @Mapping(target = "read", source = "read")
    NotificationJpaEntity toEntity(Notification notification);

    /**
     * Convert JPA entity to domain Notification
     * Uses the static factory method Notification.of() for reconstitution
     */
    default Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Notification.of(
            entity.getId(),
            entity.getUserId(),
            entity.getType(),
            entity.getChannel(),
            entity.getTitle(),
            entity.getMessage(),
            entity.getPriority(),
            entity.isRead(),
            entity.getCreatedAt(),
            entity.getReadAt()
        );
    }

    /**
     * Convert list of JPA entities to domain notifications
     */
    List<Notification> toDomainList(List<NotificationJpaEntity> entities);

    /**
     * Convert list of domain notifications to JPA entities
     */
    List<NotificationJpaEntity> toEntityList(List<Notification> notifications);
}
