package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.entity.Roomamenity;

import java.util.List;

public interface IRoomAmenityService {

    void addAmenitiesToRoom(Integer roomId, List<Integer> amenityIds);


    void removeAmenityFromRoom(Integer roomId, Integer amenityId);


    List<Roomamenity> getAmenitiesByRoomId(Integer roomId);
}
