package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.response.OrderFoodResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderTicketResponse;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.mapper.OrderMapper;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.entity.Seats;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    OrderMapper orderMapper;

    @PreAuthorize("isAuthenticated()")
    public OrderResponse getOrderById(Long orderId) {
        try {
            Orders order = orderRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            String userId = null;
            String fullName = "---";
            if (order.getUsers() != null) {
                userId = order.getUsers().getUserId();
                fullName = order.getUsers().getFirstname() + " " + order.getUsers().getLastname();
            }

            //LIST TICKET
            List<OrderTicketResponse> ticketResponses = new ArrayList<>();
            if (order.getOrderTickets() != null) {
                for (OrderTickets ticket : order.getOrderTickets()) {
                    if (ticket.getSeatShowTime() != null && ticket.getSeatShowTime().getSeats() != null) {
                        Seats seat = ticket.getSeatShowTime().getSeats();
                        ShowTimes st = ticket.getSeatShowTime().getShowTimes();

                        String ticketRoomName   = (st != null && st.getRooms() != null)    ? st.getRooms().getName()           : null;
                        String ticketMovieName  = (st != null && st.getMovies() != null)   ? st.getMovies().getTitle()         : null;
                        String ticketShowTime   = (st != null && st.getStartTime() != null) ? st.getStartTime().toString()     : null;

                        ticketResponses.add(OrderTicketResponse.builder()
                                .orderTicketId(ticket.getOrderTicketId())
                                .seatName(seat.getSeatRow() + seat.getSeatNumber())
                                .seatType(seat.getSeatType())
                                .price(ticket.getPrice())
                                .roomName(ticketRoomName)
                                .movieName(ticketMovieName)
                                .showTime(ticketShowTime)
                                .build());
                    }
                }
            }

            //LIST FOOD
            List<OrderFoodResponse> foodResponses = new ArrayList<>();
            if (order.getOrderFoods() != null) {
                for (OrderFoods food : order.getOrderFoods()) {
                    foodResponses.add(OrderFoodResponse.builder()
                            .foodId(food.getFoods().getFoodId())
                            .name(food.getFoods().getName())
                            .quantity(food.getQuantity())
                            .unitPrice(food.getUnitPrice())
                            .totalPrice(food.getTotalPrice())
                            .build());
                }
            }

            //ROOM, CINEMA, MOVIE, SHOWTIME INFO
            String movieTitle = null;
            String cinemaName = null;
            String cinemaAddress = null;
            String showTimeStr = null;
            String roomName = null;

            if (order.getOrderTickets() != null && !order.getOrderTickets().isEmpty()) {
                OrderTickets firstTicket = order.getOrderTickets().iterator().next();
                if (firstTicket.getSeatShowTime() != null) {
                    ShowTimes st = firstTicket.getSeatShowTime().getShowTimes();
                    if (st != null) {
                        if (st.getMovies() != null) movieTitle = st.getMovies().getTitle();
                        if (st.getStartTime() != null) showTimeStr = st.getStartTime().toString();
                        Rooms room = st.getRooms();
                        if (room != null) {
                            roomName = room.getName();
                            Cinemas cinema = room.getCinemas();
                            if (cinema != null) {
                                cinemaName = cinema.getName();
                                cinemaAddress = cinema.getAddress();
                            }
                        }
                    }
                }
            }

            //RESPONSE
            return OrderResponse.builder()
                    .orderId(order.getOrderId())
                    .userId(userId)
                    .fullName(fullName)
                    .totalTicketPrice(order.getTotalTicketPrice())
                    .totalFoodPrice(order.getTotalFoodPrice())
                    .memberDiscountAmount(order.getMemberDiscountAmount())
                    .discountAmount(order.getDiscountAmount())
                    .pointsEarned(order.getPointsEarned())
                    .finalPrice(order.getFinalPrice())
                    .promotionCode(order.getPromotionCode())
                    .orderStatus(order.getOrderStatus())
                    .bookingTime(order.getBookingTime())
                    .expiredTime(order.getExpiredTime())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .qrCode(order.getQrCode())
                    .tickets(ticketResponses)
                    .foods(foodResponses)
                    .movieTitle(movieTitle)
                    .cinemaName(cinemaName)
                    .cinemaAddress(cinemaAddress)
                    .showTime(showTimeStr)
                    .roomName(roomName)
                    .build();

        } catch (AppException e) {
            log.error("AppException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error getting order: " + e.getMessage(), e);
        }
    }

    @PreAuthorize("isAuthenticated()")
    public List<OrderResponse> getOrdersByUserId(String userId){
        return orderRepository.findByUsers_UserId(userId)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }
}