import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestGemini {
    public static void main(String[] args) {
        try {
            String apiKey = "AIzaSyDwDCfv1cVVoUipZgngMAeMePMrqYSXMrI";
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            String json = response.toString();
            String[] parts = json.split("\"name\": \"");
            for (int i = 1; i < parts.length; i++) {
                String name = parts[i].substring(0, parts[i].indexOf("\""));
                if (name.contains("gemini")) {
                    System.out.println(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
