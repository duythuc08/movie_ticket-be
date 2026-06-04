package com.example.movie_ticket_be.cinema.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.cinema.dto.request.AdminRoomRequest;
import com.example.movie_ticket_be.cinema.dto.request.RoomRequest;
import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.mapper.RoomMapper;
import com.example.movie_ticket_be.cinema.repository.CinemaRepository;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminRoomService {
	RoomRepository roomRepository;
	RoomMapper roomMapper;
	ShowTimeRepository showTimeRepository;
	CinemaRepository cinemaRepository;
	public RoomResponse createRoom(RoomRequest request) {
		Long cinemaId = request.getCinemas().getCinemaId();
		if (cinemaRepository.findByCinemaId(cinemaId).isEmpty()) {
			throw new AppException(ErrorCode.CINEMA_NOT_FOUND);
		}
		if (roomRepository.existsByNameAndCinemas_CinemaId(request.getName(), cinemaId)) {
			throw new AppException(ErrorCode.ROOM_EXISTED);
		}
		Rooms rooms = roomMapper.toRooms(request);
		rooms.setEntityStatus(EntityStatus.ACTIVE);
		return roomMapper.toRoomResponse(roomRepository.save(rooms));
	}

	public List<RoomResponse> createRooms(List<RoomRequest> requests) {
		return requests.stream().map(this::createRoom).toList();
	}

	public RoomResponse updateRoom(long roomId, AdminRoomRequest request) {
		Rooms room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

		if (!room.getName().equals(request.getName()) && roomRepository.existsByNameAndCinemas_CinemaIdAndRoomIdNot(
				request.getName(), room.getCinemas().getCinemaId(), roomId)) {
			throw new AppException(ErrorCode.ROOM_EXISTED);
		}

		if (request.getRoomType() != null && !request.getRoomType().equals(room.getRoomType())) {
			if (showTimeRepository.existsByRooms_RoomIdAndShowTimeStatusIn(roomId,
					List.of(ShowTimeStatus.SCHEDULED, ShowTimeStatus.ONGOING))) {
				throw new AppException(ErrorCode.ROOM_HAS_ACTIVE_SHOWTIME);
			}
		}

		room.setName(request.getName());
		room.setCapacity(request.getCapacity());
		room.setRoomType(request.getRoomType());
		room.setRoomStatus(request.getRoomStatus());

		return roomMapper.toRoomResponse(roomRepository.save(room));
	}

	public Page<RoomResponse> getRooms(Specification<Rooms> spec, Pageable pageable) {
		return roomRepository.findAll(spec, pageable).map(roomMapper::toRoomResponse);
	}

	@Transactional
	public void updateRoomsForCinema(Cinemas cinema, List<AdminRoomRequest> requests) {
		for (AdminRoomRequest request : requests) {
			if (request.getRoomId() != null) {
				updateRoom(request.getRoomId(), request);
			} else {
				if (roomRepository.existsByNameAndCinemas_CinemaId(request.getName(), cinema.getCinemaId())) {
					throw new AppException(ErrorCode.ROOM_EXISTED);
				}
				Rooms newRoom = Rooms.builder().name(request.getName()).capacity(request.getCapacity())
						.roomType(request.getRoomType()).roomStatus(request.getRoomStatus()).cinemas(cinema).build();
				newRoom.setEntityStatus(EntityStatus.ACTIVE);
				roomRepository.save(newRoom);
			}
		}
	}

	public void changeStatus(long id, EntityStatus entityStatus) {
		Rooms room = roomRepository.findByRoomId(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
		room.setEntityStatus(entityStatus);
		roomRepository.save(room);
	}
}
