package MyPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class MyServlet
 */
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String apiKey = "cf5b5a88ccc60ea7a6c745bd08379b93";

        // Get the city from the form input
        String city = request.getParameter("city");
        if (city == null || city.isEmpty()) {
            response.getWriter().write("City name cannot be empty.");
            return;
        }

        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&appid=" + apiKey;

        HttpURLConnection connection = null;
        try {
            // API request
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check for successful response
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                response.getWriter().write("Failed to fetch weather data. HTTP Error: " + status);
                return;
            }

            // Read data from API
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            Scanner scanner = new Scanner(reader);
            StringBuilder responseContent = new StringBuilder();
            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }
            scanner.close();

            // Parse JSON response
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

            // Extract weather data
            String date = new Date(jsonObject.get("dt").getAsLong() * 1000).toString();
            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int temperatureCelsius = (int) (temperatureKelvin - 273.15);
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
            String weatherCondition = jsonObject.getAsJsonArray("weather")
                                                .get(0).getAsJsonObject().get("main").getAsString();

            // Pass data to JSP
            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("temperature", temperatureCelsius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);

            request.getRequestDispatcher("index.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("An error occurred: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
