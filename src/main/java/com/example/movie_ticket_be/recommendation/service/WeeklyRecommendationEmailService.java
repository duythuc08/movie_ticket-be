package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.auth.service.EmailService;
import com.example.movie_ticket_be.recommendation.dto.response.RecommendationItemResponse;
import com.example.movie_ticket_be.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyRecommendationEmailService {

    private final RecommendationService recommendationService;
    private final EmailService emailService;

    public Map<String, Integer> sendToAllUsers(List<Users> users) {
        int success = 0, skip = 0, fail = 0;
        for (Users user : users) {
            try {
                sendTo(user);
                success++;
            } catch (Exception e) {
                log.warn("[WeeklyEmail] Gửi thất bại cho userId={}: {}", user.getUserId(), e.getMessage());
                fail++;
            }
        }
        log.info("[WeeklyEmail] Hoàn tất — success={}, skip={}, fail={}", success, skip, fail);
        return Map.of("success", success, "skip", skip, "fail", fail);
    }

    public void sendTo(Users user) {
        List<RecommendationItemResponse> movies =
                recommendationService.getRecommendationsForUser(user.getUserId());

        if (movies.isEmpty()) {
            log.debug("[WeeklyEmail] userId={} không có gợi ý, bỏ qua", user.getUserId());
            return;
        }

        String html = buildHtml(user, movies);
        emailService.sendEmail(user.getUsername(), "🎬 Phim gợi ý cho bạn tuần này", html);
        log.info("[WeeklyEmail] Đã gửi mail cho userId={} ({} phim)", user.getUserId(), movies.size());
    }

    private String buildHtml(Users user, List<RecommendationItemResponse> movies) {
        String name = user.getFirstname() != null ? user.getFirstname() : "bạn";

        StringBuilder movieCards = new StringBuilder();
        for (RecommendationItemResponse m : movies) {
            String stars = buildStars(m.getAverageRating() != null ? m.getAverageRating() : 0.0);
            String duration = m.getDuration() != null ? m.getDuration() + " phút" : "";
            movieCards.append("""
                    <tr>
                      <td style="padding: 12px 0; border-bottom: 1px solid #2a2a3e;">
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td width="80" style="vertical-align: top; padding-right: 14px;">
                              <img src="%s" alt="%s"
                                   style="width:76px;height:110px;object-fit:cover;border-radius:8px;display:block;" />
                            </td>
                            <td style="vertical-align: top;">
                              <p style="margin:0 0 4px;font-size:15px;font-weight:700;color:#ffffff;">%s</p>
                              <p style="margin:0 0 4px;font-size:12px;color:#8b8fa8;">%s</p>
                              <p style="margin:0 0 8px;font-size:12px;color:#a0a8c0;">%s</p>
                              <p style="margin:0;font-size:12px;color:#b0b8d0;line-height:1.5;
                                         display:-webkit-box;-webkit-line-clamp:2;
                                         -webkit-box-orient:vertical;overflow:hidden;">%s</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    """.formatted(
                    orEmpty(m.getPosterUrl()),
                    escapeHtml(m.getTitle()),
                    escapeHtml(m.getTitle()),
                    stars + (duration.isEmpty() ? "" : "  •  " + duration),
                    buildSourceBadge(m.getSource()),
                    escapeHtml(orEmpty(m.getDescription()))
            ));
        }

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1.0"/></head>
                <body style="margin:0;padding:0;background-color:#0d0d1a;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0d0d1a;padding:30px 16px;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="max-width:560px;background-color:#13132b;border-radius:16px;
                                    overflow:hidden;box-shadow:0 8px 32px rgba(0,0,0,0.5);">

                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#6c63ff 0%%,#3b82f6 100%%);
                                     padding:32px 28px;text-align:center;">
                            <p style="margin:0 0 6px;font-size:13px;color:rgba(255,255,255,0.7);
                                       letter-spacing:2px;text-transform:uppercase;">Infinity Cinema</p>
                            <h1 style="margin:0;font-size:24px;color:#ffffff;font-weight:800;">
                              🎬 Phim gợi ý tuần này
                            </h1>
                          </td>
                        </tr>

                        <!-- Greeting -->
                        <tr>
                          <td style="padding:24px 28px 8px;">
                            <p style="margin:0;font-size:15px;color:#c8cce8;">
                              Xin chào <strong style="color:#ffffff;">%s</strong>,
                            </p>
                            <p style="margin:8px 0 0;font-size:14px;color:#8b8fa8;line-height:1.6;">
                              Dựa trên sở thích của bạn, đây là những bộ phim chúng tôi nghĩ bạn sẽ thích tuần này.
                            </p>
                          </td>
                        </tr>

                        <!-- Movie list -->
                        <tr>
                          <td style="padding:8px 28px 8px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              %s
                            </table>
                          </td>
                        </tr>

                        <!-- CTA -->
                        <tr>
                          <td style="padding:20px 28px 32px;text-align:center;">
                            <a href="http://localhost:3000"
                               style="display:inline-block;background:linear-gradient(135deg,#6c63ff,#3b82f6);
                                      color:#ffffff;text-decoration:none;font-weight:700;font-size:14px;
                                      padding:14px 36px;border-radius:50px;
                                      box-shadow:0 4px 16px rgba(108,99,255,0.4);">
                              Đặt vé ngay
                            </a>
                          </td>
                        </tr>

                        <!-- Footer -->
                        <tr>
                          <td style="background-color:#0d0d1a;padding:16px 28px;text-align:center;">
                            <p style="margin:0;font-size:11px;color:#4a4a6a;">
                              Bạn nhận được email này vì đã đăng ký tài khoản tại Infinity Cinema.<br/>
                              Email được gửi tự động mỗi thứ Hai hàng tuần.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(name, movieCards.toString());
    }

    private String buildStars(double rating) {
        int full = (int) Math.round(rating / 2.0);
        full = Math.max(0, Math.min(5, full));
        return "⭐".repeat(full) + " " + String.format("%.1f", rating);
    }

    private String buildSourceBadge(String source) {
        if (source == null) return "";
        return switch (source) {
            case "cf" -> "<span style='color:#6c63ff;font-size:11px;'>Gợi ý cá nhân hoá</span>";
            case "cold_start_popularity" -> "<span style='color:#3b82f6;font-size:11px;'>Phim phổ biến</span>";
            default -> "";
        };
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String orEmpty(String s) {
        return s != null ? s : "";
    }
}
