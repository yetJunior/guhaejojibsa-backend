package mutsa.api.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtil {

    public static String REFRESH_TOKEN = "refresh_token";

    public static Optional<Cookie> getCookie(HttpServletRequest request, String key) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(key))
                .findAny();
    }

    public static void setCookie(HttpServletResponse response, String key, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    public static void setCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    }

    public static void removeCookie(HttpServletRequest request, HttpServletResponse response,
                                    String name) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    public static String serialize(Object o) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(o));
    }

    public static <T> T deserialize(Cookie cookie,
                                    Class<T> tClass) {
        return tClass.cast(
                SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
    }
}
