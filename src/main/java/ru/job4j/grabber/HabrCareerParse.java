package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements desc = document.select(".style-ugc");
        return desc.text();
    }

    public List<Post> list(String link) throws IOException {
        HabrCareerDateTimeParser dateParser = new HabrCareerDateTimeParser();
        List<Post> posts = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= 5; pageNumber++) {
            Connection connection = Jsoup.connect(PAGE_LINK + pageNumber);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".basic-date").first();
                String vacancyName = titleElement.text();
                String linkToVac = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String date = dateElement.attr("datetime");
                posts.add(new Post(vacancyName, linkToVac, retrieveDescription(link)), dateParser.parse(date));
            });
        }
        return posts;
    }
}