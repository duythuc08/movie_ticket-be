package com.example.movie_ticket_be.booking.mapper;

import com.example.movie_ticket_be.booking.dto.response.*;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

	@Mapping(source = "users.userId", target = "userId")
	@Mapping(target = "fullName", expression = "java(orders.getUsers().getFirstname() + \" \" + orders.getUsers().getLastname())")
	@Mapping(source = "orderTickets", target = "tickets")
	@Mapping(source = "orderFoods", target = "foods")
	@Mapping(target = "showTimeInfo", ignore = true)
	@Mapping(target = "payment", ignore = true)
	OrderResponse toOrderResponse(Orders orders);

	@Mapping(target = "seatName", expression = "java(getSeatName(orderTickets))")
	@Mapping(source = "seatShowTime.seats.seatType", target = "seatType")
	OrderTicketResponse toTicketResponse(OrderTickets orderTickets);

	@Mapping(source = "foods.foodId", target = "foodId")
	@Mapping(source = "foods.name", target = "name")
	OrderFoodResponse toFoodResponse(OrderFoods orderFoods);

	List<OrderResponse> toOrderResponseList(List<Orders> orders);

	@AfterMapping
	default void populateShowTimeInfo(Orders orders, @MappingTarget OrderResponse.OrderResponseBuilder response) {
		if (orders.getOrderTickets() == null || orders.getOrderTickets().isEmpty())
			return;

		OrderTickets firstTicket = orders.getOrderTickets().iterator().next();
		if (firstTicket.getSeatShowTime() == null)
			return;

		ShowTimes st = firstTicket.getSeatShowTime().getShowTimes();
		if (st == null)
			return;

		String roomName = null, cinemaName = null, cinemaAddress = null;
		if (st.getRooms() != null) {
			roomName = st.getRooms().getName();
			if (st.getRooms().getCinemas() != null) {
				cinemaName = st.getRooms().getCinemas().getName();
				cinemaAddress = st.getRooms().getCinemas().getAddress();
			}
		}

		response.showTimeInfo(ShowTimeInfo.builder()
				.movieName(st.getMovies() != null ? st.getMovies().getTitle() : null).roomName(roomName)
				.showTime(st.getStartTime()).cinemaName(cinemaName).cinemaAddress(cinemaAddress).build());
	}

	default String getSeatName(OrderTickets orderTickets) {
		if (orderTickets.getSeatShowTime() != null && orderTickets.getSeatShowTime().getSeats() != null) {
			return orderTickets.getSeatShowTime().getSeats().getSeatRow() + String.valueOf(orderTickets.getSeatShowTime().getSeats().getSeatNumber());
		}
		return "---";
	}
}
