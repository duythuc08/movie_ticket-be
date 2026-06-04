package com.example.movie_ticket_be.cinema.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.cinema.dto.request.AdminSeatUpdate;
import com.example.movie_ticket_be.cinema.dto.request.SeatSetupRequest;
import com.example.movie_ticket_be.cinema.dto.response.SeatResponse;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.entity.Seats;
import com.example.movie_ticket_be.cinema.enums.SeatStatus;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.cinema.mapper.SeatMapper;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.mapper.ShowTimeMapper;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.cinema.dto.request.AdminSeatStatusUpdateRequest;
import java.time.LocalDateTime;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.cinema.repository.SeatRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminSeatService {
	RoomRepository roomRepository;
	SeatRepository seatRepository;
	SeatMapper seatMapper;
	SeatShowTimeRepository seatShowTimeRepository;
	ShowTimeMapper showTimeMapper;

	public List<SeatResponse> getSeatsByRoom(Long roomId) {
		return seatRepository.findByRooms_RoomId(roomId).stream().map(seatMapper::toSeatResponse).toList();
	}

	public Set<SeatType> getDistinctSeatTypesByRoomId(Long roomId) {
		return new HashSet<>(seatRepository.findDistinctSeatTypeByRoomId(roomId));
	}

	public void changeStatus(long id, EntityStatus entityStatus) {
		Seats seat = seatRepository.findBySeatId(id).orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
		seat.setEntityStatus(entityStatus);
		seatRepository.save(seat);
	}

	public SeatResponse updateSeatType(Long seatId, AdminSeatUpdate request) {
		Seats seat = seatRepository.findBySeatId(seatId).orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
		seat.setSeatType(request.getSeatTypes());
		return seatMapper.toSeatResponse(seatRepository.save(seat));
	}

	public List<ShowTimeResponse> getBlockedUpcomingShowTimesBySeat(Long seatId) {
		return seatShowTimeRepository.findBlockedUpcomingBySeat(seatId, LocalDateTime.now()).stream()
				.map(ss -> showTimeMapper.toShowTimeResponse(ss.getShowTimes())).toList();
	}

	@Transactional
	public SeatResponse updateSeatStatus(Long seatId, AdminSeatStatusUpdateRequest request) {
		Seats seat = seatRepository.findBySeatId(seatId).orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

		seat.setSeatStatus(request.getSeatStatus());
		seatRepository.save(seat);

		if (request.getUnlockShowTimeIds() != null && !request.getUnlockShowTimeIds().isEmpty()) {
			List<SeatShowTime> blockedSeats = seatShowTimeRepository.findBlockedUpcomingBySeat(seatId,
					LocalDateTime.now());
			for (SeatShowTime ss : blockedSeats) {
				if (request.getUnlockShowTimeIds().contains(ss.getShowTimes().getShowTimeId())) {
					ss.setSeatShowTimeStatus(SeatShowTimeStatus.AVAILABLE);
					seatShowTimeRepository.save(ss);
				}
			}
		}
		return seatMapper.toSeatResponse(seat);
	}

	@Transactional
	public List<SeatResponse> setUpSeatsForRoom(Long roomId, SeatSetupRequest request) {
		Rooms room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

		seatRepository.deleteAllByRoomId(roomId);

		int totalRows = request.getRows();
		int totalCols = request.getCols();
		SeatType[][] seatTypes = request.getSeatTypes();

		double idealRow = (totalRows - 1) * 0.6;
		double centerCol = (totalCols - 1) / 2.0;

		double ratio = (double) totalCols / totalRows;
		double colWeight = Math.min(0.7, Math.max(0.3, 0.5 + (ratio - 1) * 0.1));
		double rowWeight = 1.0 - colWeight;

		double maxRowDist = Math.max(idealRow, totalRows - 1 - idealRow);

		List<Seats> seats = new ArrayList<>();
		for (int r = 0; r < totalRows; r++) {
			for (int c = 0; c < totalCols; c++) {
				SeatType type = seatTypes[r][c];
				if (type == null)
					continue;

				double rowDist = (maxRowDist == 0) ? 0.0 : Math.abs(r - idealRow) / maxRowDist;
				double colDist = (centerCol == 0) ? 0.0 : Math.abs(c - centerCol) / centerCol;
				double distScore = rowWeight * rowDist + colWeight * colDist;
				double viewScore = 10.0 - (distScore * 9.0);

				Seats seat = Seats.builder().seatRow(toRowLabel(r)).seatNumber(c + 1).rooms(room).seatType(type)
						.seatStatus(SeatStatus.NORMAL)
						.viewQuanlityScore(BigDecimal.valueOf(Math.round(viewScore * 100.0) / 100.0)).build();
				seat.setEntityStatus(EntityStatus.ACTIVE);
				seats.add(seat);
			}
		}

		return seatRepository.saveAll(seats).stream().map(seatMapper::toSeatResponse).toList();
	}

	@Transactional
	public List<SeatResponse> updateSeatTypes(Long roomId, List<AdminSeatUpdate> requests) {
		List<Seats> seats = new ArrayList<>();
		for (AdminSeatUpdate r : requests) {
			Seats seat = seatRepository
					.findBySeatRowAndSeatNumberAndRooms_RoomId(r.getSeatRow(), r.getSeatNumber(), roomId)
					.orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
			seat.setSeatType(r.getSeatTypes());
			seats.add(seat);
		}
		return seatRepository.saveAll(seats).stream().map(seatMapper::toSeatResponse).toList();
	}

	private static String toRowLabel(int index) {
		StringBuilder sb = new StringBuilder();
		int n = index;
		do {
			sb.insert(0, (char) ('A' + n % 26));
			n = n / 26 - 1;
		} while (n >= 0);
		return sb.toString();
	}
}
