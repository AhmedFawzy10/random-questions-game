import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {
  private static String decodeHtmlEntities(String input) {
    input = input
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&apos;", "'");
    Pattern pattern = Pattern.compile("&#(\\d+);");
    Matcher matcher = pattern.matcher(input);
    StringBuffer decodedBuffer = new StringBuffer();
    while (matcher.find()) {
      int charCode = Integer.parseInt(matcher.group(1));
      matcher.appendReplacement(decodedBuffer, String.valueOf((char) charCode));
    }
    matcher.appendTail(decodedBuffer);

    return decodedBuffer.toString();
  }

  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    String dif = "";
    while (true) {
      System.out.print("Select game difficulty [easy, medium, hard]: ");
      dif = in.nextLine().toLowerCase().strip();
      if (dif.equals("easy") || dif.equals("medium") || dif.equals("hard")) {
        break;
      } else
        System.out.println("Invalid input!\r");
    }
    try {
      String url = "https://opentdb.com/api.php?amount=14&difficulty=" + dif + "&type=multiple";
      URL urlObj = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod("GET");

      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      StringBuilder response = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      JSONParser parser = new JSONParser();
      JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
      JSONArray data = (JSONArray) jsonResponse.get("results");
      short qnum = 1;
      for (Object q : data) {
        JSONObject resultObject = (JSONObject) q;
        String question = decodeHtmlEntities((String) resultObject.get("question"));
        String correctAns = decodeHtmlEntities((String) resultObject.get("correct_answer"));
        JSONArray AnswersArray = (JSONArray) resultObject.get("incorrect_answers");
        String[] Answers = new String[AnswersArray.size() + 1];

        for (int i = 0; i < AnswersArray.size(); i++) {
          Answers[i] = decodeHtmlEntities((String) AnswersArray.get(i));
        }

        Answers[AnswersArray.size()] = correctAns;

        List<String> shuffledAnswers = Arrays.asList(Answers);
        Collections.shuffle(shuffledAnswers);
        System.out.print("\033[H\033[2J");
        System.out.println("Question[" + qnum + "/14]: " + question + "\n");

        if (args.length > 0 && args[0].equals("cheater")) {
          System.out.println(correctAns + "\n");
        }

        char currentChar = 'A';
        for (String element : shuffledAnswers) {
          System.out.println(currentChar + ": " + element);
          currentChar++;
        }
        int ansChr;
        while (true) {
          System.out.print("\nPlease select correct answer: ");
          String ansInp = (in.next().toLowerCase().strip());
          ansChr = (char) ansInp.charAt(0) - 97;
          if (ansChr < 0 || ansChr > 3) {
            System.out.println("Invalid input!\r");
          } else {
            break;
          }
        }
        String ans = shuffledAnswers.get(ansChr);
        if (!ans.equals(correctAns)) {
          System.out.println("""
              \nSorry you lose
              \nThe Correct Answer Is""" + " \"" + correctAns + "\"");
          break;
        }

        qnum++;
        System.out.println("Correct Answer");
        Thread.sleep(1000);
      }
      if (qnum==15) {
        System.out.println("\nCongratulations :)");
      }

    } catch (Exception e) {
      System.out.println(e);
      System.out.println("Internet Error");
    }

    System.out.print("\nCoseing...");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
