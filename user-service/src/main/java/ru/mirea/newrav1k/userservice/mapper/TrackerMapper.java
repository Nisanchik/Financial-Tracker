package ru.mirea.newrav1k.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.mirea.newrav1k.userservice.model.dto.TrackerResponse;
import ru.mirea.newrav1k.userservice.model.entity.Tracker;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrackerMapper {

    TrackerResponse toTrackerResponse(Tracker tracker);

}